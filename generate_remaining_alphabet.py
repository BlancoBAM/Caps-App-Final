#!/usr/bin/env python3
"""
Generate remaining letters and phonics (J to Z) using KittenTTS-2 voice cloning.
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

LETTERS = {
    'letter_j': 'J.',
    'letter_k': 'K.',
    'letter_l': 'L.',
    'letter_m': 'M.',
    'letter_n': 'N.',
    'letter_o': 'O.',
    'letter_p': 'P.',
    'letter_q': 'Q.',
    'letter_r': 'R.',
    'letter_s': 'S.',
    'letter_t': 'T.',
    'letter_u': 'U.',
    'letter_v': 'V.',
    'letter_w': 'W.',
    'letter_x': 'X.',
    'letter_y': 'Y.',
    'letter_z': 'Z.',
}

PHONICS = {
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

TASKS = {**LETTERS, **PHONICS}

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
    print("Loading KittenTTS-2 with student_w4 decoder...")
    m = KittenTTS('KittenML/kitten-tts-2', decoder='student_w4')

    for i, (key, text) in enumerate(TASKS.items(), 1):
        out_file = OUTPUT_DIR / f"{key}.mp3"
        print(f"[{i}/{len(TASKS)}] Generating {key}.mp3: \"{text[:35]}\"...")
        try:
            budget = 40 if key.startswith('letter_') else max(50, int(len(text) * 3.5))
            raw_audio = m.generate(text, reference=REF_AUDIO, reference_text=REF_TEXT, max_new_tokens=budget)
            processed = post_process_audio(raw_audio, input_sr=m.sample_rate, target_sr=44100)
            sf.write(str(out_file), processed, 44100, format='MP3')
            print(f"  -> Saved {key}.mp3")
        except Exception as e:
            print(f"  -> Error: {e}")

if __name__ == '__main__':
    main()
