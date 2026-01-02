package com.eastonlearning;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    
    private TextToSpeech tts;
    private boolean ttsAvailable = false;
    
    private Button btnListening;
    private Button btnSpelling;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize buttons
        btnListening = findViewById(R.id.btnListening);
        btnSpelling = findViewById(R.id.btnSpelling);
        
        // Hide buttons initially to prevent "BUTTON 1"/"BUTTON 2" flash
        btnListening.setVisibility(android.view.View.GONE);
        btnSpelling.setVisibility(android.view.View.GONE);
        
        // Initialize TTS
        initializeTTS();
    }
    
    private void initializeTTS() {
        tts = new TextToSpeech(this, this);
        setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int langStatus = tts.setLanguage(Locale.US);
            
            if (langStatus == TextToSpeech.LANG_MISSING_DATA || 
                langStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsAvailable = false;
                Toast.makeText(this, 
                    "TTS language not available — please install TTS data or choose another engine.", 
                    Toast.LENGTH_LONG).show();
            } else {
                // TTS is available and language is supported
                tts.setPitch(1.4f);
                tts.setSpeechRate(0.7f);
                ttsAvailable = true;
            }
        } else {
            ttsAvailable = false;
            Toast.makeText(this, 
                "Text-to-speech engine failed to initialize. The app UI will still work, but there will be no spoken audio.", 
                Toast.LENGTH_LONG).show();
        }
        
        // Always show the UI regardless of TTS status
        showMainMenu();
    }
    
    private void showMainMenu() {
        runOnUiThread(() -> {
            // Set correct button labels
            btnListening.setText("LEARN BY LISTENING");
            btnSpelling.setText("SPELLING BEE");
            
            // Make buttons visible
            btnListening.setVisibility(android.view.View.VISIBLE);
            btnSpelling.setVisibility(android.view.View.VISIBLE);
            
            // Set up click listeners
            btnListening.setOnClickListener(v -> onListeningModeClick());
            btnSpelling.setOnClickListener(v -> onSpellingModeClick());
        });
    }
    
    private void onListeningModeClick() {
        speak("Starting Learn by Listening mode");
        // TODO: Navigate to listening activity
    }
    
    private void onSpellingModeClick() {
        speak("Starting Spelling Bee mode");
        // TODO: Navigate to spelling activity
    }
    
    /**
     * Safe speak wrapper that only calls TTS when available
     * Updates visual elements regardless of TTS availability
     */
    private void speak(String text) {
        // Always update UI (you can add subtitle/speech bubble here)
        runOnUiThread(() -> {
            // Optional: Update a TextView subtitle or speech bubble
            // subtitleText.setText(text);
        });
        
        // Only attempt TTS if available
        if (!ttsAvailable || tts == null) {
            // Silent fallback - UI still works
            return;
        }
        
        // Use modern speak API when available
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
