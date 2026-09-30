package com.eastonlearning;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private SharedPreferences prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable stopTalkingRunnable;

    // Child name (persisted in SharedPreferences)
    private String childName = "Friend";
    private boolean isFirstLaunch = true;

    // Game Modes & Levels
    private enum GameMode { NAME_ENTRY, MENU, LEARN_BY_LISTENING, SPELLING_BEE }
    private enum SpellingLevel { LEVEL_1, LEVEL_2, LEVEL_3 }

    private GameMode currentMode = GameMode.MENU;
    private SpellingLevel currentLevel = SpellingLevel.LEVEL_1;

    // Game stats
    private int correctAnswers = 0;
    private int missedAnswers = 0;
    private int lettersExplored = 0;
    private int wordsCompletedInSession = 0;

    // Spelling state
    private String currentWord = "";
    private int currentLetterIndex = 0;
    private final List<TextView> letterBoxViews = new ArrayList<>();
    private final Random random = new Random();

    // UI Elements
    private TextView txtPlayer, txtStats;
    private Button btnMenu;
    private CapybaraView capsView;
    private SpeechBubbleView speechBubble;
    private OnScreenKeyboard onScreenKeyboard;

    // Content Panels
    private LinearLayout nameEntryPanel;
    private EditText nameInput;
    private Button nameSubmitButton;

    private LinearLayout menuPanel;
    private Button btnLearnByListening, btnSpellingBee;

    private LinearLayout lblPanel;
    private TextView txtBigLetter, txtPhonics;

    private LinearLayout spellingBeePanel;
    private LinearLayout letterBoxesContainer;
    private TextView txtSpellingHint;
    private Button btnRepeatWord;

    // Word Dictionaries
    private final String[] level1Words = {
            "cat", "dog", "sun", "hat", "pig", "fox", "bed", "cup",
            "car", "bus", "run", "hop", "red", "big", "fun", "bee",
            "bat", "cow", "box", "toy", "pen", "ice", "jam", "pie"
    };

    private final String[] level2Words = {
            "frog", "duck", "bear", "fish", "star", "tree", "bird", "lion",
            "cake", "boat", "moon", "book", "baby", "play", "jump", "ball",
            "door", "lamp", "milk", "nest", "park", "ring", "shoe", "wind"
    };

    private final String[] level3Words = {
            "apple", "tiger", "horse", "puppy", "water", "smile", "happy",
            "cloud", "music", "house", "robot", "zebra", "magic", "brave",
            "candy", "flower", "garden", "kitten", "monkey", "planet"
    };

    // Phonics associations for Learn by Listening
    private static final Map<Character, String[]> PHONICS_DATA = new HashMap<>();
    static {
        PHONICS_DATA.put('A', new String[]{"Apple 🍎", "A is for Apple! Apple starts with A!"});
        PHONICS_DATA.put('B', new String[]{"Bear 🐻", "B is for Bear! Big friendly bear!"});
        PHONICS_DATA.put('C', new String[]{"Cat 🐱", "C is for Cat! Meow!"});
        PHONICS_DATA.put('D', new String[]{"Dog 🐶", "D is for Dog! Woof woof!"});
        PHONICS_DATA.put('E', new String[]{"Elephant 🐘", "E is for Elephant! Giant elephant!"});
        PHONICS_DATA.put('F', new String[]{"Fish 🐟", "F is for Fish! Swimming in the sea!"});
        PHONICS_DATA.put('G', new String[]{"Giraffe 🦒", "G is for Giraffe! Tall neck giraffe!"});
        PHONICS_DATA.put('H', new String[]{"Hat 🎩", "H is for Hat! Put on your thinking hat!"});
        PHONICS_DATA.put('I', new String[]{"Ice Cream 🍦", "I is for Ice Cream! Yummy treat!"});
        PHONICS_DATA.put('J', new String[]{"Jellyfish 🪼", "J is for Jellyfish! Floating in the water!"});
        PHONICS_DATA.put('K', new String[]{"Kangaroo 🦘", "K is for Kangaroo! Hop hop hop!"});
        PHONICS_DATA.put('L', new String[]{"Lion 🦁", "L is for Lion! King of the jungle!"});
        PHONICS_DATA.put('M', new String[]{"Monkey 🐵", "M is for Monkey! Eating a banana!"});
        PHONICS_DATA.put('N', new String[]{"Nest 🪺", "N is for Nest! Cozy bird home!"});
        PHONICS_DATA.put('O', new String[]{"Orange 🍊", "O is for Orange! Sweet citrus fruit!"});
        PHONICS_DATA.put('P', new String[]{"Panda 🐼", "P is for Panda! Munching on bamboo!"});
        PHONICS_DATA.put('Q', new String[]{"Queen 👑", "Q is for Queen! Wearing a golden crown!"});
        PHONICS_DATA.put('R', new String[]{"Rainbow 🌈", "R is for Rainbow! Beautiful colors!"});
        PHONICS_DATA.put('S', new String[]{"Sun ☀️", "S is for Sun! Warm and bright!"});
        PHONICS_DATA.put('T', new String[]{"Tiger 🐯", "T is for Tiger! Striped and strong!"});
        PHONICS_DATA.put('U', new String[]{"Umbrella ☂️", "U is for Umbrella! Keeping dry in the rain!"});
        PHONICS_DATA.put('V', new String[]{"Violin 🎻", "V is for Violin! Playing sweet music!"});
        PHONICS_DATA.put('W', new String[]{"Watermelon 🍉", "W is for Watermelon! Fresh and juicy!"});
        PHONICS_DATA.put('X', new String[]{"Xylophone 🎵", "X is for Xylophone! Making happy sounds!"});
        PHONICS_DATA.put('Y', new String[]{"Yo-yo 🪀", "Y is for Yo-yo! Spinning up and down!"});
        PHONICS_DATA.put('Z', new String[]{"Zebra 🦓", "Z is for Zebra! Black and white stripes!"});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePreferences();
        initializeViews();
        initializeTTS();

        // Immediately set the UI state on launch without waiting for TTS
        if (isFirstLaunch || TextUtils.isEmpty(childName) || "Friend".equals(childName)) {
            showNameEntry();
        } else {
            showMainMenu();
        }
    }

    private void initializePreferences() {
        prefs = getSharedPreferences("EastonLearning", MODE_PRIVATE);
        isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);
        childName = prefs.getString("childName", "");
        correctAnswers = prefs.getInt("correctAnswers", 0);
        missedAnswers = prefs.getInt("missedAnswers", 0);

        String savedLevel = prefs.getString("currentLevel", "LEVEL_1");
        try {
            currentLevel = SpellingLevel.valueOf(savedLevel);
        } catch (Exception e) {
            currentLevel = SpellingLevel.LEVEL_1;
        }
    }

    private void savePreferences() {
        prefs.edit()
                .putString("childName", childName)
                .putBoolean("isFirstLaunch", isFirstLaunch)
                .putInt("correctAnswers", correctAnswers)
                .putInt("missedAnswers", missedAnswers)
                .putString("currentLevel", currentLevel.name())
                .apply();
    }

    private void initializeViews() {
        // Top Bar
        txtPlayer = findViewById(R.id.txtPlayer);
        txtStats = findViewById(R.id.txtStats);
        btnMenu = findViewById(R.id.btnMenu);

        // Mascot & Bubble & Keyboard
        capsView = findViewById(R.id.capsImage);
        speechBubble = findViewById(R.id.speechBubble);
        onScreenKeyboard = findViewById(R.id.onScreenKeyboard);

        // Name Entry Panel
        nameEntryPanel = findViewById(R.id.nameEntryPanel);
        nameInput = findViewById(R.id.nameInput);
        nameSubmitButton = findViewById(R.id.nameSubmitButton);

        // Menu Panel
        menuPanel = findViewById(R.id.menuPanel);
        btnLearnByListening = findViewById(R.id.btnLearnByListening);
        btnSpellingBee = findViewById(R.id.btnSpellingBee);

        // LBL Panel
        lblPanel = findViewById(R.id.lblPanel);
        txtBigLetter = findViewById(R.id.txtBigLetter);
        txtPhonics = findViewById(R.id.txtPhonics);

        // Spelling Bee Panel
        spellingBeePanel = findViewById(R.id.spellingBeePanel);
        letterBoxesContainer = findViewById(R.id.letterBoxesContainer);
        txtSpellingHint = findViewById(R.id.txtSpellingHint);
        btnRepeatWord = findViewById(R.id.btnRepeatWord);

        // Click Listeners
        btnMenu.setOnClickListener(v -> showMainMenu());
        txtPlayer.setOnClickListener(v -> showNameEntry());
        btnLearnByListening.setOnClickListener(v -> startLearnByListening());
        btnSpellingBee.setOnClickListener(v -> startSpellingBee());
        btnRepeatWord.setOnClickListener(v -> repeatCurrentWord());

        nameSubmitButton.setOnClickListener(v -> submitName());
        nameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                submitName();
                return true;
            }
            return false;
        });

        // Connect on-screen keyboard touch handler
        onScreenKeyboard.setOnKeyPressListener(this::handleKeyPress);

        updatePlayerHeader();
    }

    private void initializeTTS() {
        tts = new TextToSpeech(this, this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setPitch(1.3f);
            tts.setSpeechRate(0.85f);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    // Mouth is already started synchronously based on syllables
                }

                @Override
                public void onDone(String utteranceId) {
                    runOnUiThread(() -> {
                        if (stopTalkingRunnable != null) {
                            mainHandler.removeCallbacks(stopTalkingRunnable);
                            stopTalkingRunnable = null;
                        }
                        if (capsView != null) capsView.stopTalking();
                    });
                }

                @Override
                public void onError(String utteranceId) {
                    runOnUiThread(() -> {
                        if (stopTalkingRunnable != null) {
                            mainHandler.removeCallbacks(stopTalkingRunnable);
                            stopTalkingRunnable = null;
                        }
                        if (capsView != null) capsView.stopTalking();
                    });
                }
            });

            // Initial audio welcome based on current mode
            if (currentMode == GameMode.NAME_ENTRY) {
                speak("Hi there! I'm Caps the Capybara! What's your name?");
            } else if (currentMode == GameMode.MENU) {
                speak("Hi " + childName + "! Choose how you'd like to learn today!");
            }
        }
    }

    private void updatePlayerHeader() {
        if (TextUtils.isEmpty(childName) || "Friend".equals(childName)) {
            txtPlayer.setText("👤 Tap to Set Name ✏️");
        } else {
            txtPlayer.setText("👤 " + childName + " ✏️");
        }
    }

    private void updateScoreDisplay() {
        if (currentMode == GameMode.SPELLING_BEE) {
            String lvl = (currentLevel == SpellingLevel.LEVEL_1) ? "Lvl 1" :
                    (currentLevel == SpellingLevel.LEVEL_2 ? "Lvl 2" : "Lvl 3");
            txtStats.setText("⭐ " + lvl + " | ✅ " + correctAnswers + "  ❌ " + missedAnswers);
        } else if (currentMode == GameMode.LEARN_BY_LISTENING) {
            txtStats.setText("🔤 Letters Explored: " + lettersExplored);
        } else {
            txtStats.setText("⭐ Welcome!");
        }
    }

    // ── 1. Name Entry Mode ────────────────────────────────────────────────────

    private void showNameEntry() {
        currentMode = GameMode.NAME_ENTRY;

        btnMenu.setVisibility(View.GONE);
        nameEntryPanel.setVisibility(View.VISIBLE);
        menuPanel.setVisibility(View.GONE);
        lblPanel.setVisibility(View.GONE);
        spellingBeePanel.setVisibility(View.GONE);
        onScreenKeyboard.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(childName) && !"Friend".equals(childName)) {
            nameInput.setText(childName);
            nameInput.setSelection(childName.length());
        }

        String msg = "Hi there! I'm Caps the Capybara! What's your name?";
        speak(msg);
        updateScoreDisplay();
    }

    private void submitName() {
        String entered = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(entered)) {
            nameInput.setError("Please type your name!");
            return;
        }

        // Capitalize first letter
        childName = entered.substring(0, 1).toUpperCase() + entered.substring(1).toLowerCase();
        isFirstLaunch = false;
        savePreferences();
        updatePlayerHeader();

        // Dismiss virtual soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
        }

        String welcome = "Awesome to meet you, " + childName + "! Let's have fun!";
        speak(welcome);

        mainHandler.postDelayed(this::showMainMenu, 1400);
    }

    // ── 2. Main Menu Mode ─────────────────────────────────────────────────────

    private void showMainMenu() {
        currentMode = GameMode.MENU;

        btnMenu.setVisibility(View.GONE);
        nameEntryPanel.setVisibility(View.GONE);
        menuPanel.setVisibility(View.VISIBLE);
        lblPanel.setVisibility(View.GONE);
        spellingBeePanel.setVisibility(View.GONE);
        onScreenKeyboard.setVisibility(View.GONE);

        String greeting = "Hi " + childName + "! Choose how you'd like to learn today!";
        speak(greeting);
        updatePlayerHeader();
        updateScoreDisplay();
    }

    // ── 3. Learn By Listening Mode ────────────────────────────────────────────

    private void startLearnByListening() {
        currentMode = GameMode.LEARN_BY_LISTENING;

        btnMenu.setVisibility(View.VISIBLE);
        nameEntryPanel.setVisibility(View.GONE);
        menuPanel.setVisibility(View.GONE);
        lblPanel.setVisibility(View.VISIBLE);
        spellingBeePanel.setVisibility(View.GONE);
        onScreenKeyboard.setVisibility(View.VISIBLE);

        txtBigLetter.setText("🔤");
        txtPhonics.setText("Touch or type any letter to hear its sound!");

        String prompt = "Touch any letter on screen or press a key, " + childName + "!";
        speak(prompt);
        updateScoreDisplay();
    }

    private void handleLBLKey(char key) {
        char upper = Character.toUpperCase(key);
        lettersExplored++;
        updateScoreDisplay();

        String[] data = PHONICS_DATA.get(upper);
        String wordWithEmoji = (data != null) ? data[0] : String.valueOf(upper);
        String spokenPhrase = (data != null) ? data[1] : (upper + "! The letter " + upper + "!");

        txtBigLetter.setText(String.valueOf(upper));
        txtPhonics.setText(upper + " is for " + wordWithEmoji);

        // Animate the letter display card
        AnimatorSet anim = new AnimatorSet();
        ObjectAnimator sX = ObjectAnimator.ofFloat(txtBigLetter, "scaleX", 1f, 1.35f, 1f);
        ObjectAnimator sY = ObjectAnimator.ofFloat(txtBigLetter, "scaleY", 1f, 1.35f, 1f);
        anim.playTogether(sX, sY);
        anim.setDuration(240);
        anim.start();

        speak(spokenPhrase);
    }

    // ── 4. Spelling Bee Mode ──────────────────────────────────────────────────

    private void startSpellingBee() {
        currentMode = GameMode.SPELLING_BEE;

        btnMenu.setVisibility(View.VISIBLE);
        nameEntryPanel.setVisibility(View.GONE);
        menuPanel.setVisibility(View.GONE);
        lblPanel.setVisibility(View.GONE);
        spellingBeePanel.setVisibility(View.VISIBLE);
        onScreenKeyboard.setVisibility(View.VISIBLE);

        updateScoreDisplay();
        nextSpellingWord();
    }

    private void nextSpellingWord() {
        currentWord = pickWordForLevel();
        currentLetterIndex = 0;

        buildLetterBoxes(currentWord);

        String firstLetter = String.valueOf(currentWord.charAt(0)).toUpperCase();
        txtSpellingHint.setText("Spell: " + currentWord.toUpperCase());

        String prompt = "Can you spell " + currentWord + ", " + childName + "? The first letter is " + firstLetter + "!";
        speak(prompt);
    }

    private String pickWordForLevel() {
        String[] pool;
        switch (currentLevel) {
            case LEVEL_2:
                pool = level2Words;
                break;
            case LEVEL_3:
                pool = level3Words;
                break;
            case LEVEL_1:
            default:
                pool = level1Words;
                break;
        }
        return pool[random.nextInt(pool.length)];
    }

    private void buildLetterBoxes(String word) {
        letterBoxesContainer.removeAllViews();
        letterBoxViews.clear();

        int boxSize = dpToPx(44);
        int margin = dpToPx(5);

        for (int i = 0; i < word.length(); i++) {
            TextView box = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(boxSize, boxSize);
            lp.setMargins(margin, 0, margin, 0);
            box.setLayoutParams(lp);
            box.setGravity(Gravity.CENTER);
            box.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            box.setTypeface(Typeface.DEFAULT_BOLD);
            box.setElevation(dpToPx(2));

            if (i == 0) {
                // First target box is active
                box.setText("?");
                box.setTextColor(0xFF854D0E);
                box.setBackgroundResource(R.drawable.letter_box_active);
            } else {
                box.setText("_");
                box.setTextColor(0xFF64748B);
                box.setBackgroundResource(R.drawable.letter_box_default);
            }

            letterBoxViews.add(box);
            letterBoxesContainer.addView(box);
        }
    }

    private void handleSpellingKey(char key) {
        if (currentWord.isEmpty() || currentLetterIndex >= currentWord.length()) return;

        char expected = currentWord.charAt(currentLetterIndex);
        boolean isMatch = Character.toLowerCase(key) == Character.toLowerCase(expected);

        TextView currentBox = letterBoxViews.get(currentLetterIndex);

        if (isMatch) {
            // Correct letter!
            currentBox.setText(String.valueOf(Character.toUpperCase(expected)));
            currentBox.setTextColor(Color.WHITE);
            currentBox.setBackgroundResource(R.drawable.letter_box_correct);

            // Pop animation on the letter box
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator sX = ObjectAnimator.ofFloat(currentBox, "scaleX", 1f, 1.25f, 1f);
            ObjectAnimator sY = ObjectAnimator.ofFloat(currentBox, "scaleY", 1f, 1.25f, 1f);
            set.playTogether(sX, sY);
            set.setDuration(180);
            set.start();

            currentLetterIndex++;

            if (currentLetterIndex >= currentWord.length()) {
                // Completed the whole word!
                handleWordCompleted();
            } else {
                // Advance to next letter
                TextView nextBox = letterBoxViews.get(currentLetterIndex);
                nextBox.setText("?");
                nextBox.setTextColor(0xFF854D0E);
                nextBox.setBackgroundResource(R.drawable.letter_box_active);

                char nextExpected = Character.toUpperCase(currentWord.charAt(currentLetterIndex));
                txtSpellingHint.setText("Great! Next letter is " + nextExpected);
                speak(String.valueOf(Character.toUpperCase(expected)));
            }

        } else {
            // Incorrect letter
            missedAnswers++;
            savePreferences();
            updateScoreDisplay();

            // Shake animation on the active box
            ObjectAnimator shake = ObjectAnimator.ofFloat(currentBox, "translationX", 0f, 12f, -12f, 8f, -8f, 0f);
            shake.setDuration(280);
            shake.start();

            currentBox.setBackgroundResource(R.drawable.letter_box_wrong);
            mainHandler.postDelayed(() -> {
                if (currentLetterIndex < letterBoxViews.size()) {
                    currentBox.setBackgroundResource(R.drawable.letter_box_active);
                }
            }, 300);

            char expUpper = Character.toUpperCase(expected);
            char keyUpper = Character.toUpperCase(key);
            txtSpellingHint.setText("Try again! Press " + expUpper);

            speak("That's " + keyUpper + ". Try pressing " + expUpper + "!");
        }
    }

    private void handleWordCompleted() {
        correctAnswers++;
        wordsCompletedInSession++;
        savePreferences();
        updateScoreDisplay();

        txtSpellingHint.setText("🎉 EXCELLENT! You spelled " + currentWord.toUpperCase() + "! 🎉");

        // Flash all boxes
        for (TextView box : letterBoxViews) {
            AnimatorSet cheer = new AnimatorSet();
            ObjectAnimator bX = ObjectAnimator.ofFloat(box, "scaleX", 1f, 1.18f, 1f);
            ObjectAnimator bY = ObjectAnimator.ofFloat(box, "scaleY", 1f, 1.18f, 1f);
            cheer.playTogether(bX, bY);
            cheer.setDuration(300);
            cheer.start();
        }

        speak("Awesome job " + childName + "! You spelled " + currentWord + "!");

        // Level up check
        if (wordsCompletedInSession >= 6 && currentLevel == SpellingLevel.LEVEL_1) {
            currentLevel = SpellingLevel.LEVEL_2;
            wordsCompletedInSession = 0;
            savePreferences();
            mainHandler.postDelayed(() -> {
                speak("You're doing amazing, " + childName + "! Moving to Level 2 with bigger words!");
                mainHandler.postDelayed(this::nextSpellingWord, 2000);
            }, 1800);
            return;
        } else if (wordsCompletedInSession >= 8 && currentLevel == SpellingLevel.LEVEL_2) {
            currentLevel = SpellingLevel.LEVEL_3;
            wordsCompletedInSession = 0;
            savePreferences();
            mainHandler.postDelayed(() -> {
                speak("SUPERSTAR! You reached Level 3, " + childName + "! Let's spell like a champ!");
                mainHandler.postDelayed(this::nextSpellingWord, 2200);
            }, 1800);
            return;
        }

        mainHandler.postDelayed(this::nextSpellingWord, 1800);
    }

    private void repeatCurrentWord() {
        if (currentWord.isEmpty()) return;
        StringBuilder spelled = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            spelled.append(currentWord.charAt(i));
            if (i < currentWord.length() - 1) spelled.append(" - ");
        }
        speak("The word is " + currentWord + ". Spelled: " + spelled + "!");
    }

    // ── Input & Key Processing ────────────────────────────────────────────────

    private void handleKeyPress(char key) {
        if (key == '\b') {
            // Backspace handling in spelling mode
            if (currentMode == GameMode.SPELLING_BEE && currentLetterIndex > 0) {
                currentLetterIndex--;
                TextView prevBox = letterBoxViews.get(currentLetterIndex);
                prevBox.setText("?");
                prevBox.setTextColor(0xFF854D0E);
                prevBox.setBackgroundResource(R.drawable.letter_box_active);

                if (currentLetterIndex + 1 < letterBoxViews.size()) {
                    TextView oldBox = letterBoxViews.get(currentLetterIndex + 1);
                    oldBox.setText("_");
                    oldBox.setTextColor(0xFF64748B);
                    oldBox.setBackgroundResource(R.drawable.letter_box_default);
                }
            }
            return;
        }

        switch (currentMode) {
            case LEARN_BY_LISTENING:
                handleLBLKey(key);
                break;
            case SPELLING_BEE:
                handleSpellingKey(key);
                break;
            case NAME_ENTRY:
                // Typing directly into name input handled by EditText
                break;
            case MENU:
                break;
        }
    }

    // ── Hardware Keyboard Event Handling ─────────────────────────────────────

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // External keyboard letters A-Z
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            char letter = (char) ('a' + (keyCode - KeyEvent.KEYCODE_A));
            // Visually highlight on-screen keyboard key
            if (onScreenKeyboard != null) {
                onScreenKeyboard.highlightKey(letter);
            }
            handleKeyPress(letter);
            return true;
        }

        // Hardware Backspace key
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (onScreenKeyboard != null) {
                onScreenKeyboard.highlightKey('\b');
            }
            handleKeyPress('\b');
            return true;
        }

        // Hardware Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentMode != GameMode.MENU && currentMode != GameMode.NAME_ENTRY) {
                showMainMenu();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    // ── Speech & Talking Mascot ───────────────────────────────────────────────

    private static int utteranceCounter = 0;

    private int estimateSyllables(String text) {
        if (text == null || text.trim().isEmpty()) return 1;
        String clean = text.trim();
        // If single letter or very short single word (e.g. "A", "C", "no", "cat", "dog")
        if (clean.length() <= 3 && !clean.contains(" ")) {
            return 1;
        }

        // Count words
        String[] words = clean.split("\\s+");
        if (words.length == 1) {
            int vowels = 0;
            String lower = clean.toLowerCase();
            for (int i = 0; i < lower.length(); i++) {
                char ch = lower.charAt(i);
                if ("aeiouy".indexOf(ch) >= 0) vowels++;
            }
            return Math.max(1, vowels);
        }

        // For sentences, estimate ~1.2 syllables per word
        return Math.max(1, (int) Math.round(words.length * 1.2));
    }

    private void speak(String text) {
        if (text == null || text.trim().isEmpty()) return;

        // 1. Show text in speech bubble pointing to Capybara
        if (speechBubble != null) {
            speechBubble.showText(text);
        }

        // 2. Determine syllables to match mouth animation precisely
        int syllables = estimateSyllables(text);

        if (capsView != null) {
            if (stopTalkingRunnable != null) {
                mainHandler.removeCallbacks(stopTalkingRunnable);
                stopTalkingRunnable = null;
            }

            if (syllables <= 2) {
                // Short utterance (1-2 syllables, e.g. "A", "Cat"):
                // Mouth opens and closes exact number of times, then rests
                capsView.startTalking(syllables);
            } else {
                // Multi-word sentence: natural continuous cadence with safety timeout
                capsView.startTalkingContinuous();
                long speechDuration = Math.min(5500L, Math.max(600L, syllables * 230L));
                stopTalkingRunnable = () -> {
                    if (capsView != null) capsView.stopTalking();
                };
                mainHandler.postDelayed(stopTalkingRunnable, speechDuration);
            }
        }

        // 3. Audio TTS speech
        if (tts != null) {
            String uid = "caps_" + (utteranceCounter++);
            Bundle params = new Bundle();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, uid);
        }
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onDestroy() {
        if (stopTalkingRunnable != null) {
            mainHandler.removeCallbacks(stopTalkingRunnable);
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}