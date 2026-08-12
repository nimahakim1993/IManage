package com.nima.app.imanage.util

import androidx.compose.ui.graphics.Color

data class AppColorPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color
)

object ColorUtils {
    val palettes = listOf(
        AppColorPalette(Color(0xFF0F5C5A), Color(0xFF4DB6AC), Color(0xFFB2DFDB)),
        AppColorPalette(Color(0xFF1E3A8A), Color(0xFF60A5FA), Color(0xFFDBEAFE)),
        AppColorPalette(Color(0xFF7C2D12), Color(0xFFFB923C), Color(0xFFFFEDD5)),
        AppColorPalette(Color(0xFF4C1D95), Color(0xFFA78BFA), Color(0xFFEDE9FE)),
        AppColorPalette(Color(0xFF374151), Color(0xFF9CA3AF), Color(0xFFF3F4F6)),
        AppColorPalette(Color(0xFF1C1C1E), Color(0xFF71717A), Color(0xFFE4E4E7)),
        AppColorPalette(Color(0xFFC5A44B), Color(0xFFF4C27A), Color(0xFFF8E7C8)),
        AppColorPalette(Color(0xFF8B0000), Color(0xFFEF5350), Color(0xFFF9DEDC)),
        AppColorPalette(Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFFDBEAFE)),
        AppColorPalette(Color(0xFF047857), Color(0xFF4ADE80), Color(0xFFDCFCE7)),
        AppColorPalette(Color(0xFFB45309), Color(0xFFFB923C), Color(0xFFFFEDD5)),
        AppColorPalette(Color(0xFFBE185D), Color(0xFFF472B6), Color(0xFFFCE7F3)),
        AppColorPalette(Color(0xFF0E7490), Color(0xFF22D3EE), Color(0xFFCFFAFE)),
        AppColorPalette(Color(0xFF6D28D9), Color(0xFFA78BFA), Color(0xFFEDE9FE))
    )

    val colors = palettes.map { it.primary }
    val installmentPalette = palettes[1]
}
