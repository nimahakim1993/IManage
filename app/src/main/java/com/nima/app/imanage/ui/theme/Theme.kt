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
import com.nima.app.imanage.util.ThemeManager

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

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF156E6B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF076963),
    onPrimaryContainer = Color(0xFF003735),
    secondary = Color(0xFFCAD595),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFBE3B8),
    onSecondaryContainer = Color(0xFF2D1F08),
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF1B1D1C),
    surface = Color(0xFFFCFEFD),
    onSurface = Color(0xFF1B1D1C),
    surfaceVariant = Color(0xFFE7ECEA),
    onSurfaceVariant = Color(0xFF3A4240),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF6F7976),
    scrim = Color(0x66000000)
)

data class AppColors(
    val income: Color,
    val debt: Color
)

val LightAppColors = AppColors(
    income = IncomeLight,
    debt = DebtLight
)

val DarkAppColors = AppColors(
    income = IncomeDark,
    debt = DebtDark
)

@Composable
fun IManageTheme(
    themeMode: String = ThemeManager.THEME_SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeManager.THEME_LIGHT -> false
        ThemeManager.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }

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