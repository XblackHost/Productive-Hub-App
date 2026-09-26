# Contributing to Productive Hub

Thank you for your interest in contributing to **Productive Hub**! We welcome bug fixes, accessibility enhancements, performance optimizations, and documentation updates.

Please take a moment to review this document before submitting your contribution.

---

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please treat all contributors and users with respect, patience, and empathy.

---

## The Non-Negotiable Offline-First Rule

> ⚠️ **CRITICAL REQUIREMENT:**
> **Productive Hub is and will forever remain 100% offline.**
> 
> Any Pull Request that introduces:
> - `android.permission.INTERNET` or socket networking,
> - Telemetry, metrics, or analytics SDKs (e.g. Firebase, Mixpanel, Sentry),
> - Third-party ad networks,
> - Cloud database sync or remote authentication,
> 
> **will be rejected immediately and closed without review.** Everything built into this application must run completely on the user's local device without phone-home requirements.

---

## Reporting Bugs & Feature Requests

### Reporting Bugs
1. Search [GitHub Issues](https://github.com/XblackHost/Productive-Hub-App/issues) to ensure the bug hasn't already been reported.
2. If new, open an issue using the **Bug Report** template.
3. Include device details (manufacturer, Android version, custom OEM OS skin like Infinix XOS, Xiaomi HyperOS, Samsung One UI) and clear reproduction steps.

### Requesting Features
1. Open an issue using the **Feature Request** template.
2. Ensure the feature does not require network connectivity or violate the offline-first principle.
3. Explain the problem your feature solves and how it enhances user productivity or focus.

---

## Pull Request Guidelines

### 1. Branch Naming
Create a topic branch from `main`:
- `fix/issue-description` for bug fixes
- `feat/feature-name` for new local features
- `docs/update-readme` for documentation
- `refactor/component-name` for code refactoring

### 2. Commit Style
We follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat: add export button for grades`
- `fix: resolve back-action loop on rapid scroll`
- `docs: update OEM battery setup instructions`
- `refactor: extract chart canvas drawing logic`

### 3. Verification & Testing
Before submitting a PR:
- Ensure the project compiles cleanly:
  ```bash
  ./gradlew assembleDebug
  ./gradlew test
  ```
- Test on an actual physical Android device or emulator.
- Never modify or break the `HatifSecurityManager` KeyStore encryption logic. Existing user encrypted data must remain decodable.

---

## Code Style & Conventions

- **Language:** Kotlin 2.0+ exclusively.
- **UI Framework:** Jetpack Compose with Material 3 (M3) components.
- **Color tokens:** Do not hardcode hex color values inside composables. Use `MaterialTheme.colorScheme.*` so all elements adapt properly to Xbox Dark, Light, and System themes.
- **Formatting:** Follow the [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and Android Compose Guidelines.
- **State Management:** Keep composables stateless where possible, passing events upward and state downward via unidirectional data flow.
- **Accessibility:** Ensure touch targets are at least 48dp and provide meaningful `contentDescription` attributes for icons and interactive elements.
