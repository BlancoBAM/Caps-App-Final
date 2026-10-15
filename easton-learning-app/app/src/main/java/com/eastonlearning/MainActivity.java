package com.eastonlearning;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    private MediaPlayer mediaPlayer;
    private SharedPreferences prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable stopTalkingRunnable;

    // Audio focus & haptics
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private Vibrator vibrator;

    // Headphone state
    private boolean headphonesWarningShown = false;
    private final BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", -1);
            if (state == 0) {
                // Headphones unplugged — remind Easton
                mainHandler.post(() ->
                    playSoundOrSpeak("headphone_reminder",
                        "Heads up! I can hear that your headphones were disconnected. Make sure you can still hear me!"));
            }
        }
    };

    // Child name (persisted in SharedPreferences)
    private String childName = "Friend";
    private boolean isFirstLaunch = true;

    // Game Modes & Levels
    public enum GameMode { NAME_ENTRY, MENU, LEARN_BY_LISTENING, SPELLING_BEE }
    public enum SpellingLevel { LEVEL_1, LEVEL_2, LEVEL_3 }

    private GameMode currentMode = GameMode.MENU;
    private SpellingLevel currentLevel = SpellingLevel.LEVEL_1;

    // Game stats (all persisted)
    private int correctAnswers = 0;
    private int missedAnswers = 0;
    private int lettersExplored = 0;
    private int wordsCompletedInSession = 0;
    private int level1Completed = 0;
    private int level2Completed = 0;
    private int level3Completed = 0;

    // Spelling state
    private String currentWord = "";
    private int currentLetterIndex = 0;
    private int wordMistakes = 0;
    private String kittenApiKey = "";
    private final List<TextView> letterBoxViews = new ArrayList<>();
    private final Random random = new Random();

    // UI Elements
    private TextView txtPlayer, txtStats, txtLevelBadge, btnSettings;
    private Button btnMenu;
    private CapybaraView capsView;
    private SpeechBubbleView speechBubble;
    private OnScreenKeyboard onScreenKeyboard;
    private boolean soundMuted = false;

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
    private Button btnSkipWord;
    private Button btnHintWord;

    // Hint state
    private int hintStep = 0;
    private Runnable hintRunnable = null;

    // ── Word Dictionaries (Expanded: 1-Letter to 3-Syllable Words) ───────────

    // Level 1: 1-Syllable Early Readers (1–3 letters)
    private final String[] level1Words = {
            "a", "i", "go", "no", "up", "me", "we", "he", "be", "to",
            "do", "it", "at", "on", "in", "is", "am", "or", "so", "my",
            "by", "cat", "dog", "sun", "hat", "pig", "fox", "bed", "cup",
            "car", "bus", "run", "hop", "red", "big", "fun", "bee", "bat",
            "cow", "box", "toy", "pen", "ice", "jam", "pie", "hen", "ant",
            "owl", "egg", "nut", "bag", "web", "map", "sit", "hit", "pot",
            "top", "van", "rug", "bug", "log", "mud", "net", "lip", "tub"
    };

    // Level 2: 1-to-2 Syllable Everyday Words (4–5 letters)
    private final String[] level2Words = {
            "frog", "duck", "bear", "fish", "star", "tree", "bird", "lion",
            "cake", "boat", "moon", "book", "ball", "door", "lamp", "milk",
            "nest", "park", "ring", "shoe", "wind", "leaf", "ship", "bell",
            "drum", "flag", "hand", "jump", "king", "play", "rain", "snow",
            "swim", "kite", "baby", "water", "apple", "puppy", "kitty", "happy",
            "panda", "tiger", "zebra", "pizza", "bunny", "sunny", "candy",
            "cookie", "funny", "magic", "music", "robot", "cloudy", "picnic",
            "monkey", "pencil", "pocket", "yellow", "purple", "orange", "flower",
            "garden", "castle", "rocket"
    };

    // Level 3: 2-to-3 Syllable Challenge Words (5–10 letters)
    private final String[] level3Words = {
            "rainbow", "sunshine", "dolphin", "feather", "window", "blanket",
            "butterfly", "banana", "elephant", "dinosaur", "umbrella", "sunflower",
            "caterpillar", "hospital", "computer", "adventure", "together",
            "wonderful", "capybara", "fantastic", "alphabet", "pineapple",
            "watermelon", "chocolate", "instrument", "tomorrow", "beautiful",
            "lemonade", "basketball", "marshmallow", "octopus", "helicopter",
            "astronaut", "telescope"
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

        hideSystemUI();
        initializePreferences();
        initializeServices();
        initializeViews();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Immediately set the UI state on launch
        if (isFirstLaunch || TextUtils.isEmpty(childName) || "Friend".equals(childName)) {
            showNameEntry();
        } else {
            showMainMenu();
        }
    }

    private void initializeServices() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Register receiver for headphone connection/disconnection
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(headsetReceiver, filter);
        } catch (Exception ignored) {}

        // Initialize on-device KittenTTS neural speech engine
        KittenTTSClient.init(this);
    }

    private void initializePreferences() {
        prefs = getSharedPreferences("EastonLearning", MODE_PRIVATE);
        isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);
        childName = prefs.getString("childName", "");
        kittenApiKey = prefs.getString("kittenml_api_key", "");
        soundMuted = prefs.getBoolean("soundMuted", false);
        correctAnswers = prefs.getInt("correctAnswers", 0);
        missedAnswers = prefs.getInt("missedAnswers", 0);
        level1Completed = prefs.getInt("level1Completed", 0);
        level2Completed = prefs.getInt("level2Completed", 0);
        level3Completed = prefs.getInt("level3Completed", 0);

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
                .putBoolean("soundMuted", soundMuted)
                .putString("kittenml_api_key", kittenApiKey)
                .putInt("correctAnswers", correctAnswers)
                .putInt("missedAnswers", missedAnswers)
                .putInt("level1Completed", level1Completed)
                .putInt("level2Completed", level2Completed)
                .putInt("level3Completed", level3Completed)
                .putString("currentLevel", currentLevel.name())
                .apply();
    }

    private void initializeViews() {
        // Top Bar
        txtPlayer = findViewById(R.id.txtPlayer);
        txtStats = findViewById(R.id.txtStats);
        txtLevelBadge = findViewById(R.id.txtLevelBadge);
        btnMenu = findViewById(R.id.btnMenu);
        btnSettings = findViewById(R.id.btnSettings);

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
        btnSkipWord = findViewById(R.id.btnSkipWord);
        btnHintWord = findViewById(R.id.btnHintWord);

        // Click Listeners
        btnMenu.setOnClickListener(v -> showMainMenu());
        txtPlayer.setOnClickListener(v -> showNameEntry());
        txtLevelBadge.setOnClickListener(v -> cycleDifficultyLevel());
        btnSettings.setOnClickListener(v -> showParentalGate());
        btnLearnByListening.setOnClickListener(v -> startLearnByListening());
        btnSpellingBee.setOnClickListener(v -> startSpellingBee());
        btnRepeatWord.setOnClickListener(v -> repeatCurrentWord());
        btnSkipWord.setOnClickListener(v -> skipCurrentWord());
        btnHintWord.setOnClickListener(v -> showHint());

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

    private void cycleDifficultyLevel() {
        if (currentLevel == SpellingLevel.LEVEL_1) {
            currentLevel = SpellingLevel.LEVEL_2;
        } else if (currentLevel == SpellingLevel.LEVEL_2) {
            currentLevel = SpellingLevel.LEVEL_3;
        } else {
            currentLevel = SpellingLevel.LEVEL_1;
        }
        savePreferences();
        updateScoreDisplay();

        String levelDesc = getLevelDescription(currentLevel);
        playSoundOrSpeak("nav_level_switch", "Level switched to " + levelDesc + "!");

        if (currentMode == GameMode.SPELLING_BEE) {
            nextSpellingWord();
        }
    }

    private String getLevelDescription(SpellingLevel level) {
        switch (level) {
            case LEVEL_2: return "Level 2: 1 to 2 Syllables";
            case LEVEL_3: return "Level 3: 2 to 3 Syllables";
            case LEVEL_1:
            default:      return "Level 1: 1 Syllable";
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
            txtLevelBadge.setVisibility(View.VISIBLE);
            String lvlShort = (currentLevel == SpellingLevel.LEVEL_1) ? "🎯 Lvl 1 (1-Syl) ▾" :
                    (currentLevel == SpellingLevel.LEVEL_2 ? "🎯 Lvl 2 (1-2 Syl) ▾" : "🎯 Lvl 3 (2-3 Syl) ▾");
            txtLevelBadge.setText(lvlShort);
            txtStats.setText("✅ " + correctAnswers + "  ❌ " + missedAnswers);
        } else if (currentMode == GameMode.LEARN_BY_LISTENING) {
            txtLevelBadge.setVisibility(View.GONE);
            txtStats.setText("🔤 Letters Explored: " + lettersExplored);
        } else {
            txtLevelBadge.setVisibility(View.GONE);
            txtStats.setText("⭐ Welcome!");
        }
    }

    // ── Parent & Educator Gate / Settings ─────────────────────────────────────

    private void showParentalGate() {
        int num1 = random.nextInt(8) + 7; // 7..14
        int num2 = random.nextInt(7) + 5; // 5..11
        final int expectedAnswer = num1 + num2;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔒 Parents & Grown-Ups Only");
        builder.setMessage("Please solve this math question to enter settings:\n\nWhat is " + num1 + " + " + num2 + "?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Answer");
        input.setGravity(Gravity.CENTER);

        int pad = dpToPx(20);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, pad / 2, pad, 0);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Enter", (dialog, which) -> {
            String val = input.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    int ans = Integer.parseInt(val);
                    if (ans == expectedAnswer) {
                        showParentSettingsDialog();
                        return;
                    }
                } catch (NumberFormatException ignored) {}
            }
            Toast.makeText(this, "Incorrect answer. Grown-up verification required.", Toast.LENGTH_SHORT).show();
            hideSystemUI();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            hideSystemUI();
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> hideSystemUI());
        dialog.show();
    }

    private void showParentSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ Parent & Educator Settings");

        int pad = dpToPx(20);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad, pad, pad);

        // Child name section
        TextView lblName = new TextView(this);
        lblName.setText("Child's Name:");
        lblName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        lblName.setTypeface(null, Typeface.BOLD);
        layout.addView(lblName);

        final EditText edtName = new EditText(this);
        edtName.setText(childName);
        edtName.setHint("Enter student name");
        layout.addView(edtName);

        // Sound mute toggle
        final CheckBox chkMute = new CheckBox(this);
        chkMute.setText("Mute All Audio / Quiet Mode");
        chkMute.setChecked(soundMuted);
        chkMute.setPadding(0, dpToPx(8), 0, dpToPx(8));
        layout.addView(chkMute);

        // KittenTTS API Key (Optional)
        TextView lblApi = new TextView(this);
        lblApi.setText("Optional KittenML TTS API Key:");
        lblApi.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        lblApi.setTypeface(null, Typeface.BOLD);
        lblApi.setPadding(0, dpToPx(12), 0, 0);
        layout.addView(lblApi);

        TextView lblApiSub = new TextView(this);
        lblApiSub.setText("All 120 words and 26 letters have built-in audio. An API key is optional for custom dynamic sentences.");
        lblApiSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        lblApiSub.setTextColor(Color.GRAY);
        layout.addView(lblApiSub);

        final EditText edtApi = new EditText(this);
        edtApi.setText(kittenApiKey);
        edtApi.setHint("Leave blank for built-in audio");
        layout.addView(edtApi);

        // Reset progress section
        Button btnResetProgress = new Button(this);
        btnResetProgress.setText("Reset Stars & Learning Stats");
        btnResetProgress.setTextColor(Color.parseColor("#DC2626"));
        btnResetProgress.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset All Progress?")
                    .setMessage("Are you sure you want to reset all earned stars, completed words, and accuracy stats to zero?")
                    .setPositiveButton("Reset", (d, w) -> {
                        correctAnswers = 0;
                        missedAnswers = 0;
                        lettersExplored = 0;
                        wordsCompletedInSession = 0;
                        level1Completed = 0;
                        level2Completed = 0;
                        level3Completed = 0;
                        savePreferences();
                        updateScoreDisplay();
                        Toast.makeText(this, "Progress has been reset.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        layout.addView(btnResetProgress);

        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Save Settings", (dialog, which) -> {
            String newName = edtName.getText().toString().trim();
            if (!newName.isEmpty()) {
                childName = newName.substring(0, 1).toUpperCase() + newName.substring(1).toLowerCase();
            }
            soundMuted = chkMute.isChecked();
            kittenApiKey = edtApi.getText().toString().trim();

            savePreferences();
            updatePlayerHeader();
            updateScoreDisplay();
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
            hideSystemUI();
        });

        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
            hideSystemUI();
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> hideSystemUI());
        dialog.show();
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
        playSoundOrSpeak("ask_name", msg);
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
        playSoundOrSpeak("welcome_confirmed", welcome);

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
        playSoundOrSpeak("menu_greeting", greeting);
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
        playSoundOrSpeak("lbl_intro", prompt);
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

        // Haptic feedback for tactile satisfaction (Suggestion 6)
        triggerHaptic(true);

        // Animate the letter display card
        AnimatorSet anim = new AnimatorSet();
        ObjectAnimator sX = ObjectAnimator.ofFloat(txtBigLetter, "scaleX", 1f, 1.35f, 1f);
        ObjectAnimator sY = ObjectAnimator.ofFloat(txtBigLetter, "scaleY", 1f, 1.35f, 1f);
        anim.playTogether(sX, sY);
        anim.setDuration(240);
        anim.start();

        if (capsView != null) {
            capsView.speakLetter(upper);
        }

        playSoundOrSpeak("phonics_" + Character.toLowerCase(upper), spokenPhrase);
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
        cancelHint();
        wordMistakes = 0;
        currentWord = pickWordForLevel();
        currentLetterIndex = 0;

        buildLetterBoxes(currentWord);

        txtSpellingHint.setText("Spell: " + currentWord.toUpperCase());
        playWordIntroSequence(currentWord);
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

        // Dynamically adjust box size based on word length so 8+ letter words fit comfortably
        int boxSize = dpToPx(word.length() > 6 ? 36 : 44);
        int margin = dpToPx(word.length() > 6 ? 3 : 5);
        int fontSize = word.length() > 6 ? 18 : 22;

        for (int i = 0; i < word.length(); i++) {
            TextView box = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(boxSize, boxSize);
            lp.setMargins(margin, 0, margin, 0);
            box.setLayoutParams(lp);
            box.setGravity(Gravity.CENTER);
            box.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
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
            // Correct letter! (Suggestion 6: positive double-tap haptic)
            triggerHaptic(true);

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
                playSoundOrSpeak("letter_" + Character.toLowerCase(expected), String.valueOf(Character.toUpperCase(expected)));
            }

        } else {
            // Incorrect letter — gentle buzz
            triggerHaptic(false);

            if (capsView != null) {
                capsView.setState(CapybaraView.AnimState.GENTLE_GUIDANCE);
            }

            wordMistakes++;
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
            txtSpellingHint.setText("Try the letter " + expUpper + "!");

            // If child has missed multiple times on this word, speak soothing phrase;
            // otherwise speak the letter entered on keyboard.
            if (wordMistakes >= 3) {
                wordMistakes = 0;
                playSoundOrSpeak("soothing_misspelled", "That's okay! Take your time and try again!");
            } else {
                playSoundOrSpeak("letter_" + Character.toLowerCase(key), String.valueOf(Character.toUpperCase(key)));
            }
        }
    }

    private void handleWordCompleted() {
        if (capsView != null) {
            capsView.setState(CapybaraView.AnimState.PRAISE);
        }

        correctAnswers++;
        wordsCompletedInSession++;

        if (currentLevel == SpellingLevel.LEVEL_1) {
            level1Completed++;
        } else if (currentLevel == SpellingLevel.LEVEL_2) {
            level2Completed++;
        } else {
            level3Completed++;
        }
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

        String[] cheerOptions = {"cheer_awesome", "cheer_superstar", "cheer_fantastic", "cheer_brilliant"};
        String cheerRes = cheerOptions[random.nextInt(cheerOptions.length)];
        playSoundOrSpeak(cheerRes, "Awesome job " + childName + "! You spelled " + currentWord + "!");

        // Level up check
        if (wordsCompletedInSession >= 6 && currentLevel == SpellingLevel.LEVEL_1) {
            currentLevel = SpellingLevel.LEVEL_2;
            wordsCompletedInSession = 0;
            savePreferences();
            updateScoreDisplay();
            mainHandler.postDelayed(() -> {
                playSoundOrSpeak("level_up_2", "You're doing amazing, " + childName + "! Moving to Level 2 with bigger words!");
                mainHandler.postDelayed(this::nextSpellingWord, 2000);
            }, 1800);
            return;
        } else if (wordsCompletedInSession >= 8 && currentLevel == SpellingLevel.LEVEL_2) {
            currentLevel = SpellingLevel.LEVEL_3;
            wordsCompletedInSession = 0;
            savePreferences();
            updateScoreDisplay();
            mainHandler.postDelayed(() -> {
                playSoundOrSpeak("level_up_3", "SUPERSTAR! You reached Level 3, " + childName + "! Let's spell like a champ!");
                mainHandler.postDelayed(this::nextSpellingWord, 2200);
            }, 1800);
            return;
        }

        mainHandler.postDelayed(this::nextSpellingWord, 1800);
    }

    // ── Suggestion 11: Word Intro Audio Sequencing ───────────────────────────

    private void repeatCurrentWord() {
        if (currentWord.isEmpty()) return;
        playWordIntroSequence(currentWord);
    }

    /**
     * Plays the word sound (e.g. word_cat.mp3) followed by a short pause,
     * then spells it letter by letter with letter_X.mp3 for each,
     * ensuring the entire introduction uses only Caps' custom voice.
     */
    private void playWordIntroSequence(String word) {
        if (word == null || word.isEmpty()) return;

        // Visual presentation in bubble
        StringBuilder spelled = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            spelled.append(Character.toUpperCase(word.charAt(i)));
            if (i < word.length() - 1) spelled.append(" - ");
        }
        String prompt = "The word is " + word.toUpperCase() + "! " + spelled + ". Can you spell " + word.toUpperCase() + ", " + childName + "?";
        if (speechBubble != null) {
            speechBubble.showText(prompt);
        }

        // Animate Caps for the introduction: inquisitive/questioning state
        if (capsView != null) {
            capsView.setState(CapybaraView.AnimState.QUESTION);
            capsView.startTalkingContinuous();
        }

        // Step 1: Play word audio (word_<word>.mp3)
        playRawAudio("word_" + word.toLowerCase(), () -> {
            // Step 2: Spell out each letter sequentially with letter_X.mp3
            mainHandler.postDelayed(() -> spellWordSequentially(word, 0), 400);
        });
    }

    private void spellWordSequentially(String word, int index) {
        if (index >= word.length()) {
            if (stopTalkingRunnable != null) {
                mainHandler.removeCallbacks(stopTalkingRunnable);
                stopTalkingRunnable = null;
            }
            if (capsView != null) capsView.stopTalking();
            abandonAppAudioFocus();
            return;
        }

        char c = word.charAt(index);
        if (capsView != null) {
            capsView.speakLetter(c);
        }
        playRawAudio("letter_" + Character.toLowerCase(c), () -> {
            mainHandler.postDelayed(() -> spellWordSequentially(word, index + 1), 350);
        });
    }

    private void playRawAudio(String rawName, Runnable onFinished) {
        if (soundMuted) {
            if (onFinished != null) {
                mainHandler.postDelayed(onFinished, 300);
            }
            return;
        }
        requestAppAudioFocus();
        int resId = 0;
        if (rawName != null) {
            String clean = rawName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            resId = getResources().getIdentifier(clean, "raw", getPackageName());
        }

        if (resId != 0) {
            try {
                if (mediaPlayer != null) {
                    try { mediaPlayer.stop(); mediaPlayer.release(); } catch (Exception ignored) {}
                    mediaPlayer = null;
                }
                mediaPlayer = MediaPlayer.create(this, resId);
                if (mediaPlayer != null) {
                    mediaPlayer.setOnCompletionListener(mp -> {
                        if (onFinished != null) onFinished.run();
                    });
                    mediaPlayer.start();
                    return;
                }
            } catch (Exception ignored) {}
        }

        // If audio file doesn't exist, advance after brief pause
        if (onFinished != null) {
            mainHandler.postDelayed(onFinished, 400);
        }
    }

    private void skipCurrentWord() {
        if (currentWord.isEmpty()) return;
        cancelHint();
        txtSpellingHint.setText("No worries! Let's try another word!");
        playSoundOrSpeak("hint_general_retry", "No worries! Let's try another word!");
        mainHandler.postDelayed(this::nextSpellingWord, 1400);
    }

    /**
     * Hint: highlights each correct letter on the on-screen keyboard one by one,
     * at a pace kids can follow, so they can see exactly which keys to press.
     */
    private void showHint() {
        if (currentWord.isEmpty()) return;
        cancelHint();
        hintStep = currentLetterIndex; // start from the first un-typed letter
        txtSpellingHint.setText("Here's a hint! Watch the keys light up!");
        playSoundOrSpeak("hint_general_retry", "Here's a hint! Watch the keys!");
        scheduleHintStep();
    }

    private void scheduleHintStep() {
        if (hintStep >= currentWord.length()) return;
        hintRunnable = () -> {
            if (onScreenKeyboard != null && hintStep < currentWord.length()) {
                char hintLetter = currentWord.charAt(hintStep);
                onScreenKeyboard.highlightHintKey(hintLetter);
                hintStep++;
                if (hintStep < currentWord.length()) {
                    scheduleHintStep();
                }
            }
        };
        mainHandler.postDelayed(hintRunnable, 800);
    }

    private void cancelHint() {
        if (hintRunnable != null) {
            mainHandler.removeCallbacks(hintRunnable);
            hintRunnable = null;
        }
        hintStep = 0;
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

    // ── Unified Sound Player & Talking Mascot System ──────────────────────────

    private static int utteranceCounter = 0;

    private int estimateSyllables(String text) {
        if (text == null || text.trim().isEmpty()) return 1;
        String clean = text.trim();
        if (clean.length() <= 3 && !clean.contains(" ")) {
            return 1;
        }

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

        return Math.max(1, (int) Math.round(words.length * 1.2));
    }

    /**
     * Plays local audio file from res/raw if present (e.g. letter_a.mp3, word_cat.mp3),
     * or seamlessly falls back to TextToSpeech if custom audio is not yet recorded.
     */
    private void playSoundOrSpeak(String rawResourceName, String textFallback) {
        if (textFallback == null || textFallback.trim().isEmpty()) return;

        // Request audio focus so app audio ducks background sounds (only if unmuted)
        if (!soundMuted) {
            requestAppAudioFocus();
        }

        // 1. Show text in speech bubble
        if (speechBubble != null) {
            speechBubble.showText(textFallback);
        }

        // 2. Animate character and mouth matched to dialogue context & syllables
        int syllables = estimateSyllables(textFallback);
        if (capsView != null) {
            if (stopTalkingRunnable != null) {
                mainHandler.removeCallbacks(stopTalkingRunnable);
                stopTalkingRunnable = null;
            }

            // Map dialogue trigger to Caps animation state
            if (rawResourceName != null && (rawResourceName.startsWith("cheer_") || rawResourceName.startsWith("level_up_") || rawResourceName.startsWith("welcome_"))
                    || textFallback.contains("Awesome") || textFallback.contains("EXCELLENT") || textFallback.contains("SUPERSTAR")) {
                capsView.setState(CapybaraView.AnimState.PRAISE);
            } else if (rawResourceName != null && (rawResourceName.startsWith("soothing_") || rawResourceName.contains("retry"))
                    || textFallback.toLowerCase().contains("try again")) {
                capsView.setState(CapybaraView.AnimState.GENTLE_GUIDANCE);
            } else if (rawResourceName != null && (rawResourceName.startsWith("ask_") || rawResourceName.startsWith("hint_"))
                    || textFallback.contains("?")) {
                capsView.setState(CapybaraView.AnimState.QUESTION);
            } else if (capsView.getState() == CapybaraView.AnimState.IDLE) {
                capsView.setState(CapybaraView.AnimState.SPEAKING);
            }

            // If speaking an individual letter, set its specific viseme
            if (rawResourceName != null && (rawResourceName.startsWith("letter_") || rawResourceName.startsWith("phonics_"))) {
                char ch = rawResourceName.charAt(rawResourceName.length() - 1);
                capsView.setViseme(CapybaraView.getVisemeForChar(ch));
            }

            if (syllables <= 2) {
                capsView.startTalking(syllables);
            } else {
                capsView.startTalkingContinuous();
                long speechDuration = Math.min(5500L, Math.max(600L, syllables * 230L));
                stopTalkingRunnable = () -> {
                    if (capsView != null) capsView.stopTalking();
                };
                mainHandler.postDelayed(stopTalkingRunnable, speechDuration);
            }
        }

        // If sound is muted, visual bubble and gestures have been presented; skip audio playback
        if (soundMuted) {
            return;
        }

        // Check if text is personalized with the child's dynamic name
        boolean isDynamicPersonalized = !TextUtils.isEmpty(childName)
                && !"Friend".equalsIgnoreCase(childName)
                && textFallback.contains(childName);

        // 3. Try custom audio from res/raw if present (only for non-personalized static prompts)
        int resId = 0;
        if (!isDynamicPersonalized && rawResourceName != null) {
            String cleanName = rawResourceName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            resId = getResources().getIdentifier(cleanName, "raw", getPackageName());
            if (resId == 0 && cleanName.startsWith("phonics_")) {
                resId = getResources().getIdentifier("letter_" + cleanName.substring(8), "raw", getPackageName());
            }
            if (resId == 0 && cleanName.startsWith("spell_")) {
                resId = getResources().getIdentifier("word_" + cleanName.substring(6), "raw", getPackageName());
            }
        }

        if (resId != 0) {
            try {
                if (mediaPlayer != null) {
                    try { mediaPlayer.stop(); mediaPlayer.release(); } catch (Exception ignored) {}
                    mediaPlayer = null;
                }
                mediaPlayer = MediaPlayer.create(this, resId);
                if (mediaPlayer != null) {
                    mediaPlayer.setOnCompletionListener(mp -> {
                        abandonAppAudioFocus();
                        if (stopTalkingRunnable != null) {
                            mainHandler.removeCallbacks(stopTalkingRunnable);
                            stopTalkingRunnable = null;
                        }
                        if (capsView != null) capsView.stopTalking();
                    });
                    mediaPlayer.start();
                    return; // Successfully played custom audio asset!
                }
            } catch (Exception ignored) {}
        }

        // 4. On-device KittenTTS real-time neural speech synthesis (offline, instant Caps voice)
        KittenTTSClient.synthesizeSpeech(this, textFallback, new KittenTTSClient.SpeechCallback() {
            @Override
            public void onSuccess(File audioFile) {
                playAudioFile(audioFile);
            }

            @Override
            public void onFailure(Exception error) {
                abandonAppAudioFocus();
            }
        });
    }

    private void playAudioFile(File file) {
        try {
            if (mediaPlayer != null) {
                try { mediaPlayer.stop(); mediaPlayer.release(); } catch (Exception ignored) {}
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp -> {
                abandonAppAudioFocus();
                if (stopTalkingRunnable != null) {
                    mainHandler.removeCallbacks(stopTalkingRunnable);
                    stopTalkingRunnable = null;
                }
                if (capsView != null) capsView.stopTalking();
            });
            mediaPlayer.start();
        } catch (Exception e) {
            abandonAppAudioFocus();
        }
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    // ── Suggestion 2: Audio Focus Management ─────────────────────────────────

    private void requestAppAudioFocus() {
        if (audioManager == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest == null) {
                    AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build();
                    audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(focusChange -> {
                                // Handled automatically by system ducking
                            })
                            .build();
                }
                audioManager.requestAudioFocus(audioFocusRequest);
            } else {
                audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            }
        } catch (Exception ignored) {}
    }

    private void abandonAppAudioFocus() {
        if (audioManager == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(null);
            }
        } catch (Exception ignored) {}
    }

    // ── Suggestion 6: Tactile / Haptic Feedback ───────────────────────────────

    private void triggerHaptic(boolean correct) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (correct) {
                    // Quick, crisp double-tap pulse
                    long[] timings = {0, 25, 45, 25};
                    int[] amplitudes = {0, 180, 0, 230};
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
                } else {
                    // Gentle, friendly buzz for mistake
                    vibrator.vibrate(VibrationEffect.createOneShot(110, 120));
                }
            } else {
                if (correct) {
                    vibrator.vibrate(new long[]{0, 25, 45, 25}, -1);
                } else {
                    vibrator.vibrate(110);
                }
            }
        } catch (Exception ignored) {}
    }

    // ── Suggestion 7: Headphone Detection ─────────────────────────────────────

    private boolean areHeadphonesConnected() {
        if (audioManager == null) return false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                for (AudioDeviceInfo device : devices) {
                    int type = device.getType();
                    if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        type == AudioDeviceInfo.TYPE_USB_HEADSET) {
                        return true;
                    }
                }
                return false;
            } else {
                return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (capsView != null) {
            capsView.resumeAnimations();
        }
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try { mediaPlayer.pause(); } catch (Exception ignored) {}
        }
        abandonAppAudioFocus();
        if (stopTalkingRunnable != null) {
            mainHandler.removeCallbacks(stopTalkingRunnable);
            stopTalkingRunnable = null;
        }
        if (capsView != null) {
            capsView.pauseAnimations();
            capsView.stopTalking();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); mediaPlayer.release(); } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        super.onStop();
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } catch (Exception ignored) {}
        } else {
            try {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(headsetReceiver);
        } catch (Exception ignored) {}
        abandonAppAudioFocus();

        if (stopTalkingRunnable != null) {
            mainHandler.removeCallbacks(stopTalkingRunnable);
        }
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); mediaPlayer.release(); } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}