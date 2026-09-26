# Productive Hub

> **Your attention, your device, your rules. A 100% offline productivity suite.**

[![Android API](https://img.shields.io/badge/Android%20API-26%2B-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-purple.svg)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://github.com/XblackHost/Productive-Hub-App/actions/workflows/build.yml/badge.svg)](https://github.com/XblackHost/Productive-Hub-App/actions)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

---

## Why I Built This

Most productivity and habit-tracking apps on the Google Play Store do the exact opposite of what they promise: they track your every action, monetize your attention with intrusive ads, lock essential features behind recurring subscriptions, and require internet access just to save a thought. 

I built **Productive Hub** because our attention belongs to us. No app designed to build focus should harvest personal telemetry or phone home to cloud servers. Productive Hub is completely offline, privacy-first, free, and open-source forever. Everything you write and every metric you track stays right where it belongs: on your personal device.

---

## Features

### 1. Shorts & Reels Blocker
An intelligent Android `AccessibilityService` that monitors fast-scrolling dopamine traps in YouTube Shorts and Instagram Reels. When detected, it triggers an immediate system back action to exit the feed and records daily distraction metrics.

### 2. Encrypted Diary
A private journal secured with hardware-backed Android KeyStore encryption (`AES-256-GCM`). Your personal reflections are protected by biometric or PIN authentication, with support for encrypted backup and export.

### 3. 30-Day Self-Reflection
A structured daily assessment tool tracking focus, discipline, and habit consistency over a 30-day cycle. Generates formatted offline analytical summaries ready to paste into external AI reasoning models for self-improvement insights.

### 4. Academic Grades Tracker
A dedicated GPA and course progress manager designed for students. Track assignment scores, exam weightings, and goal percentages offline without cloud synchronization.

### 5. Pomodoro Focus Timer
A clean interval timer (25-minute focus intervals, 5-minute restorative breaks) to structure study or deep work sessions without notification distractions.

### 6. Peer Admin Lock
A tamper-proofing mechanism configured with a trusted friend, study partner, or parent. Prevents impulsive disabling of focus restrictions and lock features when willpower runs low.

---

## Screenshots

> *Note: Place high-resolution application screenshots in `docs/screenshots/`.*

| Dashboard | Shorts Blocker | Encrypted Diary |
| :---: | :---: | :---: |
| ![](docs/screenshots/dashboard.png) | ![](docs/screenshots/blocker.png) | ![](docs/screenshots/diary.png) |

| Reflection Cycle | Academic Tracker | Appearance & Settings |
| :---: | :---: | :---: |
| ![](docs/screenshots/reflection.png) | ![](docs/screenshots/grades.png) | ![](docs/screenshots/settings.png) |

---

## Privacy Guarantee

- **Zero Network Permissions:** `android.permission.INTERNET` is completely absent from the manifest. Productive Hub cannot connect to any server, transmit data, or download remote code.
- **No Telemetry & No Analytics:** No Firebase, no Google Analytics, no third-party SDKs, and no tracking libraries are included in the codebase.
- **Hardware-Backed Cryptography:** Diary entries are encrypted using `AES-256-GCM` with keys generated and stored inside the Android hardware-backed KeyStore.
- **Scoped Accessibility:** The accessibility service monitors view hierarchies strictly for YouTube Shorts and Instagram Reels identifiers and completely ignores all other applications and input events.

---

## Architecture & Tech Stack

- **UI Layer:** 100% Jetpack Compose using Material Design 3 (M3) design tokens with support for Xbox Dark, Light, and System themes.
- **Architecture Pattern:** Clean MVVM (Model-View-ViewModel) with unidirectional data flow.
- **Reactive State:** Kotlin Coroutines and `StateFlow` for state management.
- **Hardware & System Integration:** Android `AccessibilityService` for UI tree detection and back-navigation triggers.
- **Security & Storage:** Android KeyStore, `EncryptedSharedPreferences`, and standard Android `SharedPreferences`.
- **Zero Network Dependencies:** Built entirely with offline standard libraries.

---

## Getting Started / Building

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1) or newer
- **JDK:** OpenJDK 17 or newer
- **Android SDK:** Compile SDK 35, Min SDK 26

### Cloning & Building

```bash
# Clone the repository
git clone https://github.com/XblackHost/Productive-Hub-App.git
cd Productive-Hub-App

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### OEM Setup Note
Custom Android skins (e.g., Infinix XOS, Xiaomi MIUI/HyperOS, Samsung One UI, Oppo/Realme ColorOS) aggressively terminate background accessibility services. For uninterrupted operation:
1. Open **App Info** for Productive Hub.
2. Set **Battery Usage** to **Unrestricted** / **No restrictions**.
3. Enable **Autostart** or lock Productive Hub in the Recents overview.

---

## Contributing

Contributions from the open-source community are warmly welcomed! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) before submitting an issue or pull request.

> **CRITICAL:** Pull requests that introduce `android.permission.INTERNET`, external telemetry, tracking SDKs, or cloud backends will be rejected immediately without exception.

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.  
Copyright © 2026 **XBLACK1852**.

---

## Credits

- [Android Open Source Project (AOSP)](https://source.android.com/)
- [Jetpack Compose & Material 3](https://developer.android.com/jetpack/compose)
- The open-source privacy and digital well-being developer community.
