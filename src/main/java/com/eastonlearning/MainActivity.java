package com.eastonlearning;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;
import java.util.Random;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.os.Build;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private SharedPreferences prefs;
    private boolean isFirstLaunch;
    private GameMode currentMode = GameMode.MENU;
    private SpellingBeeLevel currentLevel = SpellingBeeLevel.LEVEL_1;
    private String currentWord = "";
    private int currentLetterIndex = 0;
    private int correctAnswers = 0;
    private int totalAnswers = 0;
    private int consecutiveFourLetterWords = 0;
    private int level3WordCount = 0;
    private int sessionCorrectAnswers = 0;
    private int sessionTotalAnswers = 0;
    private Random random = new Random();
    
    private TextView mainText;
    private Button button1, button2;
    private ImageView capsImage;
    private OnScreenKeyboard keyboard;
    private SpeechBubbleView speechBubble;
    
    private enum GameMode { MENU, LEARN_BY_LISTENING, SPELLING_BEE }
    private enum SpellingBeeLevel { LEVEL_1, LEVEL_2, LEVEL_3 }
    
    private String[] level1Words = {"a", "i", "go", "no", "up", "me", "we", "be", "to", "do", "it", "at", "on", "in", "is", "am", "or", "so"};
    private String[] level2Words = {"cat", "dog", "run", "fun", "sun", "hat", "bat", "sit", "hit", "big", "red", "cup", "pen", "box", "car", "bus"};
    private String[] level3OneToThreeWords = {"a", "i", "go", "no", "up", "me", "we", "be", "to", "do", "cat", "dog", "run", "fun", "sun", "hat", "bat", "sit", "hit", "big"};
    private String[] level3FourLetterWords = {"play", "jump", "help", "love", "good", "nice", "fast", "slow", "wish", "keep", "make", "take", "come", "home"};
    private String[] level3FiveLetterWords = {"apple", "house", "water", "smile", "laugh", "happy", "magic", "brave"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeTTS();
        checkFirstLaunch();
    }
    
    private void initializeViews() {
        mainText = findViewById(R.id.mainText);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        capsImage = findViewById(R.id.capsImage);
        keyboard = findViewById(R.id.onScreenKeyboard);
        speechBubble = findViewById(R.id.speechBubble);
        
        button1.setOnClickListener(v -> handleButton1Click());
        button2.setOnClickListener(v -> handleButton2Click());
        keyboard.setOnKeyPressListener(this::handleKeyPress);
    }
    
    private void initializeTTS() {
        tts = new TextToSpeech(this, this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            // Higher pitch for cute, cartoon-like voice
            tts.setPitch(1.4f);
            // Slower speech rate for better comprehension by children
            tts.setSpeechRate(0.7f);
            
            if (isFirstLaunch) {
                showFirstLaunchIntro();
            } else {
                showMainMenu();
            }
        }
    }
    
    private void checkFirstLaunch() {
        prefs = getSharedPreferences("EastonLearning", MODE_PRIVATE);
        isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);
        loadProgress();
    }
    
    private void loadProgress() {
        // Load saved progress
        sessionCorrectAnswers = prefs.getInt("sessionCorrect", 0);
        sessionTotalAnswers = prefs.getInt("sessionTotal", 0);
        
        // Load current level progress
        String levelName = prefs.getString("currentLevel", "LEVEL_1");
        currentLevel = SpellingBeeLevel.valueOf(levelName);
    }
    
    private void saveProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("sessionCorrect", sessionCorrectAnswers);
        editor.putInt("sessionTotal", sessionTotalAnswers);
        editor.putString("currentLevel", currentLevel.name());
        editor.apply();
    }
    
    private void showFirstLaunchIntro() {
        capsImage.setVisibility(View.VISIBLE);
        
        String introMessage = "Hey there Easton! My name is Caps the Capybara, and I'd I'm here to help you learn some letters, words, and typing. I like to have fun and learn and I hope you do too!";
        
        boolean hasHeadphones = areHeadphonesConnected();
        String audioMessage = hasHeadphones ? 
            "Great! I can see you have headphones connected. That's perfect for learning!" :
            "Since this is your first time, it's very important that you can hear me at all times. Please find a quiet place without distractions, and consider putting on headphones if you have them.";
        
        String fullMessage = introMessage + " " + audioMessage + " Since this is your first time, let's start with Learn By Listening mode. Are you ready?";
        
        mainText.setText(fullMessage);
        speak(fullMessage);
        
        button1.setText("I'M READY!");
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.GONE);
        
        prefs.edit().putBoolean("isFirstLaunch", false).apply();
    }
    
    private boolean areHeadphonesConnected() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    return true;
                }
            }
        } else {
            return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
        }
        return false;
    }
    
    private void showMainMenu() {
        currentMode = GameMode.MENU;
        capsImage.setVisibility(View.VISIBLE);
        
        // Add gentle breathing animation to capybara
        animateCapybara();
        
        mainText.setText("Hi Easton! Choose how you'd like to learn today!");
        
        speak("Hi Easton! Choose how you'd like to learn today!");
        
        button1.setText("LEARN BY LISTENING");
        button2.setText("SPELLING BEE");
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
    }
    
    private void animateCapybara() {
        // Gentle breathing animation
        capsImage.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(2000)
            .withEndAction(() -> {
                capsImage.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(2000)
                    .withEndAction(this::animateCapybara);
            });
    }
    
    private void handleButton1Click() {
        switch (currentMode) {
            case MENU:
                startLearnByListening();
                break;
            case SPELLING_BEE:
                continueSpellingBee();
                break;
        }
    }
    
    private void handleButton2Click() {
        switch (currentMode) {
            case MENU:
                startSpellingBee();
                break;
            case SPELLING_BEE:
                advanceSpellingBeeLevel();
                break;
        }
    }
    
    private void startLearnByListening() {
        currentMode = GameMode.LEARN_BY_LISTENING;
        mainText.setText("Great choice Easton! Press any letter and I'll say it for you. Have fun exploring!");
        speak("Great choice Easton! Press any letter and I'll say it for you. Have fun exploring!");
        
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        keyboard.setVisibility(View.VISIBLE);
    }
    
    private void startSpellingBee() {
        currentMode = GameMode.SPELLING_BEE;
        currentLevel = SpellingBeeLevel.LEVEL_1;
        correctAnswers = 0;
        totalAnswers = 0;
        
        mainText.setText("Let's practice spelling words together Easton!");
        speak("Let's practice spelling words together Easton!");
        
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        keyboard.setVisibility(View.VISIBLE);
        
        nextSpellingWord();
    }
    
    private void nextSpellingWord() {
        currentWord = getNextWord();
        currentLetterIndex = 0;
        
        mainText.setText("The word " + currentWord + " is spelled:");
        speak("The word " + currentWord + " is spelled " + spellOutWord(currentWord) + ". Now you try typing it!");
    }
    
    private String getNextWord() {
        switch (currentLevel) {
            case LEVEL_1:
                return level1Words[random.nextInt(level1Words.length)];
            case LEVEL_2:
                return level2Words[random.nextInt(level2Words.length)];
            case LEVEL_3:
                return getLevel3Word();
            default:
                return level1Words[random.nextInt(level1Words.length)];
        }
    }
    
    private String getLevel3Word() {
        // First 20 words cannot be 5-letter words
        if (level3WordCount < 20) {
            if (random.nextInt(10) < 7) { // 70% chance for 1-3 letter words
                String word = level3OneToThreeWords[random.nextInt(level3OneToThreeWords.length)];
                consecutiveFourLetterWords = 0;
                level3WordCount++;
                return word;
            } else { // 30% chance for 4-letter words, but not more than 2 consecutive
                if (consecutiveFourLetterWords < 2) {
                    consecutiveFourLetterWords++;
                    level3WordCount++;
                    return level3FourLetterWords[random.nextInt(level3FourLetterWords.length)];
                } else {
                    consecutiveFourLetterWords = 0;
                    level3WordCount++;
                    return level3OneToThreeWords[random.nextInt(level3OneToThreeWords.length)];
                }
            }
        } else {
            // After first 20 words, include 1% 5-letter words
            int rand = random.nextInt(100);
            if (rand < 1) { // 1% five-letter words
                consecutiveFourLetterWords = 0;
                return level3FiveLetterWords[random.nextInt(level3FiveLetterWords.length)];
            } else if (rand < 31) { // 30% four-letter words, but not more than 2 consecutive
                if (consecutiveFourLetterWords < 2) {
                    consecutiveFourLetterWords++;
                    return level3FourLetterWords[random.nextInt(level3FourLetterWords.length)];
                } else {
                    consecutiveFourLetterWords = 0;
                    return level3OneToThreeWords[random.nextInt(level3OneToThreeWords.length)];
                }
            } else { // 69% 1-3 letter words
                consecutiveFourLetterWords = 0;
                return level3OneToThreeWords[random.nextInt(level3OneToThreeWords.length)];
            }
        }
    }
    
    private String spellOutWord(String word) {
        StringBuilder spelled = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            spelled.append(word.charAt(i));
            if (i < word.length() - 1) spelled.append(" ");
        }
        return spelled.toString();
    }
    
    private void handleKeyPress(char key) {
        switch (currentMode) {
            case LEARN_BY_LISTENING:
                handleLearnByListening(key);
                break;
            case SPELLING_BEE:
                handleSpellingInput(key);
                break;
        }
    }
    
    private void handleLearnByListening(char key) {
        speak(String.valueOf(key));
        
        // Show visual feedback
        String message = "Great job Easton! You pressed the letter " + Character.toUpperCase(key) + "!";
        mainText.setText(message);
        
        // Add fun animation to the main text
        mainText.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction(() -> {
                mainText.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        mainText.setText("Keep exploring Easton! Press any letter to hear its sound.");
                    });
            });
    }
    
    private void handleSpellingInput(char key) {
        if (currentLetterIndex < currentWord.length()) {
            char expectedLetter = currentWord.charAt(currentLetterIndex);
            
            if (Character.toLowerCase(key) == Character.toLowerCase(expectedLetter)) {
                speak(String.valueOf(expectedLetter));
                currentLetterIndex++;
                
                if (currentLetterIndex >= currentWord.length()) {
                    correctAnswers++;
                    totalAnswers++;
                    sessionCorrectAnswers++;
                    sessionTotalAnswers++;
                    speak("Great job Easton! You spelled " + currentWord + " perfectly!");
                    
                    saveProgress();
                    checkLevelProgress();
                } else {
                    mainText.setText("Good! Keep going Easton. Next letter:");
                }
            } else {
                speak("I'm sorry, that's the letter " + key + ", and it is not part of the word we are trying to spell right now. Try again. The next letter is " + expectedLetter);
                totalAnswers++;
                sessionTotalAnswers++;
                saveProgress();
            }
        }
    }
    
    private void checkLevelProgress() {
        // Check for level advancement after every 10 words
        if (totalAnswers >= 10 && totalAnswers % 10 == 0) {
            double accuracy = (double) correctAnswers / totalAnswers;
            
            if ((currentLevel == SpellingBeeLevel.LEVEL_1 && accuracy >= 0.75) ||
                (currentLevel == SpellingBeeLevel.LEVEL_2 && accuracy >= 0.85)) {
                showLevelUpOption();
                return;
            }
        }
        nextSpellingWord();
    }
    
    private void showLevelUpOption() {
        String message = currentLevel == SpellingBeeLevel.LEVEL_1 ? 
            "You've been doing great Easton! If you feel ready, we can begin the next level with some more difficult words! Or you can stick with this for a little while longer. What would you like to do?" :
            "WOW I'm impressed Easton! You're getting really good at this! If you think you can handle it, let's review what we've learned so far!";
        
        mainText.setText(message);
        speak(message);
        
        button1.setText("STAY");
        button2.setText("NEXT");
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        keyboard.setVisibility(View.GONE);
        
        // Highlight buttons sequentially
        button1.postDelayed(() -> {
            highlightButton(button1, "This is to stay here");
            button1.postDelayed(() -> {
                highlightButton(button2, "Press this for the next level");
            }, 2000);
        }, 1000);
    }
    
    private void highlightButton(Button button, String description) {
        button.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .alpha(0.8f)
            .setDuration(800)
            .withEndAction(() -> {
                speak(description);
                button.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(500);
            });
    }
    
    private void continueSpellingBee() {
        correctAnswers = 0;
        totalAnswers = 0;
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        keyboard.setVisibility(View.VISIBLE);
        nextSpellingWord();
    }
    
    private void advanceSpellingBeeLevel() {
        if (currentLevel == SpellingBeeLevel.LEVEL_1) {
            currentLevel = SpellingBeeLevel.LEVEL_2;
        } else if (currentLevel == SpellingBeeLevel.LEVEL_2) {
            currentLevel = SpellingBeeLevel.LEVEL_3;
            // Reset level 3 specific counters
            consecutiveFourLetterWords = 0;
            level3WordCount = 0;
        }
        
        correctAnswers = 0;
        totalAnswers = 0;
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        keyboard.setVisibility(View.VISIBLE);
        nextSpellingWord();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle letter keys
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            char letter = (char) ('a' + (keyCode - KeyEvent.KEYCODE_A));
            handleKeyPress(letter);
            return true;
        }
        
        // Handle back button for main menu navigation
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentMode != GameMode.MENU) {
                showMainMenu();
                return true;
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Also handle key up events for external keyboards
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            // Visual feedback for key release on external keyboard
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        
        // Show speech bubble with animated text
        if (speechBubble != null) {
            speechBubble.setVisibility(View.VISIBLE);
            speechBubble.showText(text);
        }
    }
    
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}