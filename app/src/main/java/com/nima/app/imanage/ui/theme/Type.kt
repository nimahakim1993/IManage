package com.nima.app.imanage.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nima.app.imanage.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val vazirFontFamily = FontFamily(
    Font(R.font.vazir_bold, FontWeight.Bold)
)

fun getVazirTypography(fontScale: Float): Typography{
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (16 * fontScale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * fontScale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15 * fontScale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * fontScale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * fontScale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = (11 * fontScale).sp
        ),
    )
}