# Setup Guide — Namma-Shaale Inventory

This guide walks you through setting up the development environment and running the Namma-Shaale Inventory app from source.

---

## Prerequisites

| Requirement | Minimum Version | Notes |
|-------------|----------------|-------|
| Android Studio | Hedgehog (2023.1.1) | Koala or later recommended |
| JDK | 11 | JDK 17 also works |
| Android SDK | API 28 | Download via SDK Manager |
| Android device or emulator | Android 9.0 (API 28) | Physical device preferred for camera |
| Git | 2.x | For cloning the repository |

---

## Step-by-Step Setup

### 1. Install Android Studio

Download from: https://developer.android.com/studio

During setup, ensure the following SDK components are installed:
- **Android SDK Platform 35** (compile target)
- **Android SDK Platform 28** (minimum, for testing)
- **Android Emulator** (if testing without a physical device)
- **Intel HAXM** or **Android Emulator Hypervisor Driver** (for emulator acceleration)

### 2. Clone the Repository

```bash
git clone https://github.com/rohittsinghh/namma-shaale-inventory.git
cd namma-shaale-inventory
```

### 3. Open in Android Studio

1. Open Android Studio
2. Select **File → Open**
3. Navigate to the cloned folder and click **OK**
4. Android Studio will detect the Gradle project automatically
5. Click **Sync Now** if prompted, or wait for the automatic sync

**First sync** downloads all Gradle dependencies (~200 MB). This can take 3–10 minutes depending on your internet speed.

### 4. Verify SDK Setup

Go to **File → Project Structure → SDK Location** and confirm:
- **Android SDK location** points to your SDK installation
- **JDK location** shows JDK 11 or later

### 5. Configure an Emulator (if needed)

1. Go to **Tools → Device Manager**
2. Click **Create Device**
3. Select **Pixel 7** (or any phone with Play Store)
4. Choose a system image with **API 28 or higher** (API 34 recommended)
5. Click **Finish** and start the emulator

### 6. Run the App

1. In the toolbar, select your device from the device dropdown
2. Click the green **Run** button or press `Shift+F10`
3. The app will build (~1–2 minutes first time) and launch on your device

---

## Groq API Key Setup (Optional)

The Groq API key enables AI-powered maintenance insights. The core app works without it.

### Get a Free API Key

1. Visit [console.groq.com](https://console.groq.com)
2. Sign up or log in
3. Go to **API Keys** in the left sidebar
4. Click **Create API Key**
5. Copy the generated key

### Add Key to the App

1. Launch the app and complete sign-in
2. Tap the **Profile** tab (bottom navigation)
3. Scroll to **Groq API Key** section
4. Tap the text field and paste your key
5. Tap **Test** — you should see a success confirmation
6. Tap **Update Key** to save it securely

The key is encrypted with **AES-256-GCM** via Android Keystore before being persisted. It is never stored in plain text.

---

## Common Setup Issues

### Gradle Sync Fails

**Symptom:** `Could not resolve com.google.dagger:hilt-android:2.59.2`

**Fix:**
1. Check your internet connection
2. In Android Studio: **File → Invalidate Caches and Restart**
3. Try `./gradlew assembleDebug --refresh-dependencies` from terminal

### KSP Annotation Processing Error

**Symptom:** Build error mentioning `room-compiler` or `hilt-android-compiler`

**Fix:** KSP requires matching versions with Kotlin. The `libs.versions.toml` already has aligned versions (`ksp = "2.0.21-1.0.28"` for `kotlin = "2.0.21"`). If you upgrade Kotlin, update KSP version accordingly.

### Camera Not Working on Emulator

**Symptom:** Camera preview is black or permission denied

**Fix:**
- Enable the camera in emulator settings: **... → Camera → Back Camera → select a webcam**
- Or test camera features on a physical device

### App Crashes at Launch on First Install

**Symptom:** App crashes immediately on fresh install

**Fix:** This usually means the Room database migration is missing after a schema change. For development, you can add `fallbackToDestructiveMigration()` in `AppDatabase.kt` — but never in production.

---

## Build Variants

| Variant | Purpose |
|---------|---------|
| `debug` | Development — includes logging, no obfuscation |
| `release` | Production — should enable ProGuard/R8 before distribution |

To build a release APK (requires signing configuration):
```bash
./gradlew assembleRelease
```

---

## Project Dependencies Overview

All dependencies are managed in `gradle/libs.versions.toml` (Gradle Version Catalog). Key libraries:

| Library | Purpose | Version |
|---------|---------|---------|
| Room | Local SQLite database | 2.7.1 |
| Hilt | Dependency injection | 2.59.2 |
| Compose BOM | UI framework (Material 3) | 2024.09.00 |
| Navigation Compose | Screen routing | 2.8.5 |
| Retrofit | HTTP client for Groq API | 2.11.0 |
| Coil | Async image loading | 2.7.0 |
| DataStore | Session persistence | 1.1.1 |

To check for dependency updates:
```bash
./gradlew dependencyUpdates
```

---

## Testing

### Run Unit Tests
```bash
./gradlew test
```

Test files are located in `app/src/test/java/com/example/nammashalli/`.

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

Requires a connected device or running emulator.

### Lint
```bash
./gradlew lint
```

Lint output: `app/build/reports/lint-results-debug.html`
