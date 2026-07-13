package com.nima.app.imanage.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nima.app.imanage.R

val vazirFontFamily = FontFamily(
    Font(R.font.vazir_bold, FontWeight.Bold)
)

fun getVazirTypography(fontScale: Float): Typography {
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.displayLarge.fontSize.value * fontScale).sp
        ),
        displayMedium = default.displayMedium.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.displayMedium.fontSize.value * fontScale).sp
        ),
        displaySmall = default.displaySmall.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.displaySmall.fontSize.value * fontScale).sp
        ),
        headlineLarge = default.headlineLarge.copy(
            fontFamily = vazirFontFamily, fontWeight = FontWeight.Bold,
            fontSize = (default.headlineLarge.fontSize.value * fontScale).sp
        ),
        headlineMedium = default.headlineMedium.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.headlineMedium.fontSize.value * fontScale).sp
        ),
        headlineSmall = default.headlineSmall.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.headlineSmall.fontSize.value * fontScale).sp
        ),
        titleLarge = default.titleLarge.copy(
            fontFamily = vazirFontFamily, fontWeight = FontWeight.SemiBold,
            fontSize = (default.titleLarge.fontSize.value * fontScale).sp
        ),
        titleMedium = default.titleMedium.copy(
            fontFamily = vazirFontFamily, fontWeight = FontWeight.SemiBold,
            fontSize = (default.titleMedium.fontSize.value * fontScale).sp
        ),
        titleSmall = default.titleSmall.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.titleSmall.fontSize.value * fontScale).sp
        ),
        bodyLarge = default.bodyLarge.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.bodyLarge.fontSize.value * fontScale).sp
        ),
        bodyMedium = default.bodyMedium.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.bodyMedium.fontSize.value * fontScale).sp
        ),
        bodySmall = default.bodySmall.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.bodySmall.fontSize.value * fontScale).sp
        ),
        labelLarge = default.labelLarge.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.labelLarge.fontSize.value * fontScale).sp
        ),
        labelMedium = default.labelMedium.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.labelMedium.fontSize.value * fontScale).sp
        ),
        labelSmall = default.labelSmall.copy(
            fontFamily = vazirFontFamily,
            fontSize = (default.labelSmall.fontSize.value * fontScale).sp
        )
    )
}
