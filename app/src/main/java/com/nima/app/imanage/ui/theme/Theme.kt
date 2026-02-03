package com.nima.app.imanage.ui.theme

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

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0F5C5A),
    primaryContainer = Color(0xFF073B3A),
    secondary = Color(0xFFF4C27A),
    background = Color(0xFF0B1F1E),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF1A1A1A),
    surface = Color(0xFF1C1F24),
    surfaceVariant = Color(0xFF2A2E35),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED),
    errorContainer = Color(0xFFB0B6C3),
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F5C5A),
    primaryContainer = Color(0xFF0A4A48),
    secondary = Color(0xFFF4C27A),
    background = Color(0xFF0F5C5A),
    onPrimary = Color(0xFFF1F1F1),
    onSecondary = Color(0xFF202124),
    surface = Color(0xFF2F3136),
    surfaceVariant = Color(0xFF4B4F5C),
    onBackground = Color(0xFFF1F1F1),
    onSurface = Color(0xFFF1F1F1),
    errorContainer = Color(0xFF8A8F9C),
    scrim = Color(0xFF202124)
)

@Composable
fun IManageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
        typography = getVazirTypography(1f),
        content = content
    )
}