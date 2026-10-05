#!/bin/bash
# ==============================================================================
# Script: run_fire_tablet.sh
# Purpose: Launch Amazon Fire HD 10 Android Virtual Device and run Caps App
# Specs: 1920x1200 (Landscape Tablet), 240 DPI, 2048 MB RAM, 8 vCPUs, Sound Enabled
# ==============================================================================

set -eo pipefail

# ANSI Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AVD_NAME="Amazon_Fire_HD_10"
APK_PATH="$PROJECT_DIR/Caps-Updated.apk"
PACKAGE_NAME="com.eastonlearning"
MAIN_ACTIVITY="com.eastonlearning/.MainActivity"

echo -e "${CYAN}${BOLD}"
echo "=========================================================="
echo "   Amazon Fire HD 10 Virtual Device & App Launcher        "
echo "=========================================================="
echo -e "${NC}"

# 1. Environment & Paths
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"
export PULSE_SERVER="${PULSE_SERVER:-/run/user/$(id -u)/pulse/native}"
export QTWEBENGINE_RESOURCES_PATH="$ANDROID_HOME/emulator/lib64/qt/resources"
export LD_LIBRARY_PATH="$ANDROID_HOME/emulator/lib64:$LD_LIBRARY_PATH"

# 2. Dependency & Library Checks
echo -e "${BLUE}[1/5] Checking environment and dependencies...${NC}"

if [ ! -d "$ANDROID_HOME/emulator" ] || [ ! -d "$ANDROID_HOME/platform-tools" ]; then
    echo -e "${RED}Error: Android SDK not found at $ANDROID_HOME${NC}"
    exit 1
fi

# Ensure libprotobuf.so.28.4.0 symlink exists
if [ -f "$ANDROID_HOME/emulator/lib64/libprotobuf.so" ] && [ ! -e "$ANDROID_HOME/emulator/lib64/libprotobuf.so.28.4.0" ]; then
    ln -sf libprotobuf.so "$ANDROID_HOME/emulator/lib64/libprotobuf.so.28.4.0"
fi

# Ensure QtWebEngine resources are linked
if [ -d "$ANDROID_HOME/emulator/lib64/qt/libexec" ] && [ -d "$ANDROID_HOME/emulator/lib64/qt/resources" ]; then
    ln -sf "$ANDROID_HOME/emulator/lib64/qt/libexec/qtwebengine_resources.pak" "$ANDROID_HOME/emulator/lib64/qt/resources/qtwebengine_resources.pak" 2>/dev/null || true
    ln -sf "$ANDROID_HOME/emulator/lib64/qt/libexec/qtwebengine_devtools_resources.pak" "$ANDROID_HOME/emulator/lib64/qt/resources/qtwebengine_devtools_resources.pak" 2>/dev/null || true
    ln -sf "$ANDROID_HOME/emulator/lib64/qt/libexec/icudtl.dat" "$ANDROID_HOME/emulator/lib64/qt/resources/icudtl.dat" 2>/dev/null || true
fi

# 3. Ensure AVD exists and matches specifications
echo -e "${BLUE}[2/5] Verifying Amazon Fire HD 10 AVD configuration...${NC}"
AVD_DIR="$HOME/.android/avd/$AVD_NAME.avd"
AVD_INI="$HOME/.android/avd/$AVD_NAME.ini"

if [ ! -d "$AVD_DIR" ] || [ ! -f "$AVD_DIR/config.ini" ]; then
    echo -e "${YELLOW}AVD $AVD_NAME not found. Creating AVD with Amazon Fire HD 10 specs...${NC}"
    mkdir -p "$AVD_DIR"
    
    cat << EOF > "$AVD_INI"
avd.ini.encoding=UTF-8
path=$AVD_DIR
path.rel=avd/$AVD_NAME.avd
target=android-30
EOF

    # Copy base userdata and generate sdcard
    SYS_IMG_USERDATA="$ANDROID_HOME/system-images/android-30/google_apis/x86_64/userdata.img"
    if [ -f "$SYS_IMG_USERDATA" ]; then
        cp "$SYS_IMG_USERDATA" "$AVD_DIR/userdata.img"
    fi
    "$ANDROID_HOME/emulator/mksdcard" 512M "$AVD_DIR/sdcard.img" >/dev/null 2>&1 || true

    cat << EOF > "$AVD_DIR/config.ini"
AvdId=$AVD_NAME
PlayStore.enabled=false
abi.type=x86_64
avd.ini.displayname=Amazon Fire HD 10
avd.ini.encoding=UTF-8
disk.dataPartition.size=6G
fastboot.chosenSnapshotFile=
fastboot.forceChosenSnapshotBoot=no
fastboot.forceColdBoot=no
fastboot.forceFastBoot=yes
hw.accelerometer=yes
hw.arc=false
hw.audioInput=yes
hw.audioOutput=yes
hw.battery=yes
hw.camera.back=virtualscene
hw.camera.front=emulated
hw.cpu.arch=x86_64
hw.cpu.ncore=8
hw.dPad=no
hw.device.manufacturer=Amazon
hw.device.name=fire_hd_10
hw.gps=yes
hw.gpu.enabled=yes
hw.gpu.mode=auto
hw.gyroscope=yes
hw.initialOrientation=landscape
hw.keyboard=yes
hw.lcd.density=240
hw.lcd.height=1200
hw.lcd.width=1920
hw.mainKeys=no
hw.ramSize=2048
hw.sdCard=yes
hw.sensors.light=yes
hw.sensors.magnetic_field=yes
hw.sensors.orientation=yes
hw.sensors.pressure=yes
hw.sensors.proximity=no
hw.trackBall=no
image.sysdir.1=system-images/android-30/google_apis/x86_64/
runtime.network.latency=none
runtime.network.speed=full
sdcard.path=$AVD_DIR/sdcard.img
sdcard.size=512M
showDeviceFrame=yes
skin.dynamic=yes
tag.display=Google APIs
tag.displaynames=Google APIs
tag.id=google_apis
tag.ids=google_apis
target=android-30
vm.heapSize=256
EOF
    echo -e "${GREEN}✓ Created Amazon Fire HD 10 AVD profile successfully.${NC}"
