# Caps-App - Learn By Listening

A kid-friendly Android learning app featuring Caps the Capybara that teaches alphabet recognition and basic typing skills.

## Features Implemented

### Core Functionality
- **Learn By Listening Mode**: Interactive alphabet learning with voice feedback
- **Spelling Bee Mode**: Progressive word spelling with three difficulty levels
- **External Keyboard Support**: Works with both on-screen and physical keyboards
- **Text-to-Speech**: Kid-friendly voice with encouraging feedback

### Character & Design
- **Caps the Capybara**: Animated capybara mascot with gentle breathing animation
- **Speech Bubbles**: Animated speech bubbles with typing effect for subtitles
- **Colorful Interface**: Vibrant, kid-friendly color scheme optimized for young learners
- **Kid-Optimized Layout**: Large buttons, high contrast text, easy navigation

### Educational Features
- **Progressive Difficulty**: 
  - Level 1: 1-2 letter words (75% accuracy required)
  - Level 2: 3-4 letter words (85% accuracy required)
  - Level 3: Mixed difficulty with controlled 5-letter word introduction
- **Smart Word Selection**: Prevents consecutive difficult words, gradual complexity increase
- **Progress Tracking**: Saves learning progress across sessions
- **Encouraging Feedback**: Positive reinforcement without criticism

### Technical Features
- **First Launch Experience**: Proper introduction with headphone detection
- **Audio Optimization**: Detects headphones, recommends quiet environment
- **Level Advancement**: Animated button highlighting with voice guidance
- **Session Persistence**: Saves current level and progress between app launches

## App Structure
```
easton-learning-app/
├── app/src/main/java/com/eastonlearning/
│   ├── MainActivity.java - Main app logic and game modes
│   ├── OnScreenKeyboard.java - Colorful QWERTY keyboard
│   └── SpeechBubbleView.java - Animated speech bubble system
└── app/src/main/res/
    ├── drawable/ - UI elements and capybara character
    ├── layout/ - Main activity layout
    └── values/ - Colors and app configuration
```

## Usage Instructions

### First Launch
1. App introduces Caps the Capybara
2. Checks for headphones and recommends quiet environment
3. Starts with "Learn By Listening" mode as recommended

### Learn By Listening Mode
- Press any letter key (on-screen or external keyboard)
- Caps speaks the letter name
- Visual feedback shows encouragement
- Indefinite exploration mode

### Spelling Bee Mode
- Three progressive levels with accuracy requirements
- Words spoken first, then spelled letter by letter
- Child types each letter with real-time feedback
- Level advancement with animated choice prompts

### Navigation
- Back button returns to main menu
- External keyboard fully supported
- Touch-friendly for tablets
