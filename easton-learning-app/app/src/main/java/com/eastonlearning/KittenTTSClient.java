package com.eastonlearning;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * KittenTTSClient handles communication with the KittenML TTS API (https://api.kittenml.com/v1/audio/speech).
 * Uses KittenTTS 0.8 Nano / Mini with Caps' voice ('Rosie') and caches all generated MP3s on disk.
 */
public class KittenTTSClient {

    private static final String TAG = "KittenTTSClient";
    private static final String API_URL = "https://api.kittenml.com/v1/audio/speech";
    private static final String DEFAULT_MODEL = "kitten-tts-nano-0.8";
    private static final String DEFAULT_VOICE = "Rosie";

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
        return new File(cacheDir, "speech_" + hashText(text) + ".mp3");
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo != null && netInfo.isConnected();
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Synthesizes text using the KittenML Cloud API if an API key is provided,
     * or retrieves from local disk cache if previously synthesized.
     */
    public static void synthesizeSpeech(Context context, String apiKey, String text, SpeechCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        File cached = getCachedAudioFile(context, text);
        if (cached.exists() && cached.length() > 0) {
            mainHandler.post(() -> callback.onSuccess(cached));
            return;
        }

        if (apiKey == null || apiKey.trim().isEmpty() || !isNetworkAvailable(context)) {
            mainHandler.post(() -> callback.onFailure(new IllegalStateException("No API key or network unavailable")));
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("model", DEFAULT_MODEL);
                json.put("input", text);
                json.put("voice", DEFAULT_VOICE);
                json.put("response_format", "mp3");
                json.put("speed", 1.0);

                byte[] postBytes = json.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postBytes);
                    os.flush();
                }

                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    try (InputStream is = conn.getInputStream();
                         FileOutputStream fos = new FileOutputStream(cached)) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                        fos.flush();
                    }

                    mainHandler.post(() -> callback.onSuccess(cached));
                } else {
                    mainHandler.post(() -> callback.onFailure(new Exception("KittenML API returned HTTP " + code)));
                }

            } catch (Exception e) {
                Log.e(TAG, "Speech synthesis failed", e);
                mainHandler.post(() -> callback.onFailure(e));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
