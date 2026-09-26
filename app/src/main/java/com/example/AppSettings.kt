package com.example

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    XBOX_DARK,
    LIGHT,
    SYSTEM
}

data class AppSettingsState(
    val themeMode: ThemeMode = ThemeMode.XBOX_DARK,
    val featureDiaryEnabled: Boolean = true,
    val featureReflectionEnabled: Boolean = true,
    val featureGradesEnabled: Boolean = true,
    val featurePomodoroEnabled: Boolean = true,
    val featureShortsBlockerEnabled: Boolean = true,
    val featureAdminLockEnabled: Boolean = true,
    val showAdvancedSettings: Boolean = false,
    val isFirstLaunchComplete: Boolean = false
)

object AppSettings {
    private const val PREFS_NAME = "app_settings_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FEATURE_DIARY = "feature_diary"
    private const val KEY_FEATURE_REFLECTION = "feature_reflection"
    private const val KEY_FEATURE_GRADES = "feature_grades"
    private const val KEY_FEATURE_POMODORO = "feature_pomodoro"
    private const val KEY_FEATURE_SHORTS_BLOCKER = "feature_shorts_blocker"
    private const val KEY_FEATURE_ADMIN_LOCK = "feature_admin_lock"
    private const val KEY_SHOW_ADVANCED_SETTINGS = "show_advanced_settings"
    private const val KEY_FIRST_LAUNCH_COMPLETE = "is_first_launch_complete"

    private val _settingsState = MutableStateFlow(AppSettingsState())
    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        val sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sharedPrefs

        val themeModeStr = sharedPrefs.getString(KEY_THEME_MODE, ThemeMode.XBOX_DARK.name) ?: ThemeMode.XBOX_DARK.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            ThemeMode.XBOX_DARK
        }

        _settingsState.value = AppSettingsState(
            themeMode = themeMode,
            featureDiaryEnabled = sharedPrefs.getBoolean(KEY_FEATURE_DIARY, true),
            featureReflectionEnabled = sharedPrefs.getBoolean(KEY_FEATURE_REFLECTION, true),
            featureGradesEnabled = sharedPrefs.getBoolean(KEY_FEATURE_GRADES, true),
            featurePomodoroEnabled = sharedPrefs.getBoolean(KEY_FEATURE_POMODORO, true),
            featureShortsBlockerEnabled = sharedPrefs.getBoolean(KEY_FEATURE_SHORTS_BLOCKER, true),
            featureAdminLockEnabled = sharedPrefs.getBoolean(KEY_FEATURE_ADMIN_LOCK, true),
            showAdvancedSettings = sharedPrefs.getBoolean(KEY_SHOW_ADVANCED_SETTINGS, false),
            isFirstLaunchComplete = sharedPrefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETE, false)
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
        _settingsState.value = _settingsState.value.copy(themeMode = mode)
    }

    fun setFeatureDiaryEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FEATURE_DIARY, enabled)?.apply()
        _settingsState.value = _settingsState.value.copy(featureDiaryEnabled = enabled)
    }

    fun setFeatureReflectionEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FEATURE_REFLECTION, enabled)?.apply()
        _settingsState.value = _settingsState.value.copy(featureReflectionEnabled = enabled)
    }

    fun setFeatureGradesEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FEATURE_GRADES, enabled)?.apply()
        _settingsState.value = _settingsState.value.copy(featureGradesEnabled = enabled)
    }

    fun setFeaturePomodoroEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FEATURE_POMODORO, enabled)?.apply()
        _settingsState.value = _settingsState.value.copy(featurePomodoroEnabled = enabled)
    }

    fun setFeatureShortsBlockerEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FEATURE_SHORTS_BLOCKER, enabled)?.apply()
        _settingsState.value = _settingsState.value.copy(featureShortsBlockerEnabled = enabled)
    }

    fun setFeatureAdminLockEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FEATURE_ADMIN_LOCK, enabled)?.apply()
        _settingsState.value = _settingsState.value.copy(featureAdminLockEnabled = enabled)
    }

    fun setShowAdvancedSettings(show: Boolean) {
        prefs?.edit()?.putBoolean(KEY_SHOW_ADVANCED_SETTINGS, show)?.apply()
        _settingsState.value = _settingsState.value.copy(showAdvancedSettings = show)
    }

    fun setFirstLaunchComplete(complete: Boolean) {
        prefs?.edit()?.putBoolean(KEY_FIRST_LAUNCH_COMPLETE, complete)?.apply()
        _settingsState.value = _settingsState.value.copy(isFirstLaunchComplete = complete)
    }
}
