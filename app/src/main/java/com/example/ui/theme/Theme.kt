package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val XboxDarkColorScheme = darkColorScheme(
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek Xbox dark theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = XboxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
