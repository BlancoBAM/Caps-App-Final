#!/usr/bin/env python3
"""
Generate priority UI and Headphone audio files using KittenTTS-2 with student_w4 decoder.
Reference: ~/Downloads/Untitled(1).mp3
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

TARGET_PHRASES = {
    'headphone_reminder': "Heads up! Make sure you can hear me clearly! You can plug your headphones in if you'd like!",
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
    'hint_general_retry': "That's okay! Keep trying, you can do it!",
}

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
    print(f"Loading KittenTTS-2 with student_w4 decoder...")
    t0 = time.time()
    m = KittenTTS('KittenML/kitten-tts-2', decoder='student_w4')
    print(f"Model loaded in {time.time()-t0:.2f}s")

    for i, (key, text) in enumerate(TARGET_PHRASES.items(), 1):
        out_file = OUTPUT_DIR / f"{key}.mp3"
        print(f"[{i}/{len(TARGET_PHRASES)}] Generating {key}.mp3...")
        t_start = time.time()
        try:
            budget = max(50, int(len(text) * 3.5))
            raw_audio = m.generate(text, reference=REF_AUDIO, reference_text=REF_TEXT, max_new_tokens=budget)
            processed = post_process_audio(raw_audio, input_sr=m.sample_rate, target_sr=44100)
            sf.write(str(out_file), processed, 44100, format='MP3')
            print(f"  -> Saved {key}.mp3 in {time.time()-t_start:.1f}s")
        except Exception as e:
            print(f"  -> Error: {e}")

    print("\nPriority UI audio generation complete!")

if __name__ == '__main__':
    main()
