## Description
Please include a concise summary of the changes made, the motivation behind them, and which issues they resolve (if applicable).

Fixes #(issue)

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds offline functionality)
- [ ] UI / UX refinement or theme improvement
- [ ] Code refactoring / performance optimization
- [ ] Documentation update

## Offline & Privacy Integrity Checklist
- [ ] **100% Offline Rule:** This PR does NOT add `android.permission.INTERNET` or socket networking.
- [ ] **No Analytics:** This PR does NOT add telemetry, logging trackers, or analytics SDKs.
- [ ] **Zero Breaking Changes to Cryptography:** `HatifSecurityManager` and AES-256-GCM KeyStore logic remain intact and backward compatible with existing user diaries.
- [ ] **Service Invariance:** `ShortsReelsBlockerService` detection logic is intact.
- [ ] **Theme Support:** New or modified composables use `MaterialTheme.colorScheme` tokens and render cleanly in Xbox Dark, Light, and System themes.
- [ ] **Compilation:** The project compiles cleanly with `./gradlew assembleDebug` and `./gradlew test`.
- [ ] **Tested on Device:** Verified on a physical Android device or emulator.
