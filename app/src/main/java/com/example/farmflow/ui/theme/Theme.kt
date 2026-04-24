package com.example.farmflow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenLight,
    onPrimary = Color.White,
    primaryContainer = FarmGreenDark,
    onPrimaryContainer = FarmGreenSurface,
    secondary = FarmOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = FarmOrange,
    tertiary = FarmBrownLight,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FarmGreen,
    onPrimary = Color.White,
    primaryContainer = FarmGreenSurface,
    onPrimaryContainer = FarmGreenOnSurface,
    secondary = FarmOrange,
    onSecondary = Color.White,
    secondaryContainer = FarmOrangeSurface,
    onSecondaryContainer = Color(0xFF3E2700),
    tertiary = FarmBrown,
    onTertiary = Color.White,
    background = SurfaceLight,
    surface = CardWhite,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0F0F0),
    error = FarmRed
)

@Composable
fun FarmFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}