package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudyHubDarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = RoyalBlueDark,
    onPrimaryContainer = RoyalBlueLight,
    secondary = DarkTextMuted,
    onSecondary = DarkMidnightBg,
    background = DarkMidnightBg,
    onBackground = DarkTextWhite,
    surface = DarkMidnightSurface,
    onSurface = DarkTextWhite,
    surfaceVariant = DarkMidnightCard,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

private val StudyHubLightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = RoyalBlueLight,
    onPrimaryContainer = RoyalBlueDark,
    secondary = SlateMuted,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = NavyText,
    surface = CardWhite,
    onSurface = NavyText,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = NavySlate,
    outline = CardBorder,
    outlineVariant = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudyHubDarkColorScheme else StudyHubLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
