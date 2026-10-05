#!/usr/bin/env python3
"""
Caps Audio Generator using KittenTTS-2 Voice Cloning
---------------------------------------------------
Reference audio: ~/Downloads/Untitled(1).mp3
Outputs to: easton-learning-app/app/src/main/res/raw/

Generates:
1. Alphabet Letters (letter_a.mp3 to letter_z.mp3) - clean letter names
2. Phonics Lines (phonics_a.mp3 to phonics_z.mp3) - letter + example word
3. UI, Welcome, & Headphone lines (welcome_first, welcome_back, headphone_reminder, etc.)
4. Celebrations (cheer_awesome, cheer_superstar, etc.)
5. Gentle hints (hint_general_retry, hint_a to hint_z)
"""

import os
import sys
import time
import numpy as np
import soxr
import soundfile as sf
from pathlib import Path
from kittenml import KittenTTS

REF_AUDIO = os.path.expanduser('~/Downloads/Untitled(1).mp3')
REF_TEXT = (
    "A. A is for apple. Apple starts with A. "
    "B. B is for bear. Big friendly bear. "
    "C. C is for cat. Meow. "
    "D. D is for dog. Woof woof. "
    "E. E is for elephant. Giant elephant. "
    "F. F is for fish. Swimming in the sea. "
    "G. G is for giraffe. Tall neck giraffe."
)

OUTPUT_DIR = Path('/home/s8n/Caps-App-Final/easton-learning-app/app/src/main/res/raw')
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# 1. Clean Letter Names (spoken when letter is pressed on keyboard)
# We format with period/clean token so TTS speaks the exact letter name clearly
LETTERS = {
    'letter_a': 'A',
    'letter_b': 'B',
    'letter_c': 'C',
    'letter_d': 'D',
    'letter_e': 'E',
    'letter_f': 'F',
    'letter_g': 'G',
    'letter_h': 'H',
    'letter_i': 'I',
    'letter_j': 'J',
    'letter_k': 'K',
    'letter_l': 'L',
    'letter_m': 'M',
    'letter_n': 'N',
    'letter_o': 'O',
    'letter_p': 'P',
    'letter_q': 'Q',
    'letter_r': 'R',
    'letter_s': 'S',
    'letter_t': 'T',
    'letter_u': 'U',
    'letter_v': 'V',
    'letter_w': 'W',
    'letter_x': 'X',
    'letter_y': 'Y',
    'letter_z': 'Z',
}

# 2. Phonics with example words (for Learn By Listening mode)
PHONICS = {
    'phonics_a': "A is for Apple! Apple starts with A!",
    'phonics_b': "B is for Bear! Big friendly bear!",
    'phonics_c': "C is for Cat! Meow!",
    'phonics_d': "D is for Dog! Woof woof!",
    'phonics_e': "E is for Elephant! Giant elephant!",
    'phonics_f': "F is for Fish! Swimming in the sea!",
    'phonics_g': "G is for Giraffe! Tall neck giraffe!",
    'phonics_h': "H is for Hat! Put on your thinking hat!",
    'phonics_i': "I is for Ice Cream! Yummy treat!",
    'phonics_j': "J is for Jellyfish! Floating in the water!",
    'phonics_k': "K is for Kangaroo! Hop hop hop!",
    'phonics_l': "L is for Lion! King of the jungle!",
    'phonics_m': "M is for Monkey! Eating a banana!",
    'phonics_n': "N is for Nest! Cozy bird home!",
    'phonics_o': "O is for Orange! Sweet citrus fruit!",
    'phonics_p': "P is for Panda! Munching on bamboo!",
    'phonics_q': "Q is for Queen! Wearing a golden crown!",
    'phonics_r': "R is for Rainbow! Beautiful colors!",
    'phonics_s': "S is for Sun! Warm and bright!",
    'phonics_t': "T is for Tiger! Striped and strong!",
    'phonics_u': "U is for Umbrella! Keeping dry in the rain!",
    'phonics_v': "V is for Violin! Playing sweet music!",
    'phonics_w': "W is for Watermelon! Fresh and juicy!",
    'phonics_x': "X is for Xylophone! Making happy sounds!",
    'phonics_y': "Y is for Yo-yo! Spinning up and down!",
    'phonics_z': "Z is for Zebra! Black and white stripes!",
}

