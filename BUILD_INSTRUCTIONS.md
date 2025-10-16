# Building Caps-App APK

Your Caps-App is ready to build! Since you don't have Java/Android SDK installed locally, here are your options:

## Option 1: Install Android Studio (Recommended)

1. **Download Android Studio**: https://developer.android.com/studio
2. **Install it** on your system
3. **Open the project**: Open the `/home/bbam/Caps-App/easton-learning-app` folder in Android Studio
4. **Build APK**: Go to Build → Build Bundle(s) / APK(s) → Build APK(s)
5. **Find the APK**: It will be in `app/build/outputs/apk/debug/app-debug.apk`

## Option 2: Command Line Build (After Installing Java/Android SDK)

```bash
# Install OpenJDK 11
sudo apt update
sudo apt install openjdk-11-jdk

# Download and install Android SDK
wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip
unzip commandlinetools-linux-8512546_latest.zip
mkdir -p ~/Android/Sdk/cmdline-tools
mv cmdline-tools ~/Android/Sdk/cmdline-tools/latest

# Set environment variables
export ANDROID_HOME=~/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept licenses and install required packages
yes | sdkmanager --licenses
sdkmanager "platforms;android-33" "build-tools;33.0.0"

# Build the APK
cd /home/bbam/Caps-App/easton-learning-app
./gradlew assembleDebug
```

## Option 3: Use GitHub Actions (Cloud Build)

I can help you set up automated building using GitHub Actions if you push your code to GitHub.

## Option 4: Docker Build

```bash
# Use an Android build container
docker run --rm -v "$(pwd)":/workspace -w /workspace mingc/android-build-box bash -c "
    chmod +x gradlew && 
    ./gradlew assembleDebug
"
```

## Installing the APK on Amazon Fire Tablet

Once you have the APK file:

1. **Enable Developer Options** on the Fire tablet:
   - Go to Settings → Device Options
   - Tap "Serial Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "Apps from Unknown Sources"

2. **Transfer the APK**:
   - Copy `app-debug.apk` to the tablet via USB or cloud storage

3. **Install**:
   - Open the APK file on the tablet
   - Tap "Install" when prompted
   - The app will appear as "Learn By Listening"

## Quick Test Build

If you just want to test that everything is set up correctly, you can use Android Studio's "Instant Run" feature or the built-in emulator to test the app before building the final APK.

The app is fully configured and ready to build - all the functionality from your requirements is implemented!