package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.AppSettings
import com.example.ThemeMode

val XboxDarkColorScheme = darkColorScheme(
    primary = XboxNeonGreen,
    onPrimary = XboxBlack,
    primaryContainer = XboxDarkGreen,
    onPrimaryContainer = XboxNeonGreen,
    secondary = XboxGreen,
    onSecondary = XboxTextPrimary,
    secondaryContainer = XboxDarkSurfaceVariant,
    onSecondaryContainer = XboxLightGreen,
    tertiary = XboxLightGreen,
    onTertiary = XboxBlack,
    background = XboxBlack,
    onBackground = XboxTextPrimary,
    surface = XboxDarkSurface,
    onSurface = XboxTextPrimary,
    surfaceVariant = XboxDarkSurfaceVariant,
    onSurfaceVariant = XboxTextSecondary,
    outline = XboxOutline,
    outlineVariant = XboxOutlineHighlight,
    error = StatusErrorRed,
    onError = XboxBlack
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = XboxGreen,
    onSecondary = LightOnPrimary,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightPrimary,
    tertiary = XboxLightGreen,
    onTertiary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutlineHighlight,
    error = StatusErrorRed,
    onError = LightOnPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val settingsState by AppSettings.settingsState.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (settingsState.themeMode) {
        ThemeMode.XBOX_DARK -> XboxDarkColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.SYSTEM -> if (isSystemDark) XboxDarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