# 3. System, Onboarding, Headphone & Navigation lines
SYSTEM_PHRASES = {
    'welcome_first': (
        "Hey there! My name is Caps the Capybara, and I'm here to help you learn some letters, "
        "words, and typing. I like to have fun and learn, and I hope you do too! "
        "Go ahead and choose which way you'd like to begin today!"
    ),
    'welcome_back': "Hi Easton! Choose how you'd like to learn today!",
    'welcome_confirmed': "Awesome to meet you, Easton! Let's have fun!",
    'ask_name': "Hi there! I'm Caps the Capybara! What's your name?",
    'menu_greeting': "Hi Easton! Choose how you'd like to learn today!",
    'lbl_intro': "Touch any letter on screen or press a key, Easton!",
    'nav_level_switch': "Level switched! Let's keep having fun!",
    'level_up_2': "You're doing amazing, Easton! Moving to Level 2 with bigger words!",
    'level_up_3': "SUPERSTAR! You reached Level 3, Easton! Let's spell like a champ!",
    'headphone_reminder': "Heads up! Make sure you can hear me clearly! You can plug your headphones in if you'd like!",
}

# 4. Celebrations & Encouragement
CHEERS = {
    'cheer_awesome': "Awesome job Easton! You spelled it!",
    'cheer_superstar': "Superstar! You're doing amazing!",
    'cheer_fantastic': "Fantastic spelling, Easton!",
    'cheer_brilliant': "Brilliant! You did it!",
}

# 5. Hints & mistake support
HINTS = {
    'hint_general_retry': "That's okay! Keep trying, you can do it!",
}
for c in 'abcdefghijklmnopqrstuvwxyz':
    HINTS[f'hint_{c}'] = f"Try pressing the letter {c.upper()}!"

# Combine all target audio tasks
ALL_TASKS = {**LETTERS, **PHONICS, **SYSTEM_PHRASES, **CHEERS, **HINTS}

def post_process_audio(audio, input_sr=24000, target_sr=44100, threshold=0.010, pad_ms=30):
    if audio.ndim > 1:
        audio = audio.mean(axis=1)
    energy = np.abs(audio)
    above = np.where(energy > threshold)[0]
    if len(above) == 0:
        trimmed = audio.copy()
    else:
        start = max(0, above[0] - int(pad_ms * input_sr / 1000))
        end = min(len(audio), above[-1] + int(pad_ms * input_sr / 1000))
        trimmed = audio[start:end].copy()

    fade_len = min(int(0.01 * input_sr), len(trimmed) // 2)
    if fade_len > 0:
        trimmed[:fade_len] *= np.linspace(0.0, 1.0, fade_len)
        trimmed[-fade_len:] *= np.linspace(1.0, 0.0, fade_len)

    resampled = soxr.resample(trimmed, input_sr, target_sr)
    peak = np.max(np.abs(resampled))
    if peak > 0:
        resampled = resampled * (0.92 / peak)
    return resampled

def main():
    print("=" * 60)
    print("CAPS AUDIO GENERATOR — KITTENTTS-2 VOICE CLONING")
    print(f"Reference voice: {REF_AUDIO}")
    print(f"Total phrases to generate: {len(ALL_TASKS)}")
    print("=" * 60)

    t0 = time.time()
    print("Loading KittenTTS-2 model...")
    m = KittenTTS('KittenML/kitten-tts-2')
    print(f"Model loaded in {time.time()-t0:.2f}s")

    success = 0
    errors = 0

    for i, (key, text) in enumerate(ALL_TASKS.items(), 1):
        out_file = OUTPUT_DIR / f"{key}.mp3"
        print(f"[{i}/{len(ALL_TASKS)}] Generating {key}.mp3: \"{text[:45]}...\"" if len(text) > 45 else f"[{i}/{len(ALL_TASKS)}] Generating {key}.mp3: \"{text}\"")
        try:
            raw_audio = m.generate(text, reference=REF_AUDIO, reference_text=REF_TEXT)
            processed = post_process_audio(raw_audio, input_sr=m.sample_rate, target_sr=44100)
            sf.write(str(out_file), processed, 44100, format='MP3')
            success += 1
        except Exception as e:
            print(f"  ERROR generating {key}: {e}")
            errors += 1

    print("\n" + "=" * 60)
    print(f"GENERATION COMPLETE! Success: {success}, Errors: {errors}")
    print(f"Total time: {time.time()-t0:.2f}s")
    print("=" * 60)

if __name__ == '__main__':
    main()
