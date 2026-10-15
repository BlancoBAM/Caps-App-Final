package com.eastonlearning;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

/**
 * KittenTTSClient provides high-fidelity, real-time speech synthesis directly on-device.
 *
 * Runs the embedded KittenTTS 0.8 Nano ONNX neural model offline using ONNX Runtime.
 * All dynamic speech (e.g. child's name, customized praise, spelling prompts) is generated
 * in real time (~150ms) using Caps the Capybara's custom voice persona and cached to disk.
 */
public class KittenTTSClient {

    private static final String TAG = "KittenTTSClient";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static OrtEnvironment ortEnv;
    private static OrtSession ortSession;
    private static float[] voiceStyleMatrix; // 400 x 256 float array
    private static Map<Character, Integer> vocabMap;
    private static Map<String, String> ipaDict;
    private static boolean isInitialized = false;
    private static boolean isInitializing = false;

    public interface SpeechCallback {
        void onSuccess(File audioFile);
        void onFailure(Exception error);
    }

    private static String hashText(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }

    public static File getCachedAudioFile(Context context, String text) {
        File cacheDir = new File(context.getCacheDir(), "kittentts");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return new File(cacheDir, "speech_" + hashText(text) + ".wav");
    }

    /**
     * Initializes the ONNX model, character vocabulary, IPA pronunciation dictionary,
     * and Caps voice persona embedding from APK assets in the background.
     */
    public static synchronized void init(Context context) {
        if (isInitialized || isInitializing) return;
        isInitializing = true;

        EXECUTOR.execute(() -> {
            try {
                Context appCtx = context.getApplicationContext();

                // 1. Initialize ONNX Environment
                if (ortEnv == null) {
                    ortEnv = OrtEnvironment.getEnvironment();
                }

                // 2. Extract or open ONNX Model
                File modelFile = new File(appCtx.getFilesDir(), "kitten_tts_nano_v2.onnx");
                if (!modelFile.exists() || modelFile.length() < 1000000) {
                    try (InputStream is = appCtx.getAssets().open("models/kitten_tts_nano_v2.onnx");
                         FileOutputStream fos = new FileOutputStream(modelFile)) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = is.read(buf)) != -1) {
                            fos.write(buf, 0, r);
                        }
                    }
                }

                OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
                opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);
                opts.setIntraOpNumThreads(2);
                ortSession = ortEnv.createSession(modelFile.getAbsolutePath(), opts);

                // 3. Load Caps Voice Style Embedding (400 x 256 float32)
                voiceStyleMatrix = new float[400 * 256];
                try (InputStream is = appCtx.getAssets().open("models/caps_voice_style.bin")) {
                    byte[] bytes = new byte[400 * 256 * 4];
                    int totalRead = 0;
                    while (totalRead < bytes.length) {
                        int read = is.read(bytes, totalRead, bytes.length - totalRead);
                        if (read == -1) break;
                        totalRead += read;
                    }
                    ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                    FloatBuffer fb = bb.asFloatBuffer();
                    fb.get(voiceStyleMatrix);
                }

                // 4. Load Character Vocab
                vocabMap = new HashMap<>();
                try (InputStream is = appCtx.getAssets().open("models/vocab.json")) {
                    byte[] data = new byte[is.available()];
                    is.read(data);
                    String jsonStr = new String(data, StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(jsonStr);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (key.length() > 0) {
                            vocabMap.put(key.charAt(0), json.getInt(key));
                        }
                    }
                }

                // 5. Load IPA Dictionary
                ipaDict = new HashMap<>();
                try (InputStream is = appCtx.getAssets().open("models/ipa_dict.json")) {
                    byte[] data = new byte[is.available()];
                    is.read(data);
                    String jsonStr = new String(data, StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(jsonStr);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        ipaDict.put(key.toLowerCase(), json.getString(key));
                    }
                }