else
    echo -e "${GREEN}✓ Amazon Fire HD 10 AVD profile verified.${NC}"
fi

# Print Specifications
echo -e "${CYAN}Target Device Specs:${NC}"
echo "  • Device:  Amazon Fire HD 10"
echo "  • Display: 1920 x 1200 px (Landscape Tablet)"
echo "  • Density: 240 DPI"
echo "  • RAM:     2048 MB"
echo "  • vCPUs:   8 cores"
echo "  • Sound:   PulseAudio / PipeWire Enabled (Speaker & Mic)"

# Optional rebuild if requested with --rebuild
if [[ "$*" == *"--rebuild"* ]]; then
    echo -e "${BLUE}Rebuilding latest APK from source...${NC}"
    cd "$PROJECT_DIR/easton-learning-app"
    chmod +x gradlew
    ./gradlew assembleDebug
    cp app/build/outputs/apk/debug/app-debug.apk "$APK_PATH"
    cd "$PROJECT_DIR"
    echo -e "${GREEN}✓ APK rebuilt.${NC}"
fi

# 4. Check if emulator is already running or start it
echo -e "${BLUE}[3/5] Starting Amazon Fire HD 10 Emulator...${NC}"

# Check if an emulator instance is already running
RUNNING_SERIAL=$(adb devices | grep -E "emulator-[0-9]+" | awk '{print $1}' | head -n 1 || true)

if [ -n "$RUNNING_SERIAL" ]; then
    echo -e "${GREEN}Found running emulator: $RUNNING_SERIAL${NC}"
    TARGET_SERIAL="$RUNNING_SERIAL"
else
    echo -e "${YELLOW}Launching emulator in background with sound enabled...${NC}"
    # Launch emulator with hardware acceleration and audio
    nohup "$ANDROID_HOME/emulator/emulator" \
        -avd "$AVD_NAME" \
        -accel on \
        -gpu swiftshader_indirect \
        -allow-host-audio \
        > /tmp/fire_hd_10_emulator.log 2>&1 &
    
    EMU_PID=$!
    echo -e "${CYAN}Emulator PID: $EMU_PID (logs at /tmp/fire_hd_10_emulator.log)${NC}"

    echo -e "${YELLOW}Waiting for emulator to connect to adb...${NC}"
    adb wait-for-device
    TARGET_SERIAL=$(adb devices | grep -E "emulator-[0-9]+" | awk '{print $1}' | head -n 1)
fi

echo -e "${BLUE}[4/5] Waiting for Android system to complete boot...${NC}"
BOOT_COMPLETE=""
RETRIES=0
MAX_RETRIES=60

while [ "$BOOT_COMPLETE" != "1" ] && [ $RETRIES -lt $MAX_RETRIES ]; do
    BOOT_COMPLETE=$(adb -s "$TARGET_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)
    if [ "$BOOT_COMPLETE" = "1" ]; then
        break
    fi
    sleep 2
    RETRIES=$((RETRIES + 1))
    echo -n "."
done
echo ""

if [ "$BOOT_COMPLETE" != "1" ]; then
    echo -e "${RED}Warning: Timed out waiting for sys.boot_completed. Will try proceeding...${NC}"
fi

# Unmute and set media volume to maximum
adb -s "$TARGET_SERIAL" shell media volume --stream 3 --set 15 2>/dev/null || true

# 5. Install APK and launch application
echo -e "${BLUE}[5/5] Installing and launching latest Caps App...${NC}"

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}Error: APK not found at $APK_PATH${NC}"
    exit 1
fi

echo -e "Installing: ${BOLD}$APK_PATH${NC}..."
adb -s "$TARGET_SERIAL" install -r "$APK_PATH"

echo -e "Launching: ${BOLD}$MAIN_ACTIVITY${NC}..."
adb -s "$TARGET_SERIAL" shell am start -n "$MAIN_ACTIVITY"

echo -e "${GREEN}${BOLD}"
echo "=========================================================="
echo "   Caps App is now running on Amazon Fire HD 10!         "
echo "=========================================================="
echo -e "${NC}"
echo -e "Audio is unmuted and streaming to your system speakers."
echo -e "To view emulator logs in real-time:"
echo -e "  ${CYAN}tail -f /tmp/fire_hd_10_emulator.log${NC}"
echo -e "To view app logs (Logcat):"
echo -e "  ${CYAN}adb logcat | grep -i easton${NC}"
echo ""

# If launched by this script, wait on emulator so closing the window exits cleanly
if [ -n "$EMU_PID" ]; then
    echo -e "${YELLOW}Emulator is active. Press Ctrl+C or close the emulator window to finish.${NC}"
    wait "$EMU_PID" 2>/dev/null || true
fi
