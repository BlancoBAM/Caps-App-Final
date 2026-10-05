#!/usr/bin/env python3
"""
Full High-Quality Audio Generator for Caps the Capybara
-------------------------------------------------------
1. Directly extracts Letters A-G (pure letter names & phonics) from user recording:
   ~/Downloads/Untitled(1).mp3
2. Clones the user's voice for Letters H-Z, Phonics H-Z, and all UI/Speech lines
   using KittenTTS-2.
3. Applies clean silence trimming, anti-click fades, and standard MP3 encoding
   directly into easton-learning-app/app/src/main/res/raw/.
"""

import os
import sys
import time
import numpy as np
import soxr
import soundfile as sf
from pathlib import Path
from kittenml import KittenTTS

REF_AUDIO = os.path.expanduser('~/Downloads/Untitled.mp3')
REF_TEXT = (
    "A. A is for apple. Apple starts with A. "
    "B. B is for bear. Big friendly bear. "
    "C. C is for cat. Meow. "
    "D. D is for dog. Woof woof. "
    "E. E is for elephant. Giant elephant. "
    "F. F is for fish. Swimming in the sea. "
    "G. G is for giraffe. Tall neck giraffe. "
    "H. H is for hat. Put on your thinking hat. "
    "I. I is for ice cream. Yummy treat."
)

OUTPUT_DIR = Path('/home/s8n/Caps-App-Final/easton-learning-app/app/src/main/res/raw')
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# Exact slices from the user's authentic recording:
# Pure letter names (ONLY the letter name, tight cut with no phonics sound)
USER_SLICES = {
    'letter_a': (0.00, 0.95),
    'letter_b': (4.48, 5.12),
    'letter_c': (8.60, 9.22),
    'letter_d': (11.56, 12.18),
    'letter_e': (15.22, 15.60),
    'letter_f': (18.70, 19.25),
    'letter_g': (23.16, 24.10),
    'letter_h': (27.50, 28.12),
    'letter_i': (31.94, 32.65),
    # Phonics lines with example words (for Learn By Listening mode)
    'phonics_a': (1.60, 4.45),
    'phonics_b': (5.14, 8.56),
    'phonics_c': (9.32, 11.56),
    'phonics_d': (12.20, 15.22),
    'phonics_e': (15.60, 18.70),
    'phonics_f': (19.28, 23.16),
    'phonics_g': (24.46, 27.45),
    'phonics_h': (28.64, 31.92),
    'phonics_i': (33.04, 36.02),
}

# Remaining pure letter names (J to Z) to generate via voice clone
LETTERS_TO_GEN = {
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

# Remaining phonics lines (J to Z) to generate via voice clone
PHONICS_TO_GEN = {
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

# UI, onboarding, celebrations, hints
UI_PHRASES = {
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
    'cheer_awesome': "Awesome job Easton! You spelled it!",
    'cheer_superstar': "Superstar! You're doing amazing!",
    'cheer_fantastic': "Fantastic spelling, Easton!",
    'cheer_brilliant': "Brilliant! You did it!",
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
    print("=" * 60)
    print("CAPS AUDIO GENERATION PIPELINE")
    print("=" * 60)

    # 1. Process direct slices from user audio
    ref_data, ref_sr = sf.read(REF_AUDIO)
    print(f"Loaded reference audio: {REF_AUDIO} (sr={ref_sr}, len={len(ref_data)/ref_sr:.2f}s)")

    print("\n--- Phase 1: Direct Slices (Letters A-G) ---")
    for key, (t0, t1) in USER_SLICES.items():
        s0 = int(t0 * ref_sr)
        s1 = int(t1 * ref_sr)
        slice_audio = ref_data[s0:s1]
        processed = post_process_audio(slice_audio, input_sr=ref_sr, target_sr=44100)
        out_file = OUTPUT_DIR / f"{key}.mp3"
        sf.write(str(out_file), processed, 44100, format='MP3')
        print(f"  Sliced: {key}.mp3 ({t0:.2f}s - {t1:.2f}s)")

    # 2. Load KittenTTS-2 model with fast 4-step student decoder
    print("\n--- Phase 2: Cloned Voice Synthesis (H-Z & Phrases) ---")
    t0 = time.time()
    print("Loading KittenTTS-2 (student_w4 decoder)...")
    m = KittenTTS('KittenML/kitten-tts-2', decoder='student_w4')
    print(f"Model loaded in {time.time()-t0:.2f}s")

    gen_tasks = {**LETTERS_TO_GEN, **PHONICS_TO_GEN, **UI_PHRASES}
    total = len(gen_tasks)
    print(f"Total phrases to synthesize: {total}")

    success = 0
    errors = 0

    for i, (key, text) in enumerate(gen_tasks.items(), 1):
        out_file = OUTPUT_DIR / f"{key}.mp3"
        # Sizing token budget for fast generation
        token_budget = max(45, int(len(text) * 4))
        if key.startswith('letter_'):
            token_budget = 40

        print(f"[{i}/{total}] Synthesizing {key}.mp3: \"{text[:40]}\" (budget={token_budget})...")
        t_start = time.time()
        try:
            raw_audio = m.generate(
                text,
                reference=REF_AUDIO,
                reference_text=REF_TEXT,
                max_new_tokens=token_budget
            )
            processed = post_process_audio(raw_audio, input_sr=m.sample_rate, target_sr=44100)
            sf.write(str(out_file), processed, 44100, format='MP3')
            print(f"      -> Done in {time.time()-t_start:.1f}s")
            success += 1
        except Exception as e:
            print(f"      -> ERROR: {e}")
            errors += 1

    print("\n" + "=" * 60)
    print(f"AUDIO GENERATION COMPLETE!")
    print(f"Direct from user voice: {len(USER_SLICES)} clips (A-G)")
    print(f"Synthesized matching voice: {success} clips")
    print(f"Errors: {errors}")
    print(f"Total time: {time.time()-t0:.1f}s")
    print("=" * 60)

if __name__ == '__main__':
    main()
