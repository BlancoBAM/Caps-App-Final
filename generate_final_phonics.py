#!/usr/bin/env python3
"""
Generate phonics_s through phonics_z with KittenTTS-2 voice cloning.
Reference: ~/Downloads/Untitled(1).mp3
"""
import os, time
import soundfile as sf
import soxr
import numpy as np
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

PHONICS_REMAINING = {
    'phonics_s': "S is for Sun! Warm and bright!",
    'phonics_t': "T is for Tiger! Striped and strong!",
    'phonics_u': "U is for Umbrella! Keeping dry in the rain!",
    'phonics_v': "V is for Violin! Playing sweet music!",
    'phonics_w': "W is for Watermelon! Fresh and juicy!",
    'phonics_x': "X is for Xylophone! Making happy sounds!",
    'phonics_y': "Y is for Yo-yo! Spinning up and down!",
    'phonics_z': "Z is for Zebra! Black and white stripes!",
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
    print("Loading KittenTTS-2 with student_w4 decoder...")
    m = KittenTTS('KittenML/kitten-tts-2', decoder='student_w4')
    for key, text in PHONICS_REMAINING.items():
        out_file = OUTPUT_DIR / f"{key}.mp3"
        print(f"Generating {key}.mp3: \"{text}\"...")
        try:
            budget = max(50, int(len(text) * 3.5))
            raw_audio = m.generate(text, reference=REF_AUDIO, reference_text=REF_TEXT, max_new_tokens=budget)
            processed = post_process_audio(raw_audio, input_sr=m.sample_rate, target_sr=44100)
            sf.write(str(out_file), processed, 44100, format='MP3')
            print(f"  ✓ Saved {key}.mp3")
        except Exception as e:
            print(f"  ✗ Error: {e}")
    print("All remaining phonics generated!")

if __name__ == '__main__':
    main()
