package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MidwifePrimaryDark,
    secondary = MidwifeSecondaryDark,
    tertiary = MidwifeTertiaryDark,
    background = MidwifeBackgroundDark,
    surface = MidwifeSurfaceDark,
    onPrimary = Color(0xFF1E1415),
    onSecondary = Color(0xFF1E1415),
    onBackground = Color(0xFFFDE8EB),
    onSurface = Color(0xFFFDE8EB)
)

private val LightColorScheme = lightColorScheme(
    primary = MidwifePrimary,
    secondary = MidwifeSecondary,
    tertiary = MidwifeTertiary,
    background = MidwifeBackground,
    surface = MidwifeSurface,
    onPrimary = MidwifeOnPrimary,
    onSecondary = MidwifeOnSecondary,
    onBackground = MidwifeOnBackground,
    onSurface = MidwifeOnSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
