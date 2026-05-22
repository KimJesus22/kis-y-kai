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
    darkTheme: Boolean = false, // El tema de alta densidad por defecto es el tema claro/lavanda estándar
    dynamicColor: Boolean = false, // Deshabilitar el color dinámico del sistema para mantener la identidad visual del restaurante
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KisKeiColorScheme,
        typography = Typography,
        content = content
    )
}
