package com.fpf.sentinellens.ui.theme

import android.hardware.lights.Light
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light mode color scheme
private val LightColorPalette = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    secondary = Silver200,
    onSecondary = Color.White,
    background = LightBackgroundColor,
    surface = LightBackgroundColor,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceContainer = LightGray
)

// Dark mode color scheme
private val DarkColorPalette = darkColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    secondary = Silver200,
    onSecondary = Color.White,
    background = DarkBackgroundColor,
    surface = DarkBackgroundColor,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceContainer = DarkGray
)

@Composable
fun MyAppTheme(
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
