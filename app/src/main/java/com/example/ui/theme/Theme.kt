package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KisKeiColorScheme = lightColorScheme(
    primary = NeonPink,
    onPrimary = Color.White,
    secondary = HoneyGold,
    onSecondary = Color.White,
    tertiary = Color(0xFF625F4C),
    background = CharcoalDark,
    onBackground = TextLight,
    surface = ObsidianCard,
    onSurface = TextLight,
    surfaceVariant = Color(0xFFF0EDED),
    onSurfaceVariant = TextMuted,
    outline = CardBorder,
    error = Color(0xFFBA1A1A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // High Density theme defaults to standard light/lavender theme
    dynamicColor: Boolean = false, // Disable system dynamic color so the custom restaurant identity is locked
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KisKeiColorScheme,
        typography = Typography,
        content = content
    )
}
