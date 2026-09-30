#!/bin/bash
set -e

echo "=== Setting up Android SDK ==="
ANDROID_HOME="$HOME/Android/Sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"

# Download command-line tools if not present
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    echo "Downloading Android command-line tools..."
    cd /tmp
    wget -q --show-progress https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
    unzip -q cmdline-tools.zip -d /tmp/android-tools
    mv /tmp/android-tools/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
    rm cmdline-tools.zip
    echo "Done downloading."
fi

export ANDROID_HOME="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/33.0.2"
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"

echo "=== Accepting SDK licenses ==="
yes | sdkmanager --licenses > /dev/null 2>&1 || true

echo "=== Installing SDK platform and build tools ==="
sdkmanager "platforms;android-33" "build-tools;33.0.2" "platform-tools"

echo "=== Building APK ==="
cd "$HOME/Caps-App-Final/easton-learning-app"
chmod +x gradlew

# Write local.properties so Gradle finds the SDK
echo "sdk.dir=$ANDROID_HOME" > local.properties

ANDROID_HOME="$ANDROID_HOME" JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64" ./gradlew assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$HOME/Caps-App-Final/Caps-Updated.apk"
    echo ""
    echo "=== BUILD SUCCESSFUL ==="
    echo "APK saved to: $HOME/Caps-App-Final/Caps-Updated.apk"
else
    echo "APK not found at expected path."
fi
