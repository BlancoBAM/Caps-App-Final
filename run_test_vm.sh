#!/bin/bash
set -e

export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

AVD_NAME="CapsTestAVD"
APK_PATH="$HOME/Caps-App-Final/Caps-Updated.apk"

echo "=== Checking AVD ==="
if ! emulator -list-avds | grep -q "$AVD_NAME"; then
    echo "Creating AVD $AVD_NAME..."
    echo "no" | avdmanager create avd -n "$AVD_NAME" -k "system-images;android-34;google_apis;x86_64" --device "pixel_tablet" -f
fi

echo "=== Starting Emulator ($AVD_NAME) ==="
if adb devices | grep -q "emulator"; then
    echo "Emulator is already active."
    EMU_PID=$(pgrep -f "emulator.*$AVD_NAME" | head -n 1)
else
    emulator -avd "$AVD_NAME" -no-boot-anim -gpu swiftshader_indirect &
    EMU_PID=$!
    echo "Waiting for emulator device to be recognized (PID: $EMU_PID)..."
    adb wait-for-device
fi

echo "=== Waiting for Android system to complete boot ==="
while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    sleep 2
done
echo "Android VM is fully booted and ready!"

echo "=== Installing latest APK ($APK_PATH) ==="
adb install -r "$APK_PATH"

echo "=== Launching Caps App ==="
adb shell am start -n com.eastonlearning/.MainActivity
sleep 3

SCREENSHOT_PATH="$HOME/Caps-App-Final/caps_running_screenshot.png"
adb exec-out screencap -p > "$SCREENSHOT_PATH" 2>/dev/null || true
echo "Saved runtime screenshot to $SCREENSHOT_PATH"

echo "=== Testing Ready: Caps App is running on $AVD_NAME ==="
echo "The Android VM window is active. Press Ctrl+C or close the emulator window to stop."
if [ -n "$EMU_PID" ]; then
    wait $EMU_PID
fi
