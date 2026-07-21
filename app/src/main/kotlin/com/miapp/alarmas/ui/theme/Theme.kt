package com.miapp.alarmas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.miapp.alarmas.data.ThemeMode

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueLightCard,
    background = SurfaceLight,
    surface = SurfaceLight,
    onBackground = BlackText,
    onSurface = BlackText
)

private val DarkColors = darkColorScheme(
    primary = BluePrimary,
    secondary = CardDark,
    background = SurfaceDark,
    surface = CardDark,
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED)
)

@Composable
fun AlarmAppTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val useDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
