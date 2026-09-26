package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class ShortsReelsBlockerService : AccessibilityService() {

    companion object {
        const val PACKAGE_YOUTUBE = "com.google.android.youtube"
        const val PACKAGE_INSTAGRAM = "com.instagram.android"

        // Cooldown between back operations to allow fragment transition to complete
        private const val COOLDOWN_MS = 2500L
        private const val TIMER_TICK_INTERVAL_MS = 2000L // 2s tick interval to save CPU & battery

        @Volatile
        var isServiceConnected: Boolean = false
            private set

        fun isServiceRunning(context: Context): Boolean {
            if (isServiceConnected) return true
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager ?: return false
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            return enabledServices.any {
                it.resolveInfo.serviceInfo.packageName == context.packageName &&
                        it.resolveInfo.serviceInfo.name == ShortsReelsBlockerService::class.java.name
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastBlockTimestamp = 0L

    // In-memory active session tracking for daily scroll timer
    private var activePlatform: String? = null
    private var activeSessionLastTickMs = 0L

    private val timerTickerRunnable = object : Runnable {
        override fun run() {
            val platform = activePlatform
            if (platform == null) return

            val now = SystemClock.elapsedRealtime()
            val elapsedSec = ((now - activeSessionLastTickMs) / 1000L).toInt()

            if (elapsedSec >= 1) {
                val isYt = platform == PACKAGE_YOUTUBE
                FocusPreferences.addSecondsUsed(applicationContext, isYt, elapsedSec)
                activeSessionLastTickMs = now

                // Check if the daily limit was just hit
                if (FocusPreferences.isLimitReached(applicationContext, isYt)) {
                    stopActiveTrackingSession()
                    if (now - lastBlockTimestamp >= COOLDOWN_MS) {
                        executeBlockAction(now)
                    }
                    return
                }
            }

            // Continue ticking while actively scrolling Shorts/Reels
            mainHandler.postDelayed(this, TIMER_TICK_INTERVAL_MS)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.packageNames = arrayOf(PACKAGE_YOUTUBE, PACKAGE_INSTAGRAM)
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 150
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 1. Check if master switch is enabled
        if (!FocusPreferences.isBlockingEnabled(applicationContext)) {
            stopActiveTrackingSession()
            return
        }

        // 2. Package verification
        val packageName = event.packageName?.toString() ?: return
        if (packageName != PACKAGE_YOUTUBE && packageName != PACKAGE_INSTAGRAM) {
            stopActiveTrackingSession()
            return
        }

        val now = SystemClock.elapsedRealtime()

        // 3. Prevent handling during transition cooldown to avoid re-triggering while backing out
        if (now - lastBlockTimestamp < COOLDOWN_MS) {
            return
        }

        // Ensure we obtain the true window root
        var rootNode = rootInActiveWindow
        if (rootNode == null && event.source != null) {
            var current: AccessibilityNodeInfo? = event.source
            while (current?.parent != null) {
                current = current.parent
            }
            rootNode = current
        }
        if (rootNode == null) return

        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        val isYoutube = packageName == PACKAGE_YOUTUBE
        val isShortsOrReels = if (isYoutube) {
            isYouTubeShortsActive(rootNode, screenHeight, screenWidth)
        } else {
            isInstagramReelsActive(rootNode, screenHeight, screenWidth)
        }

        if (isShortsOrReels) {
            // Check if user's daily timer allowance is reached or if limit is 0 (strict block)
            val limitReached = FocusPreferences.isLimitReached(applicationContext, isYoutube)

            if (limitReached) {
                stopActiveTrackingSession()
                executeBlockAction(now)
            } else {
                // User has daily allowance remaining: track their scrolling session
                startOrContinueTrackingSession(packageName, now)
            }
        } else {
            // User is on a normal screen (feed, inbox, search, normal videos, stories)
            stopActiveTrackingSession()
        }
    }

    private fun startOrContinueTrackingSession(packageName: String, now: Long) {
        if (activePlatform != packageName) {
            stopActiveTrackingSession()
            activePlatform = packageName
            activeSessionLastTickMs = now
            mainHandler.postDelayed(timerTickerRunnable, TIMER_TICK_INTERVAL_MS)
        }
    }

    private fun stopActiveTrackingSession() {
        val platform = activePlatform ?: return
        mainHandler.removeCallbacks(timerTickerRunnable)

        val now = SystemClock.elapsedRealtime()
        val remainingSec = ((now - activeSessionLastTickMs) / 1000L).toInt()
        if (remainingSec >= 1) {
            FocusPreferences.addSecondsUsed(applicationContext, platform == PACKAGE_YOUTUBE, remainingSec)
        }

        activePlatform = null
        activeSessionLastTickMs = 0L
    }

    /**
     * Detects YouTube Shorts full-screen player.
     * Preserves normal YouTube videos, search, feed, and subscriptions.
     */
    private fun isYouTubeShortsActive(root: AccessibilityNodeInfo, screenHeight: Int, screenWidth: Int): Boolean {
        // Exclude normal video player screen
        val normalVideoIds = arrayOf(
            "com.google.android.youtube:id/watch_while_layout",
            "com.google.android.youtube:id/player_fragment_container",
            "com.google.android.youtube:id/player_view",
            "com.google.android.youtube:id/watch_panel"
        )
        for (id in normalVideoIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            if (!list.isNullOrEmpty()) {
                val rect = Rect()
                list[0].getBoundsInScreen(rect)
                // If normal video watch panel is visible, not Shorts
                if (rect.height() > screenHeight * 0.3) {
                    return false
                }
            }
        }

        val queue = java.util.ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visitedCount = 0
        val maxVisits = 60
        val tempRect = Rect()
        var hasShortsSignature = false

        while (queue.isNotEmpty() && visitedCount < maxVisits) {
            val current = queue.removeFirst()
            visitedCount++

            val viewId = current.viewIdResourceName?.lowercase() ?: ""

            // Exclude search or feed shelves
            if (viewId.contains("search_box") || viewId.contains("search_edit_text")) {
                return false
            }

            val isShortsId = viewId.contains("shorts_player") ||
                    viewId.contains("reel_recycler") ||
                    viewId.contains("reel_watch_fragment") ||
                    viewId.contains("shorts_container") ||
                    viewId.contains("reel_player")

            if (isShortsId) {
                current.getBoundsInScreen(tempRect)
                // Must be full-screen vertical player
                if (tempRect.height() >= screenHeight * 0.75 && tempRect.width() >= screenWidth * 0.80) {
                    hasShortsSignature = true
                }
            }

            if (viewId.contains("reel_sound_pivot_button") || viewId.contains("reel_remix_button")) {
                hasShortsSignature = true
            }

            for (i in 0 until current.childCount) {
                val child = current.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }

        return hasShortsSignature
    }

    /**
     * Detects Instagram Reels viewer.
     * STRICTLY PRESERVES:
     * 1. Stories (Story viewer, segmented progress bar, close button, reply prompt, highlights)
     * 2. Inbox / Direct Messages (chat threads, message list, voice notes, shared clips in DM)
     * 3. Main feed, Explore grid, Search, and User Profile
     */
    private fun isInstagramReelsActive(root: AccessibilityNodeInfo, screenHeight: Int, screenWidth: Int): Boolean {
        // ----------------------------------------------------
        // Step 1: Instant Stories Check (ABSOLUTE OVERRIDE)
        // ----------------------------------------------------
        val storyTexts = arrayOf(
            "reply to", "send message", "send a message", "seen by",
            "add to story", "your story", "story by", "share story",
            "story highlights", "highlights", "send to", "quick reaction"
        )
        for (st in storyTexts) {
            val nodes = root.findAccessibilityNodeInfosByText(st)
            if (!nodes.isNullOrEmpty()) {
                for (n in nodes) {
                    if (n.isVisibleToUser) {
                        return false // Found visible story prompt, definitely a story!
                    }
                }
            }
        }

        val storyViewIds = arrayOf(
            "com.instagram.android:id/reel_viewer_container",
            "com.instagram.android:id/reel_viewer_title",
            "com.instagram.android:id/reel_viewer_close_button",
            "com.instagram.android:id/segmented_progress_bar",
            "com.instagram.android:id/stories_progress_bar",
            "com.instagram.android:id/story_progress",
            "com.instagram.android:id/stories_tray",
            "com.instagram.android:id/stories_header",
            "com.instagram.android:id/reel_item_wrapper",
            "com.instagram.android:id/archive_stories",
            "com.instagram.android:id/viewer_media_view_pager",
            "com.instagram.android:id/toolbar_container"
        )
        for (id in storyViewIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            if (!list.isNullOrEmpty()) {
                for (n in list) {
                    if (n.isVisibleToUser) {
                        return false
                    }
                }
            }
        }

        // ----------------------------------------------------
        // Step 2: Instant Direct Messages / Inbox Check
        // ----------------------------------------------------
        val directViewIds = arrayOf(
            "com.instagram.android:id/direct_message_list",
            "com.instagram.android:id/row_thread_title",
            "com.instagram.android:id/thread_title",
            "com.instagram.android:id/direct_text_edit_text",
            "com.instagram.android:id/message_composer",
            "com.instagram.android:id/action_bar_search_edit_text"
        )
        for (id in directViewIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            if (!list.isNullOrEmpty()) {
                return false
            }
        }

        // ----------------------------------------------------
        // Step 3: Main Feed / Explore Grid / Profile Check
        // ----------------------------------------------------
        val feedViewIds = arrayOf(
            "com.instagram.android:id/main_feed",
            "com.instagram.android:id/feed_recycler",
            "com.instagram.android:id/explore_recycler",
            "com.instagram.android:id/profile_header",
            "com.instagram.android:id/profile_tabs",
            "com.instagram.android:id/profile_pager"
        )
        for (id in feedViewIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            if (!list.isNullOrEmpty()) {
                return false
            }
        }

        // ----------------------------------------------------
        // Step 4: BFS Traversal for Genuine Reels Viewer
        // ----------------------------------------------------
        val queue = java.util.ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visitedCount = 0
        val maxVisits = 150
        val tempRect = Rect()

        var hasVisibleClipsContainer = false
        var hasReelsControls = false

        while (queue.isNotEmpty() && visitedCount < maxVisits) {
            val current = queue.removeFirst()
            visitedCount++

            if (!current.isVisibleToUser) {
                // Ignore background or hidden views
                continue
            }

            val viewId = current.viewIdResourceName?.lowercase() ?: ""
            val desc = current.contentDescription?.toString()?.lowercase() ?: ""
            val text = current.text?.toString()?.lowercase() ?: ""

            // If user is currently typing anywhere in an editable field, never block
            if (current.isEditable) {
                return false
            }

            // If any node is a Story reply field or story prompt
            if (text.startsWith("reply to") || text.startsWith("send message") || desc.contains("story")) {
                return false
            }

            // Top Close button (X) check: Reels has no close button, Stories always have a close button
            if ((viewId.contains("close") || desc == "close" || text == "close")) {
                current.getBoundsInScreen(tempRect)
                if (tempRect.top < screenHeight * 0.22) {
                    // Close button near the top of the screen = Story, Dialog, or Composer, NOT Reels!
                    return false
                }
            }

            // Check for genuine Clips / Reels container
            if (viewId.contains("clips_viewer_view_pager") ||
                viewId.contains("clips_video_container") ||
                viewId.contains("clips_swipe_refresh_layout") ||
                viewId.contains("clips_viewer_container")
            ) {
                current.getBoundsInScreen(tempRect)
                if (tempRect.height() >= screenHeight * 0.70 && tempRect.width() >= screenWidth * 0.75) {
                    hasVisibleClipsContainer = true
                }
            }

            // Reels action pills and controls
            if (viewId.contains("clips_audio_pill") ||
                viewId.contains("clips_sound_button") ||
                viewId.contains("clips_remix_button") ||
                desc.contains("reel by") ||
                desc.contains("reels audio")
            ) {
                hasReelsControls = true
            }

            for (i in 0 until current.childCount) {
                val child = current.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }

        // Must have BOTH the large vertical Clips container AND genuine Reels controls
        return hasVisibleClipsContainer && hasReelsControls
    }

    /**
     * Executes back navigation to return the user to their feed, inbox, or previous screen.
     * NEVER triggers GLOBAL_ACTION_HOME to avoid kicking the user out of Instagram/YouTube.
     */
    private fun executeBlockAction(now: Long) {
        lastBlockTimestamp = now

        // Strictly use GLOBAL_ACTION_BACK to exit Reels/Shorts back to Feed or Inbox
        performGlobalAction(GLOBAL_ACTION_BACK)

        mainHandler.post {
            Toast.makeText(
                applicationContext,
                getString(R.string.toast_blocked),
                Toast.LENGTH_SHORT
            ).show()
        }

        FocusPreferences.recordBlock(applicationContext)
    }

    override fun onInterrupt() {
        stopActiveTrackingSession()
        isServiceConnected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopActiveTrackingSession()
        isServiceConnected = false
    }
}
