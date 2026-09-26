# Focus Hatif!

**Focus Hatif!** is a distraction-blocking and scrolling limiter application engineered in Kotlin and Jetpack Compose for the **Infinix Hot 60i** running **Android 15 / XOS 15**.

Focus Hatif! allows you to set custom daily scrolling timers for YouTube Shorts and Instagram Reels. Once your daily allowance runs out (or immediately if set to Strict 0m), Focus Hatif! automatically backs you out of Shorts and Reels so you can regain your focus.

---

## ⏱️ Daily Scrolling Allowance & Auto-Exit

- **Custom Timers for Shorts & Reels**:
  - Independent limits for YouTube Shorts and Instagram Reels (Presets: Strict 0m, 5m, 15m, 30m, 45m, 60m, or any custom value via slider).
  - While within your daily allowance, enjoy Shorts & Reels freely.
  - Active in-session countdown tracks elapsed seconds in real-time.
- **Automatic Exit**:
  - The second your allowance for the day expires, Focus Hatif! smoothly executes the back action to return you to your feed or chat, and displays a motivating toast: *"Focus Hatif!: Limit reached — back to work!"*.
  - Strict 0m setting instantly auto-backs whenever Shorts or Reels are opened.
- **Strict Inbox & Direct Message Protection**:
  - Focus Hatif! strictly protects Instagram Direct Messages, chat threads, story viewers, and inbox navigation so you can message and view shared content without interference.
- **Reset Today's Time**:
  - One-tap button on the dashboard to reset today's counters whenever needed.

---

## 🔒 Peer Admin Lock (Anti-Relapse Protection)

To prevent impulsively turning off the blocker or expanding daily scrolling timers, Focus Hatif! includes a **Peer Admin Lock**:

1. **Setup by a Trusted Friend or Parent**:
   - Hand the phone to a peer. They set an admin password or PIN that you do not know.
   - The peer selects a **Security Question** and answers it.
   - A unique 10-character **Master Recovery Key** (`FH-XXXX-XXXX`) is generated and can be copied or screenshotted.
2. **Ironclad Protection**:
   - Disabling the Shorts/Reels shield, increasing daily scrolling limits, and resetting usage counters are locked behind the admin password.
3. **Forgotten Password Resolution**:
   - If the peer forgets the password, the app provides three recovery pathways:
     - **Option 1 (Security Question)**: The peer answers their secret question to reset access.
     - **Option 2 (Master Recovery Key)**: Entering the saved recovery key unlocks the app immediately.
     - **Option 3 (24-Hour Cooling-Off Emergency Reset)**: If both are forgotten, a 24-hour delayed reset countdown can be started. The lock remains active during the 24 hours to prevent impulsive relapse, and deactivates safely once the full 24 hours elapse.

---

## 📸 Instagram Stories & DMs Guarantee

- **Stories**: Full support for Instagram Stories (story viewer, segmented progress bar, profile headers, reply prompts, and close buttons) without accidental triggers.
- **Direct Messages**: Chat threads, message composer, voice notes, and shared links are completely unrestricted.

---

## 📱 Infinix Hot 60i / XOS 15 Configuration Guide

XOS 15 aggressive power-saving policies can sleep accessibility background services. Follow these four simple steps for uninterrupted protection:

1. **Set Battery to Unrestricted**:
   - Go to **Settings** > **Apps** > **Focus Hatif!** > **Battery** > Select **Unrestricted**.
2. **Allow Background Activity**:
   - Go to **Settings** > **Battery** > **Power Saving** > **App Battery Management** > **Focus Hatif!** > Enable **Allow background activity**.
3. **Enable Autostart**:
   - Go to **Settings** > **Apps** > **Focus Hatif!** > **Autostart** > Toggle **ON**.
4. **Lock in Recent Apps**:
   - Swipe up to view Recent Apps, find Focus Hatif!, and tap the **Lock** icon on the card so XOS never clears it from memory.

---

## 🔋 Battery Consumption FAQ

- **Running Automated JVM / Unit Tests**:
  - Running Gradle tests (`gradle :app:testDebugUnitTest`) happens entirely inside your build machine or local computer, consuming **0% phone battery**.
- **Running & Testing the App on your Device**:
  - Focus Hatif! uses Android's native event-driven `AccessibilityService`. It sleeps when you are in other apps and only checks view hierarchies when you open YouTube or Instagram.
  - No background polling, no GPS, no Bluetooth, and no network connections.
  - Typical daily battery usage is **virtually imperceptible (under 0.5% – 1% over a full day)**.

---

## 🛠️ Build Instructions

### Command Line
To compile and generate the debug APK:
```bash
gradle assembleDebug
```
The resulting APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```
