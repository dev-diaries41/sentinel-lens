package com.fpf.sentinellens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorPalette = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    primaryContainer = Teal700.copy(alpha = 0.5f),
    onPrimaryContainer = Color.Black
)

val DarkColorPalette = darkColorScheme(
    primary = Teal500,
    onPrimary = Color.Black,
    primaryContainer = Teal500.copy(alpha = 0.5f),
    onPrimaryContainer = Color.White
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
