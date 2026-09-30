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

### 🐝 Spelling Bee Mode
- **Multi-Level Progressive Learning**:
  - **Level 1 (Easy)**: 2–3 letter words (*cat, dog, sun, hat, bee*).
  - **Level 2 (Medium)**: 4-letter words (*frog, duck, star, lion, book*).
  - **Level 3 (Advanced)**: 5+ letter words (*apple, tiger, puppy, smile, happy*).
- **Interactive Letter Boxes**: Target words displayed as visual slots (`[ ? ] [ _ ] [ _ ]`). Active letters pulse yellow, correct entries flash emerald green, and wrong entries shake red.
- **Instant Encouragement**: Friendly voice hints (*"That's X! Try pressing C!"*) keep children motivated without frustration.
- **Audio Replay**: One-tap `🔊 Hear Word` button repeats word pronunciation and spelling aloud.
- **Victory Celebrations**: Celebratory star bursts and congratulatory messages after completing words.

### 🦫 Animated Mascot — Caps the Capybara
- **Syllable-Accurate Speech**: Caps's mouth opens and closes naturally in exact sync with word syllables (single-syllable words open and close once smoothly; multi-word sentences animate at conversational cadence).
- **Angled Presentation**: Positioned on the left and angled 3/4 right toward the speech bubble and board.
- **Speech Bubble Connection**: Dynamic speech bubble with a left-facing pointer tail connecting directly to Caps's mouth.
- **Lifelike Character**: Organic eye blinks every ~3.5 seconds, gentle breathing idle cycle, and signature yuzu/orange fruit on its head.

### ⌨️ Dual Keyboard Input
- **On-Screen Touch Keyboard**: Vibrantly colored, 3-row QWERTY keyboard with tactile touch-press animation.
- **Physical / External Keyboard Integration**: Plug in any USB or Bluetooth keyboard. Typing physical keys instantly triggers a gold highlight glow (`#FACC15`) and scale bounce on the corresponding on-screen key.
- **Backspace & Delete Support**: `⌫ DEL` key on-screen and hardware Backspace key support undoing spelling input.

### 👤 Personalization & Progress Tracking
- **First-Launch Onboarding**: Asks for the child's name with an intuitive, friendly entry modal and "LET'S PLAY! 🎉" button.
- **Personalized Addressing**: Caps addresses the child by name throughout speech bubbles, voice greetings, and praise.
- **One-Tap Profile Editing**: Tapping the top-bar player badge (`👤 [Name] ✏️`) allows switching or updating the player name anytime.
- **Data Persistence**: Child name, levels, correct answers, and missed attempts are safely saved across sessions via `SharedPreferences`.

---

## 📱 App Architecture & Structure

```
Caps-App-Final/
├── Caps-Updated.apk                 # Latest compiled production debug APK
├── README.md                        # Documentation and guide
├── build_and_setup.sh               # Automated Android SDK setup and build script
└── easton-learning-app/
    ├── app/
    │   ├── build.gradle             # Application dependencies and SDK configuration
    │   └── src/main/
    │       ├── AndroidManifest.xml  # Manifest with landscape & fullscreen configuration
    │       ├── java/com/eastonlearning/
    │       │   ├── MainActivity.java      # Game state manager, TTS, scoring & navigation
    │       │   ├── CapybaraView.java      # Custom animated mascot with syllable timing
    │       │   ├── SpeechBubbleView.java  # Custom speech bubble with left pointer tail
    │       │   └── OnScreenKeyboard.java  # Touch & external keyboard highlight controller
    │       └── res/
    │           ├── drawable/        # Tactile buttons, mode cards, letter boxes & themes
    │           ├── layout/          # Landscape responsive activity layout
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

# 1. Start emulator (if not already running)
$ANDROID_HOME/emulator/emulator -avd CapsTestAVD -no-audio -no-boot-anim -gpu swiftshader_indirect &

# 2. Wait for device and install updated APK
adb wait-for-device
adb install -r Caps-Updated.apk

# 3. Launch the application
adb shell am start -n com.eastonlearning/.MainActivity
```

---

## 🏷️ Tags & Metadata

`#Android` `#EducationalApp` `#KidsLearning` `#SpellingBee` `#Phonics` `#Capybara` `#CustomViews` `#TextToSpeech` `#KidFriendly` `#Java` `#AndroidDev`
