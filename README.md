# 🦫 Caps App — Interactive Learning & Spelling Adventure

[![Platform](https://img.shields.io/badge/Platform-Android%205.0%2B%20(API%2022--33)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?logo=gradle&logoColor=white)](https://gradle.org)
[![Java](https://img.shields.io/badge/Language-Java%2017%20%2F%201.8-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org)
[![Orientation](https://img.shields.io/badge/Orientation-Landscape%20Optimized-blue)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An engaging, kid-friendly Android educational app featuring **Caps the Capybara** — designed to teach letter identification, phonics, and spelling skills through multi-sensory interactive gameplay.

---

## 🌟 Key Features

### 🔤 Learn By Listening Mode
- **Phonics & Letter Exploration**: Touch on-screen keys or type on a keyboard to hear letter names and phonetic associations (*"A is for Apple 🍎"*, *"B is for Bear 🐻"*).
- **Interactive Card Bounces**: Animated visual letter display with lively bounce feedback upon each keypress.
- **Exploration Counter**: Live top-bar tracking of letters explored during the session.

### 🐝 Spelling Bee Mode (1-Letter to 3-Syllable Words)
- **Multi-Level Progressive Vocabulary**:
  - **Level 1 (1 Syllable, 1–3 Letters)**: Early sight words & phonics CVC words (*a, i, go, no, up, cat, dog, sun, hat, pig, fox, bed, cup, car, bus, run, hop, red, big, fun, bee, bat, cow, box, toy, pen, jam, pie, hen, ant, egg, nut, bag, web, map, rug, mud, tub*).
  - **Level 2 (1–2 Syllables, 4–5 Letters)**: Consonant blends, digraphs, and sweet everyday words (*frog, duck, bear, fish, star, tree, bird, lion, cake, boat, moon, book, ball, door, milk, nest, park, ring, shoe, wind, leaf, jump, king, play, rain, baby, water, apple, puppy, kitty, happy, panda, tiger, zebra, pizza, bunny, candy, cookie, robot, picnic, monkey, yellow, purple, orange, flower, garden, castle, rocket*).
  - **Level 3 (2–3 Syllables, 5–10 Letters)**: Challenge words for developing readers (*rainbow, sunshine, dolphin, feather, window, blanket, butterfly, banana, elephant, dinosaur, umbrella, sunflower, caterpillar, hospital, computer, adventure, together, wonderful, capybara, fantastic, alphabet, pineapple, watermelon, chocolate, instrument, tomorrow, beautiful, lemonade, basketball, marshmallow, octopus, helicopter, astronaut, telescope*).
- **Interactive Top-Bar Level Selector**: Tapping the top-bar level badge (`🎯 Lvl 1 (1-Syl) ▾`) lets parents and learners switch levels on the fly.
- **Interactive Letter Boxes**: Target words displayed as visual slots (`[ ? ] [ _ ] [ _ ]`). Active letters pulse yellow, correct entries flash emerald green, and wrong entries shake red.
- **Instant Encouragement**: Friendly voice hints (*"That's X! Try pressing C!"*) keep children motivated without frustration.
- **Audio Replay**: One-tap `🔊 Hear Word` button repeats word pronunciation and spelling aloud.
- **Victory Celebrations**: Celebratory star bursts and congratulatory messages after completing words.

### 🔊 Dual Audio System & Custom Voice Support
- **Custom Audio Loader**: The app automatically checks `app/src/main/res/raw/` for custom audio files (e.g. `letter_a.mp3`, `word_cat.mp3`, `cheer_awesome.mp3`). If present, it plays them via `MediaPlayer` with high fidelity!
- **Automatic TTS Fallback**: If custom audio recordings are not yet installed, the app falls back seamlessly to Android `TextToSpeech` with kid-friendly pitch and speech rate.
- **Master Voice Script**: See [`CAPS_VOICE_SCRIPTS.md`](file:///home/s8n/Caps-App-Final/CAPS_VOICE_SCRIPTS.md) for the complete list of all 160+ vocabulary words, letter sounds, celebration lines, and mistake support scripts ready for voice generation (ElevenLabs, OpenAI TTS, or custom recording).

### 🦫 Animated Mascot — Caps the Capybara
- **5-State Behavioral Animation System**:
  - `IDLE`: Organic gentle breathing, lateral weight sway, relaxed clasped hands, and natural eye blinks every ~3.5 seconds (`loop: true`).
  - `SPEAKING`: Expressive hand gestures, active syllable sync, and dynamic viseme cycling (`interruptible: true`).
  - `PRAISE / SUCCESS`: Clapping/rubbing hands gesture (inspired by sample 3D animation clip), enthusiastic body bounces, and joyful wide smile (`loop: false`, automatic return to `IDLE`).
  - `QUESTION / HINT`: Inquisitive head tilt, raised eyebrow, and one hand up/pointing gesture (`loop: false`, automatic return to `IDLE`).
  - `GENTLE_GUIDANCE`: Reassuring open palms, comforting slight tilt, and sympathetic smile when children make mistakes or need encouragement (`loop: false`, automatic return to `IDLE`).
- **6-Viseme Phoneme Synchronization**:
  1. `REST`: Neutral closed lips (M, B, P sounds)
  2. `WIDE_OPEN`: Low jaw drop for broad vowels (Ah, A, O)
  3. `ROUND_PUCKERED`: Tight rounded lips (Oh/Oo, U, W)
  4. `WIDE_SMILE`: Stretched smiling lips (Ee, E, I, S, Z)
  5. `TEETH_ON_LIP`: Upper teeth contacting lower lip (F, V)
  6. `TONGUE_ALVEOLAR`: Tongue tip raised towards upper palate (L, T, D, N)
- **Front-Facing Character Styling**: Matches the 3D animation reference with rounded torso, light tummy patch, prominent snout, dark hands/feet, and signature orange yuzu fruit.
- **Speech Bubble Connection**: Dynamic speech bubble with left pointer tail connecting directly to Caps's mouth.
- **3D Pipeline & Assets**: Includes [`caps_animation_controller.json`](file:///home/s8n/Caps-App-Final/easton-learning-app/app/src/main/assets/caps_animation_controller.json) and Blender export script [`export_caps_animations.py`](file:///home/s8n/Caps-App-Final/export_caps_animations.py).

### ⌨️ Dual Keyboard Input
- **On-Screen Touch Keyboard**: Vibrantly colored, 3-row QWERTY keyboard with tactile touch-press animation.
- **Physical / External Keyboard Integration**: Plug in any USB or Bluetooth keyboard. Typing physical keys instantly triggers a gold highlight glow (`#FACC15`) and scale bounce on the corresponding on-screen key.
- **Backspace & Delete Support**: `⌫ DEL` key on-screen and hardware Backspace key support undoing spelling input.

### 👤 Personalization & Progress Tracking
- **First-Launch Onboarding**: Asks for the child's name with an intuitive, friendly entry modal and "LET'S PLAY! 🎉" button.
- **Personalized Addressing**: Caps addresses the child by name throughout speech bubbles, voice greetings, and praise.
- **One-Tap Profile Editing**: Tapping the top-bar player badge (`👤 [Name] ✏️`) allows switching or updating the player name anytime.
- **Data Persistence**: Child name, current level, level completion counters, correct answers, and missed attempts are safely saved across sessions via `SharedPreferences`.

### 🔒 Parent & Educator Settings (Protected by Parental Gate)
- **Math Verification Gate**: To comply with Google Play Families and COPPA regulations, accessing settings requires solving a dynamic math challenge that toddlers cannot bypass.
- **Sound & Quiet Mode**: Instant toggle to mute all audio while keeping speech bubbles and cartoon mascot gestures active for quiet environments (car rides, libraries, bedtime).
- **Learning Stats Reset**: Grown-ups can reset stars and completion counts whenever starting fresh with a new student.
- **Optional Cloud TTS Integration**: Field for optional KittenML API key for dynamic sentence synthesis, while all 120 words and 26 letters play built-in studio audio without requiring an internet connection.

### 🛡️ Store Compliance & High-Performance Engineering
- **Target SDK 34 (Android 14)**: Meets current Google Play & Amazon Appstore release requirements with backward compatibility to API 22+.
- **Zero Dangerous Permissions**: Removed all microphone/audio recording permissions to guarantee COPPA safety and zero data collection.
- **Immersive Sticky Fullscreen**: Keeps system navigation bars hidden so children cannot accidentally exit the app during gameplay.
- **Zero-Allocation Canvas Pipeline**: Mascot and speech bubble custom views pre-allocate all `Paint`, `Path`, and `RectF` structures, preventing GC stutter and frame drops.
- **Managed Lifecycle**: Automatically pauses animation loops and releases audio focus when the app is placed in the background.

---

## 📱 App Architecture & Structure

```
Caps-App-Final/
├── CAPS_VOICE_SCRIPTS.md            # Complete voice lines, words, letters & phrases
├── Caps-Updated.apk                 # Latest compiled release debug APK
├── README.md                        # Documentation and guide
├── build_and_setup.sh               # Automated Android SDK setup and build script
└── easton-learning-app/
    ├── app/
    │   ├── build.gradle             # Application dependencies and SDK configuration
    │   └── src/main/
    │       ├── AndroidManifest.xml  # Manifest with landscape & fullscreen configuration
    │       ├── java/com/eastonlearning/
    │       │   ├── MainActivity.java      # Game state manager, Audio loader, scoring & navigation
    │       │   ├── CapybaraView.java      # Custom animated mascot with syllable timing
    │       │   ├── SpeechBubbleView.java  # Custom speech bubble with left pointer tail
    │       │   └── OnScreenKeyboard.java  # Touch & external keyboard highlight controller
    │       └── res/
    │           ├── drawable/        # Tactile buttons, mode cards, letter boxes & themes
    │           ├── layout/          # Landscape responsive activity layout
    │           ├── raw/             # Target directory for custom .mp3 voice clips
    │           └── values/          # Color schemes and app tokens
    ├── build.gradle                 # Project-level Gradle build configuration
    ├── gradle.properties            # JVM & AndroidX build settings
    └── settings.gradle              # Module settings
```

---

## 🛠️ Build & Installation

### Prerequisites
- **JDK**: Java 17 (`openjdk-17-jdk`)
- **Android SDK**: Build Tools 33.0.2, Platform 33 (`android-33`)
- **Gradle**: 7.5 (bundled via `./gradlew`)

### Build from Source
```bash
cd easton-learning-app
export ANDROID_HOME="$HOME/Android/Sdk"
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"

chmod +x gradlew
./gradlew assembleDebug
```
The output APK will be generated at:
`easton-learning-app/app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Testing & Running on Emulator / Device

To run the app on an Android emulator or connected device:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools"

# 1. Start emulator with audio enabled (do NOT use -no-audio)
$ANDROID_HOME/emulator/emulator -avd CapsTestAVD -no-boot-anim -gpu swiftshader_indirect &

# 2. Wait for device and install updated APK
adb wait-for-device
adb install -r Caps-Updated.apk

# 3. Launch the application
adb shell am start -n com.eastonlearning/.MainActivity
```

> **Note on Emulator Sound**:
> When launching the emulator, omit `-no-audio` so that host audio output is enabled. Real Android phones and tablets have Google TTS and system audio pre-configured out of the box. Adding custom `.mp3` audio files to `app/src/main/res/raw/` ensures 100% consistent audio on all hardware.

---

## 🏷️ Tags & Metadata

`#Android` `#EducationalApp` `#KidsLearning` `#SpellingBee` `#Phonics` `#Capybara` `#CustomViews` `#VoiceAudio` `#KidFriendly` `#Java` `#AndroidDev`
