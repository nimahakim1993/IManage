package com.nima.app.imanage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.nima.app.imanage.R
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.compose.common.component.TextComponent as VicoTextComponent

@Composable
fun rememberVazirTextComponent(
    fontSize: Float = 12f,
    color: Color = vicoTheme.textColor
): VicoTextComponent {
    val context = LocalContext.current
    val typeface = remember { ResourcesCompat.getFont(context, R.font.vazir_bold) }

    return rememberTextComponent(
        style = TextStyle(
            fontFamily = vazirFontFamily,
            fontSize = fontSize.sp,
            color = color
        )
    )
}

@Composable
fun ProvideIManageVicoTheme(content: @Composable () -> Unit) {
    val isDark = LocalIsDarkTheme.current

    val seriesColors = remember(isDark) {
        if (isDark) {
            listOf(
                Color(0xFF60A5FA),
                Color(0xFFA78BFA),
                Color(0xFFFB923C),
                Color(0xFF34D399),
                Color(0xFFF472B6),
                Color(0xFFFBBF24),
                Color(0xFFA3E635)
            )
        } else {
            listOf(
                Color(0xFF1E3A8A),
                Color(0xFF4C1D95),
                Color(0xFF7C2D12),
                Color(0xFF065F46),
                Color(0xFF9D174D),
                Color(0xFF92400E),
                Color(0xFF3F6212)
            )
        }
    }

    val vicoTheme = remember(isDark) {
        VicoTheme(
            candlestickCartesianLayerColors = VicoTheme.CandlestickCartesianLayerColors(
                bullish = if (isDark) Color(0xFF34D399) else Color(0xFF065F46),
                neutral = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                bearish = if (isDark) Color(0xFFF87171) else Color(0xFF991B1B)
            ),
            columnCartesianLayerColors = seriesColors,
            lineColor = if (isDark) Color(0xFF4B5563) else Color(0xFF9CA3AF),
            textColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF374151)
        )
    }

    com.patrykandpatrick.vico.compose.common.ProvideVicoTheme(vicoTheme) {
        content()
    }
}

fun formatChartValue(value: Float): String {
    return com.nima.app.imanage.util.NumberFormatUtils.format(value.toLong())
}

fun formatChartPercent(value: Float): String {
    return "${com.nima.app.imanage.util.NumberFormatUtils.format(value.toInt())}%"
}