                isInitialized = true;
                isInitializing = false;
                Log.i(TAG, "On-device KittenTTS engine successfully initialized for Caps!");

            } catch (Exception e) {
                isInitializing = false;
                Log.e(TAG, "Failed to initialize on-device KittenTTS engine", e);
            }
        });
    }

    /**
     * Synthesizes text in real time using the on-device KittenTTS neural model.
     * Checks local cache first for instant playback.
     */
    public static void synthesizeSpeech(Context context, String text, SpeechCallback callback) {
        synthesizeSpeech(context, null, text, callback);
    }

    public static void synthesizeSpeech(Context context, String apiKey, String text, SpeechCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        File cached = getCachedAudioFile(context, text);
        if (cached.exists() && cached.length() > 44) {
            mainHandler.post(() -> callback.onSuccess(cached));
            return;
        }

        EXECUTOR.execute(() -> {
            try {
                if (!isInitialized) {
                    init(context);
                    // Wait up to 3 seconds for initial load
                    int waits = 0;
                    while (!isInitialized && waits < 30) {
                        Thread.sleep(100);
                        waits++;
                    }
                }

                if (!isInitialized || ortSession == null) {
                    throw new IllegalStateException("On-device TTS engine not ready");
                }

                // Tokenize text using IPA dictionary & vocabulary
                long[] tokenIds = textToTokenIds(text);
                if (tokenIds.length <= 3) {
                    throw new IllegalArgumentException("No speakable tokens generated");
                }

                // Select voice style vector based on token sequence length
                int styleIdx = Math.min(tokenIds.length, 399);
                float[] styleVector = new float[256];
                System.arraycopy(voiceStyleMatrix, styleIdx * 256, styleVector, 0, 256);

                // Prepare ONNX Tensors
                OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnv, LongBuffer.wrap(tokenIds), new long[]{1, tokenIds.length});
                OnnxTensor styleTensor = OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(styleVector), new long[]{1, 256});
                OnnxTensor speedTensor = OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(new float[]{1.0f}), new long[]{1});

                Map<String, OnnxTensor> inputs = new HashMap<>();
                inputs.put("input_ids", inputTensor);
                inputs.put("style", styleTensor);
                inputs.put("speed", speedTensor);

                // Run ONNX Inference (<150ms on mobile CPU)
                try (OrtSession.Result result = ortSession.run(inputs)) {
                    float[] waveform = (float[]) result.get(0).getValue();

                    // Model produces trailing padding of ~5000 samples (~200ms)
                    int usefulSamples = Math.max(1, waveform.length - 5000);

                    // Write 16-bit PCM WAV (24000 Hz, mono)
                    writeWavFile(cached, waveform, usefulSamples, 24000);

                    mainHandler.post(() -> callback.onSuccess(cached));
                }

            } catch (Exception e) {
                Log.e(TAG, "On-device speech synthesis failed: " + text, e);
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    private static long[] textToTokenIds(String text) {
        String clean = text.toLowerCase().replaceAll("[^a-z0-9\\s?!,.]", " ");
        String[] words = clean.trim().split("\\s+");

        StringBuilder phonemeBuilder = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (ipaDict != null && ipaDict.containsKey(w)) {
                phonemeBuilder.append(ipaDict.get(w)).append(" ");
            } else {
                phonemeBuilder.append(w).append(" ");
            }
        }

        String phonemeText = phonemeBuilder.toString().trim();
        List<Long> tokens = new ArrayList<>();
        tokens.add(0L); // Start token

        for (int i = 0; i < phonemeText.length(); i++) {
            char c = phonemeText.charAt(i);
            if (vocabMap != null && vocabMap.containsKey(c)) {
                tokens.add((long) vocabMap.get(c));
            }
        }

        tokens.add(10L); // End token 1
        tokens.add(0L);  // End token 2

        long[] arr = new long[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            arr[i] = tokens.get(i);
        }
        return arr;
    }

    private static void writeWavFile(File outFile, float[] samples, int numSamples, int sampleRate) throws Exception {
        int numChannels = 1;
        int bitsPerSample = 16;
        int dataSize = numSamples * numChannels * (bitsPerSample / 8);
        int totalSize = 36 + dataSize;

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            // RIFF Header
            fos.write("RIFF".getBytes(StandardCharsets.US_ASCII));
            fos.write(intToByteArray(totalSize));
            fos.write("WAVE".getBytes(StandardCharsets.US_ASCII));

            // fmt Subchunk
            fos.write("fmt ".getBytes(StandardCharsets.US_ASCII));
            fos.write(intToByteArray(16)); // Subchunk1Size
            fos.write(shortToByteArray((short) 1)); // PCM format = 1
            fos.write(shortToByteArray((short) numChannels));
            fos.write(intToByteArray(sampleRate));
            fos.write(intToByteArray(sampleRate * numChannels * (bitsPerSample / 8))); // ByteRate
            fos.write(shortToByteArray((short) (numChannels * (bitsPerSample / 8))));  // BlockAlign
            fos.write(shortToByteArray((short) bitsPerSample));

            // data Subchunk
            fos.write("data".getBytes(StandardCharsets.US_ASCII));
            fos.write(intToByteArray(dataSize));

            // Convert float [-1.0, 1.0] to 16-bit PCM little-endian
            byte[] pcmBuffer = new byte[numSamples * 2];
            int pcmIdx = 0;
            for (int i = 0; i < numSamples; i++) {
                float sample = Math.max(-1.0f, Math.min(1.0f, samples[i]));
                short val = (short) (sample * 32767.0f);
                pcmBuffer[pcmIdx++] = (byte) (val & 0xff);
                pcmBuffer[pcmIdx++] = (byte) ((val >> 8) & 0xff);
            }
            fos.write(pcmBuffer);
            fos.flush();
        }
    }

    private static byte[] intToByteArray(int val) {
        return new byte[]{
                (byte) (val & 0xff),
                (byte) ((val >> 8) & 0xff),
                (byte) ((val >> 16) & 0xff),
                (byte) ((val >> 24) & 0xff)
        };
    }

    private static byte[] shortToByteArray(short val) {
        return new byte[]{
                (byte) (val & 0xff),
                (byte) ((val >> 8) & 0xff)
        };
    }
}
