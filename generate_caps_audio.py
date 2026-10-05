#!/usr/bin/env python3
"""
Generate Caps the Capybara Audio Assets
---------------------------------------
Parses CAPS_VOICE_SCRIPTS.md and synthesizes all required audio files using KittenTTS.
Processes audio with silence trimming, anti-click fading, 44.1 kHz resampling,
and peak volume normalization, saving standard MP3 files directly into
easton-learning-app/app/src/main/res/raw/.
"""

import os
import re
import sys
import time
import argparse
import numpy as np
import soxr
import soundfile as sf
from kittenml import KittenTTS

SCRIPT_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'CAPS_VOICE_SCRIPTS.md')
OUTPUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'easton-learning-app/app/src/main/res/raw')

def clean_text(t):
    """Strip quotes and markdown formatting."""
    t = t.strip()
    if t.startswith('"') and t.endswith('"'):
        t = t[1:-1]
    if t.startswith("'") and t.endswith("'"):
        t = t[1:-1]
    t = t.replace('`', '').replace('**', '').strip()
    return t

def parse_scripts(markdown_path):
    """Parses CAPS_VOICE_SCRIPTS.md into a list of (filename, spoken_text, section)."""
    with open(markdown_path, 'r', encoding='utf-8') as f:
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
            # parts: [filename, letter, letter_name_sound, phonics_assoc]
            letter_sound = clean_text(parts[2])
            phonics = clean_text(parts[3])
            letter_char = fn.replace('letter_', '').replace('.mp3', '')

            # letter_<x>.mp3 -> Letter Name & Sound (e.g. "A! Ah!")
            tasks.append((fn, letter_sound, current_sec))
            # phonics_<x>.mp3 -> Phonics Association (e.g. "A is for Apple! Apple starts with A!")
            tasks.append((f"phonics_{letter_char}.mp3", phonics, current_sec))

        elif current_sec.startswith('2. Level 1') or current_sec.startswith('3. Level 2') or current_sec.startswith('4. Level 3'):
            # parts: [filename, word, prompt_script, guide_script]
            prompt = clean_text(parts[2])
            guide = clean_text(parts[3])
            word_str = fn.replace('word_', '').replace('.mp3', '')

            # word_<word>.mp3 -> Word prompt (e.g. "Can you spell: Cat?")
            tasks.append((fn, prompt, current_sec))
            # spell_<word>.mp3 -> Spelling guide (e.g. "Cat is spelled C - A - T!")
            tasks.append((f"spell_{word_str}.mp3", guide, current_sec))

        elif current_sec.startswith('5. Welcome') or current_sec.startswith('6. Celebrations') or current_sec.startswith('7. Gentle Hints'):
            # parts: [filename, trigger/letter, voice_script]
            script = clean_text(parts[2])
            tasks.append((fn, script, current_sec))

    return tasks

def process_audio(audio, input_sr=24000, target_sr=44100, threshold=0.012, pad_ms=40):
    """Trim silence, apply cross-fade margins, resample, and peak-normalize."""
    energy = np.abs(audio)
    above = np.where(energy > threshold)[0]
    if len(above) == 0:
        trimmed = audio
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

def main():
    parser = argparse.ArgumentParser(description="Generate Caps Mascot Voice Audio")
    parser.add_argument('--voice', default='Hugo', help='KittenTTS voice (default: Hugo)')
    parser.add_argument('--speed', type=float, default=1.0, help='Speech speed (default: 1.0)')
    parser.add_argument('--overwrite', action='store_true', help='Overwrite existing files')
    args = parser.parse_args()

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    tasks = parse_scripts(SCRIPT_PATH)
    print(f"Loaded {len(tasks)} audio tasks from {SCRIPT_PATH}")

    print(f"Loading KittenTTS (KittenML/kitten-tts-nano-0.8)...")
    tts = KittenTTS('KittenML/kitten-tts-nano-0.8')
    print(f"Using voice '{args.voice}' at speed {args.speed}")
    print(f"Target directory: {OUTPUT_DIR}")

    start_time = time.time()
    generated_count = 0
    skipped_count = 0

    for i, (fn, text, sec) in enumerate(tasks, 1):
        out_path = os.path.join(OUTPUT_DIR, fn)
        if os.path.exists(out_path) and not args.overwrite:
            skipped_count += 1
            continue

        raw_audio = tts.generate(text, voice=args.voice, speed=args.speed)
        processed = process_audio(raw_audio)
        sf.write(out_path, processed, 44100, format='MP3')
        generated_count += 1

        if generated_count % 25 == 0 or i == len(tasks):
            elapsed = time.time() - start_time
            rate = generated_count / max(1e-5, elapsed)
            rem = (len(tasks) - i) / max(1e-5, rate)
            print(f"[{i}/{len(tasks)}] Generated {generated_count} clips ({rate:.1f} clips/s, ~{rem:.0f}s remaining)...")

    total_time = time.time() - start_time
    print(f"\nFinished! Generated: {generated_count}, Skipped: {skipped_count} in {total_time:.2f}s")
    
    # Calculate total size
    total_size = sum(os.path.getsize(os.path.join(OUTPUT_DIR, f)) for f in os.listdir(OUTPUT_DIR) if f.endswith('.mp3'))
    print(f"Total audio assets size: {total_size / (1024 * 1024):.2f} MB ({len(os.listdir(OUTPUT_DIR))} files)")

if __name__ == '__main__':
    main()
