# Security Policy

## Supported Versions

We provide security updates for the following versions of **Productive Hub**:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

---

## Reporting a Vulnerability

If you discover a security vulnerability in Productive Hub, **please do NOT open a public GitHub issue.** Public disclosure exposes users before a fix can be delivered.

Instead, please report vulnerabilities responsibly via:
- **GitHub Security Advisories:** Go to the [Security Tab](https://github.com/XblackHost/Productive-Hub-App/security/advisories/new) of this repository and open a private advisory draft.
- **Direct Email:** Contact the project maintainer at `xblackgamerz24@gmail.com` with the subject `[SECURITY] Productive Hub Vulnerability Report`.

Please include:
1. Detailed description of the vulnerability.
2. Steps to reproduce or proof-of-concept code.
3. Potential impact on user confidentiality or device integrity.
4. Any proposed remediations.

We will acknowledge receipt of your report within 48 hours and work with you on a timely resolution before public disclosure.

---

## Threat Model & Security Posture

### 1. Zero Network Surface
Productive Hub has **no network attack surface**:
- `android.permission.INTERNET` is not requested.
- No network listeners, HTTP clients, socket connections, or cloud endpoints exist in the application binary.
- Remote exploitation via the network is fundamentally impossible.

### 2. Cryptographic Storage & KeyStore
- Personal journal and diary entries are encrypted using **AES-256-GCM**.
- Secret keys are generated through the hardware-backed **Android KeyStore** provider (`AndroidKeyStore`) and never leave the secure hardware enclave (TEE/StrongBox).
- Cryptographic initialization vectors (IVs) are uniquely generated per encryption operation and prepended to ciphertexts.

### 3. Accessibility Service Boundaries
- The `AccessibilityService` is strictly constrained to detecting `com.google.android.youtube` and `com.instagram.android` scroll nodes.
- View content from other applications is dropped immediately at runtime without logging or persistence.
- Keystrokes, passwords, and sensitive input fields are never inspected or recorded.

### 4. Out of Scope
The following are explicitly outside the application's threat model:
- **Physical possession of an unlocked, rooted device:** If an attacker has root privileges (`su`) or physical access to an unlocked device with developer debugging enabled, device-level operating system guarantees are void.
- **Side-channel or hardware fault attacks:** Physical hardware tampering of device flash chips or SoC enclaves is outside the control of user-space Android applications.
- **User voluntary export disclosure:** When a user exports unencrypted plain text or shares backups with third parties.
