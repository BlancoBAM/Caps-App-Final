#!/usr/bin/env python3
"""
Full Audio Asset Generator for Caps the Capybara
-------------------------------------------------
1. Extracts Letters A-I (names & phonics) from user reference audio (~/Downloads/Untitled.mp3).
2. Synthesizes all remaining scripts from CAPS_VOICE_SCRIPTS.md using KittenTTS
   with the matching voice persona ('Rosie', speed 1.1) for complete acoustic consistency.
3. Trims dead silence, applies anti-click fades, resamples to 44.1 kHz, and peak-normalizes.
4. Saves standard MP3 assets to easton-learning-app/app/src/main/res/raw/.
"""

import os
import re
import time
import numpy as np
import soxr
import soundfile as sf
from kittenml import KittenTTS

BASE_DIR = '/home/s8n/Caps-App-Final'
SCRIPT_PATH = os.path.join(BASE_DIR, 'CAPS_VOICE_SCRIPTS.md')
OUTPUT_DIR = os.path.join(BASE_DIR, 'easton-learning-app/app/src/main/res/raw')
REF_AUDIO_PATH = '/home/s8n/Downloads/Untitled.mp3'

os.makedirs(OUTPUT_DIR, exist_ok=True)

def clean_text(t):
    t = t.strip()
    if t.startswith('"') and t.endswith('"'):
        t = t[1:-1]
    if t.startswith("'") and t.endswith("'"):
        t = t[1:-1]
    return t.replace('`', '').replace('**', '').strip()

def process_audio(audio, input_sr=24000, target_sr=44100, threshold=0.012, pad_ms=40):
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
        resampled = resampled * (0.90 / peak)
    return resampled

def parse_scripts():
    with open(SCRIPT_PATH, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    current_sec = ""
    tasks = []

    for line in lines:
        line = line.strip()
        if line.startswith('## '):
            current_sec = line[3:].strip()
            continue

        if not (line.startswith('|') and '.mp3' in line):
            continue

        parts = [p.strip() for p in line.split('|')[1:-1]]
        fn = clean_text(parts[0])

        if current_sec.startswith('1. Alphabet Letters'):
            letter_sound = clean_text(parts[2])
            phonics = clean_text(parts[3])
            letter_char = fn.replace('letter_', '').replace('.mp3', '')
            tasks.append((fn, letter_sound, current_sec))
            tasks.append((f"phonics_{letter_char}.mp3", phonics, current_sec))

        elif current_sec.startswith('2. Level 1') or current_sec.startswith('3. Level 2') or current_sec.startswith('4. Level 3'):
            prompt = clean_text(parts[2])
            guide = clean_text(parts[3])
            word_str = fn.replace('word_', '').replace('.mp3', '')
            tasks.append((fn, prompt, current_sec))
            tasks.append((f"spell_{word_str}.mp3", guide, current_sec))

        elif current_sec.startswith('5. Welcome') or current_sec.startswith('6. Celebrations') or current_sec.startswith('7. Gentle Hints'):
            script = clean_text(parts[2])
            tasks.append((fn, script, current_sec))

    return tasks

# Reference slices from user audio for A through I
REF_SLICES = {
    'letter_a.mp3': (0.00, 1.50),
    'phonics_a.mp3': (1.50, 4.25),
    'letter_b.mp3': (4.25, 5.05),
    'phonics_b.mp3': (5.05, 8.65),
    'letter_c.mp3': (8.65, 9.30),
    'phonics_c.mp3': (9.30, 11.45),
    'letter_d.mp3': (11.45, 12.10),
    'phonics_d.mp3': (12.10, 15.30),
    'letter_e.mp3': (15.30, 16.35),
    'phonics_e.mp3': (16.35, 18.60),
    'letter_f.mp3': (18.60, 19.25),
    'phonics_f.mp3': (19.25, 23.00),
    'letter_g.mp3': (23.00, 24.40),
    'phonics_g.mp3': (24.40, 27.40),
    'letter_h.mp3': (27.40, 28.60),
    'phonics_h.mp3': (28.60, 31.80),
    'letter_i.mp3': (31.80, 33.10),
    'phonics_i.mp3': (33.10, 36.05),
}

def main():
    print("=" * 60)
    print("CAPS THE CAPYBARA — AUDIO ASSET GENERATOR")
    print("=" * 60)

    # 1. Load user reference audio
    has_ref = os.path.exists(REF_AUDIO_PATH)
    ref_data = None
    ref_sr = 16000
    if has_ref:
        print(f"Loaded reference audio from {REF_AUDIO_PATH}")
        ref_data, ref_sr = sf.read(REF_AUDIO_PATH)
    else:
        print(f"Warning: Reference audio not found at {REF_AUDIO_PATH}, using TTS for all files.")

    # 2. Parse script tasks
    tasks = parse_scripts()
    print(f"Parsed {len(tasks)} audio tasks from {SCRIPT_PATH}")

    # 3. Load KittenTTS model
    print("Loading KittenTTS (KittenML/kitten-tts-nano-0.8)...")
    tts = KittenTTS('KittenML/kitten-tts-nano-0.8')
    VOICE = 'Rosie'  # Matches pitch & timbre of reference audio (~240 Hz F0)
    SPEED = 1.10
    print(f"TTS Voice: '{VOICE}' at speed {SPEED}")

    start_time = time.time()
    generated_count = 0
    sliced_from_ref = 0

    for i, (fn, text, sec) in enumerate(tasks, 1):
        out_path = os.path.join(OUTPUT_DIR, fn)

        if has_ref and fn in REF_SLICES:
            t0, t1 = REF_SLICES[fn]
            s0 = int(t0 * ref_sr)
            s1 = int(t1 * ref_sr)
            slice_audio = ref_data[s0:s1]
            processed = process_audio(slice_audio, input_sr=ref_sr, target_sr=44100)
            sf.write(out_path, processed, 44100, format='MP3')
            sliced_from_ref += 1
        else:
            raw_audio = tts.generate(text, voice=VOICE, speed=SPEED)
            processed = process_audio(raw_audio, input_sr=24000, target_sr=44100)
            sf.write(out_path, processed, 44100, format='MP3')
            generated_count += 1

        if i % 25 == 0 or i == len(tasks):
            elapsed = time.time() - start_time
            rate = i / max(1e-5, elapsed)
            rem = (len(tasks) - i) / max(1e-5, rate)
            print(f"[{i}/{len(tasks)}] Sliced: {sliced_from_ref}, Synthesized: {generated_count} ({rate:.1f} clips/s, ~{rem:.0f}s rem)...")

    total_time = time.time() - start_time
    total_files = len([f for f in os.listdir(OUTPUT_DIR) if f.endswith('.mp3')])
    total_size_mb = sum(os.path.getsize(os.path.join(OUTPUT_DIR, f)) for f in os.listdir(OUTPUT_DIR) if f.endswith('.mp3')) / (1024 * 1024)

    print("\n" + "=" * 60)
    print(f"AUDIO GENERATION COMPLETE!")
    print(f"Total files: {total_files}")
    print(f"Direct from user voice: {sliced_from_ref} clips (A-I)")
    print(f"Synthesized matching voice: {generated_count} clips")
    print(f"Total audio directory size: {total_size_mb:.2f} MB")
    print(f"Elapsed time: {total_time:.2f}s")
    print("=" * 60)

if __name__ == '__main__':
    main()
