package com.example

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object FocusPreferences {
    private const val PREFS_NAME = "focushatif_prefs"
    private const val KEY_BLOCKING_ENABLED = "key_blocking_enabled"
    private const val KEY_YT_LIMIT_MINUTES = "key_yt_limit_minutes"
    private const val KEY_IG_LIMIT_MINUTES = "key_ig_limit_minutes"
    private const val KEY_YT_USED_SECONDS = "key_yt_used_seconds"
    private const val KEY_IG_USED_SECONDS = "key_ig_used_seconds"
    private const val KEY_TODAY_COUNT = "key_today_count"
    private const val KEY_TOTAL_COUNT = "key_total_count"
    private const val KEY_LAST_DATE = "key_last_date"

    // Admin Lock preferences
    private const val KEY_ADMIN_LOCK_ENABLED = "key_admin_lock_enabled"
    private const val KEY_ADMIN_PIN_HASH = "key_admin_pin_hash"
    private const val KEY_ADMIN_QUESTION = "key_admin_question"
    private const val KEY_ADMIN_ANSWER_HASH = "key_admin_answer_hash"
    private const val KEY_ADMIN_RECOVERY_KEY = "key_admin_recovery_key"
    private const val KEY_ADMIN_EMERGENCY_RESET_TIME = "key_admin_emergency_reset_time"

    // 24 hours in milliseconds for emergency recovery
    const val EMERGENCY_RESET_DURATION_MS = 24 * 60 * 60 * 1000L

    data class FocusStats(
        val isBlockingEnabled: Boolean = true,
        val youtubeLimitMinutes: Int = 0,    // 0 = Strict Block immediately
        val instagramLimitMinutes: Int = 0,  // 0 = Strict Block immediately
        val youtubeUsedSeconds: Int = 0,
        val instagramUsedSeconds: Int = 0,
        val todayBlocks: Int = 0,
        val totalBlocks: Int = 0,
        val isAdminLockEnabled: Boolean = false,
        val securityQuestion: String = "",
        val recoveryKey: String = "",
        val emergencyResetStartTime: Long = 0L
    )

    private val _statsFlow = MutableStateFlow(FocusStats())
    val statsFlow: StateFlow<FocusStats> = _statsFlow.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun hashString(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.trim().lowercase().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun init(context: Context) {
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""

        var todayCount = prefs.getInt(KEY_TODAY_COUNT, 0)
        var ytUsedSec = prefs.getInt(KEY_YT_USED_SECONDS, 0)
        var igUsedSec = prefs.getInt(KEY_IG_USED_SECONDS, 0)

        if (today != lastDate) {
            todayCount = 0
            ytUsedSec = 0
            igUsedSec = 0
            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_TODAY_COUNT, 0)
                .putInt(KEY_YT_USED_SECONDS, 0)
                .putInt(KEY_IG_USED_SECONDS, 0)
                .apply()
        }

        val isEnabled = prefs.getBoolean(KEY_BLOCKING_ENABLED, true)
        val ytLimit = prefs.getInt(KEY_YT_LIMIT_MINUTES, 0)
        val igLimit = prefs.getInt(KEY_IG_LIMIT_MINUTES, 0)
        val totalCount = prefs.getInt(KEY_TOTAL_COUNT, 0)

        // Admin lock initialization
        var isAdminLocked = prefs.getBoolean(KEY_ADMIN_LOCK_ENABLED, false)
        val question = prefs.getString(KEY_ADMIN_QUESTION, "") ?: ""
        val recoveryKey = prefs.getString(KEY_ADMIN_RECOVERY_KEY, "") ?: ""
        val emergencyResetTime = prefs.getLong(KEY_ADMIN_EMERGENCY_RESET_TIME, 0L)

        // Check if emergency 24-hour reset has matured
        if (isAdminLocked && emergencyResetTime > 0L) {
            val elapsed = System.currentTimeMillis() - emergencyResetTime
            if (elapsed >= EMERGENCY_RESET_DURATION_MS) {
                // Auto-clear admin lock after 24-hour cooling off period
                prefs.edit()
                    .putBoolean(KEY_ADMIN_LOCK_ENABLED, false)
                    .remove(KEY_ADMIN_PIN_HASH)
                    .remove(KEY_ADMIN_QUESTION)
                    .remove(KEY_ADMIN_ANSWER_HASH)
                    .remove(KEY_ADMIN_RECOVERY_KEY)
                    .remove(KEY_ADMIN_EMERGENCY_RESET_TIME)
                    .apply()
                isAdminLocked = false
            }
        }

        _statsFlow.value = FocusStats(
            isBlockingEnabled = isEnabled,
            youtubeLimitMinutes = ytLimit,
            instagramLimitMinutes = igLimit,
            youtubeUsedSeconds = ytUsedSec,
            instagramUsedSeconds = igUsedSec,
            todayBlocks = todayCount,
            totalBlocks = totalCount,
            isAdminLockEnabled = isAdminLocked,
            securityQuestion = question,
            recoveryKey = recoveryKey,
            emergencyResetStartTime = emergencyResetTime
        )
    }

    fun isBlockingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BLOCKING_ENABLED, true)
    }

    fun setBlockingEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BLOCKING_ENABLED, enabled).apply()
        _statsFlow.value = _statsFlow.value.copy(isBlockingEnabled = enabled)
    }

    fun setYoutubeLimitMinutes(context: Context, minutes: Int) {
        val clamped = minutes.coerceAtLeast(0)
        getPrefs(context).edit().putInt(KEY_YT_LIMIT_MINUTES, clamped).apply()
        _statsFlow.value = _statsFlow.value.copy(youtubeLimitMinutes = clamped)
    }

    fun setInstagramLimitMinutes(context: Context, minutes: Int) {
        val clamped = minutes.coerceAtLeast(0)
        getPrefs(context).edit().putInt(KEY_IG_LIMIT_MINUTES, clamped).apply()
        _statsFlow.value = _statsFlow.value.copy(instagramLimitMinutes = clamped)
    }

    fun isLimitReached(context: Context, isYoutube: Boolean): Boolean {
        val stats = _statsFlow.value
        val limitMinutes = if (isYoutube) stats.youtubeLimitMinutes else stats.instagramLimitMinutes
        if (limitMinutes <= 0) {
            // 0 means strict block immediately
            return true
        }
        val usedSeconds = if (isYoutube) stats.youtubeUsedSeconds else stats.instagramUsedSeconds
        return usedSeconds >= limitMinutes * 60
    }

    @Synchronized
    fun addSecondsUsed(context: Context, isYoutube: Boolean, secondsToAdd: Int) {
        if (secondsToAdd <= 0) return
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""

        var ytUsed = prefs.getInt(KEY_YT_USED_SECONDS, 0)
        var igUsed = prefs.getInt(KEY_IG_USED_SECONDS, 0)
        var todayCount = prefs.getInt(KEY_TODAY_COUNT, 0)

        if (today != lastDate) {
            ytUsed = 0
            igUsed = 0
            todayCount = 0
        }

        if (isYoutube) {
            ytUsed += secondsToAdd
        } else {
            igUsed += secondsToAdd
        }

        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_TODAY_COUNT, todayCount)
            .putInt(KEY_YT_USED_SECONDS, ytUsed)
            .putInt(KEY_IG_USED_SECONDS, igUsed)
            .apply()

        _statsFlow.value = _statsFlow.value.copy(
            youtubeUsedSeconds = ytUsed,
            instagramUsedSeconds = igUsed,
            todayBlocks = todayCount
        )
    }

    @Synchronized
    fun recordBlock(context: Context) {
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        var todayCount = prefs.getInt(KEY_TODAY_COUNT, 0)
        var ytUsed = prefs.getInt(KEY_YT_USED_SECONDS, 0)
        var igUsed = prefs.getInt(KEY_IG_USED_SECONDS, 0)

        if (today != lastDate) {
            todayCount = 0
            ytUsed = 0
            igUsed = 0
        }
        todayCount += 1
        val totalCount = prefs.getInt(KEY_TOTAL_COUNT, 0) + 1

        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_TODAY_COUNT, todayCount)
            .putInt(KEY_YT_USED_SECONDS, ytUsed)
            .putInt(KEY_IG_USED_SECONDS, igUsed)
            .putInt(KEY_TOTAL_COUNT, totalCount)
            .apply()

        _statsFlow.value = _statsFlow.value.copy(
            todayBlocks = todayCount,
            totalBlocks = totalCount
        )
    }

    @Synchronized
    fun resetTodayUsage(context: Context) {
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_YT_USED_SECONDS, 0)
            .putInt(KEY_IG_USED_SECONDS, 0)
            .putInt(KEY_TODAY_COUNT, 0)
            .apply()

        _statsFlow.value = _statsFlow.value.copy(
            youtubeUsedSeconds = 0,
            instagramUsedSeconds = 0,
            todayBlocks = 0
        )
    }

    // ==========================================
    // ADMIN LOCK (PEER / PARENT LOCK) METHODS
    // ==========================================

    fun generateNewRecoveryKey(): String {
        val raw = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.US)
        return "FH-${raw.substring(0, 4)}-${raw.substring(4, 8)}"
    }

    fun enableAdminLock(
        context: Context,
        pin: String,
        question: String,
        answer: String,
        recoveryKey: String
    ) {
        val pinHash = hashString(pin)
        val answerHash = hashString(answer)

        getPrefs(context).edit()
            .putBoolean(KEY_ADMIN_LOCK_ENABLED, true)
            .putString(KEY_ADMIN_PIN_HASH, pinHash)
            .putString(KEY_ADMIN_QUESTION, question.trim())
            .putString(KEY_ADMIN_ANSWER_HASH, answerHash)
            .putString(KEY_ADMIN_RECOVERY_KEY, recoveryKey.trim())
            .remove(KEY_ADMIN_EMERGENCY_RESET_TIME)
            .apply()

        _statsFlow.value = _statsFlow.value.copy(
            isAdminLockEnabled = true,
            securityQuestion = question.trim(),
            recoveryKey = recoveryKey.trim(),
            emergencyResetStartTime = 0L
        )
    }

    fun disableAdminLock(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_ADMIN_LOCK_ENABLED, false)
            .remove(KEY_ADMIN_PIN_HASH)
            .remove(KEY_ADMIN_QUESTION)
            .remove(KEY_ADMIN_ANSWER_HASH)
            .remove(KEY_ADMIN_RECOVERY_KEY)
            .remove(KEY_ADMIN_EMERGENCY_RESET_TIME)
            .apply()

        _statsFlow.value = _statsFlow.value.copy(
            isAdminLockEnabled = false,
            securityQuestion = "",
            recoveryKey = "",
            emergencyResetStartTime = 0L
        )
    }

    fun verifyAdminPin(context: Context, pin: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_ADMIN_PIN_HASH, "") ?: ""
        if (storedHash.isEmpty()) return false
        return hashString(pin) == storedHash
    }

    fun verifySecurityAnswer(context: Context, answer: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_ADMIN_ANSWER_HASH, "") ?: ""
        if (storedHash.isEmpty()) return false
        return hashString(answer) == storedHash
    }

    fun verifyRecoveryKey(context: Context, key: String): Boolean {
        val storedKey = getPrefs(context).getString(KEY_ADMIN_RECOVERY_KEY, "") ?: ""
        if (storedKey.isEmpty()) return false
        val normalizedInput = key.trim().replace("-", "").uppercase(Locale.US)
        val normalizedStored = storedKey.trim().replace("-", "").uppercase(Locale.US)
        return normalizedInput == normalizedStored
    }

    fun startEmergencyReset(context: Context) {
        val now = System.currentTimeMillis()
        getPrefs(context).edit()
            .putLong(KEY_ADMIN_EMERGENCY_RESET_TIME, now)
            .apply()
        _statsFlow.value = _statsFlow.value.copy(emergencyResetStartTime = now)
    }

    fun cancelEmergencyReset(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_ADMIN_EMERGENCY_RESET_TIME)
            .apply()
        _statsFlow.value = _statsFlow.value.copy(emergencyResetStartTime = 0L)
    }

    fun completeEmergencyResetIfReady(context: Context): Boolean {
        val prefs = getPrefs(context)
        val startTime = prefs.getLong(KEY_ADMIN_EMERGENCY_RESET_TIME, 0L)
        if (startTime <= 0L) return false

        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed >= EMERGENCY_RESET_DURATION_MS) {
            disableAdminLock(context)
            return true
        }
        return false
    }

    fun getEmergencyResetRemainingHours(context: Context): Int {
        val startTime = getPrefs(context).getLong(KEY_ADMIN_EMERGENCY_RESET_TIME, 0L)
        if (startTime <= 0L) return 0
        val elapsed = System.currentTimeMillis() - startTime
        val remainingMs = (EMERGENCY_RESET_DURATION_MS - elapsed).coerceAtLeast(0L)
        return ((remainingMs / (1000 * 60 * 60)) + 1).toInt().coerceAtMost(24)
    }
}
