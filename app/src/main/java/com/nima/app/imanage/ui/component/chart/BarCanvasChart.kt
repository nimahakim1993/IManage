package com.nima.app.imanage.ui.component.chart

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.nima.app.imanage.R
import com.nima.app.imanage.util.NumberFormatUtils

data class BarCanvasItem(
    val label: String,
    val values: List<Pair<Float, Color>>,
    val valueLabel: String? = null
)

@Composable
fun BarCanvasChart(
    items: List<BarCanvasItem>,
    textColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier,
    showGridLines: Boolean = true,
    showYAxisLabels: Boolean = false,
    showValueLabels: Boolean = false,
    roundedCorners: Boolean = false,
    barWidthRatio: Float = 0.5f,
    maxBarWidthDp: Float = 48f,
    bottomPaddingDp: Float = 40f,
    topPaddingDp: Float = 20f,
    formatYAxis: (Float) -> String = {
        NumberFormatUtils.format(it.toLong()).replace('\u066C', ',')
    }
) {
    val context = LocalContext.current
    val typeface = remember { ResourcesCompat.getFont(context, R.font.vazir_bold) }
    val textColorArgb = textColor.hashCode()

    Canvas(modifier = modifier) {
        val chartHeight = size.height - bottomPaddingDp.dp.toPx() - topPaddingDp.dp.toPx()
        val chartWidth = size.width
        val barCount = items.size.coerceAtLeast(1)
        val groupWidth = chartWidth / barCount
        val maxBarWidth = maxBarWidthDp.dp.toPx()

        val seriesCount = items.maxOfOrNull { it.values.size } ?: 1
        val singleBarWidth = if (seriesCount > 1) {
            (groupWidth * barWidthRatio / seriesCount).coerceAtMost(maxBarWidth)
        } else {
            (groupWidth * barWidthRatio).coerceAtMost(maxBarWidth)
        }

        val maxVal =
            items.flatMap { it.values.map { v -> v.first } }.maxOrNull()?.coerceAtLeast(1f) ?: 1f

        if (showGridLines) {
            for (i in 0..4) {
                val y = chartHeight * i / 4
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )
            }
        }

        if (showYAxisLabels) {
            for (i in 0..4) {
                val y = chartHeight * i / 4
                val labelVal = maxVal * (4 - i) / 4
                drawContext.canvas.nativeCanvas.drawText(
                    formatYAxis(labelVal),
                    4.dp.toPx(),
                    y + 12.dp.toPx(),
                    Paint().apply {
                        this.color = textColorArgb
                        textSize = 9.sp.toPx()
                        textAlign = Paint.Align.LEFT
                        this.typeface = typeface
                    }
                )
            }
        }

        items.forEachIndexed { index, item ->
            val centerX = groupWidth * index + groupWidth / 2

            if (seriesCount > 1) {
                val totalBarsWidth = singleBarWidth * seriesCount + 2.dp.toPx() * (seriesCount - 1)
                var startX = centerX - totalBarsWidth / 2

                item.values.forEach { (value, color) ->
                    val barHeight = (value / maxVal * chartHeight).coerceAtLeast(0f)

                    if (roundedCorners) {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(
                                startX,
                                topPaddingDp.dp.toPx() + chartHeight - barHeight
                            ),
                            size = Size(singleBarWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    } else {
                        drawRect(
                            color = color,
                            topLeft = Offset(
                                startX,
                                topPaddingDp.dp.toPx() + chartHeight - barHeight
                            ),
                            size = Size(singleBarWidth, barHeight)
                        )
                    }
                    startX += singleBarWidth + 2.dp.toPx()
                }
            } else {
                val value = item.values.firstOrNull()?.first ?: 0f
                val color = item.values.firstOrNull()?.second ?: Color.Gray
                val barHeight = (value / maxVal * chartHeight).coerceAtLeast(0f)

                if (roundedCorners) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(
                            centerX - singleBarWidth / 2,
                            topPaddingDp.dp.toPx() + chartHeight - barHeight
                        ),
                        size = Size(singleBarWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                } else {
                    drawRect(
                        color = color,
                        topLeft = Offset(
                            centerX - singleBarWidth / 2,
                            topPaddingDp.dp.toPx() + chartHeight - barHeight
                        ),
                        size = Size(singleBarWidth, barHeight)
                    )
                }

                if (showValueLabels && item.valueLabel != null) {
                    drawContext.canvas.nativeCanvas.drawText(
                        item.valueLabel,
                        centerX,
                        topPaddingDp.dp.toPx() + chartHeight - barHeight - 6.dp.toPx(),
                        Paint().apply {
                            this.color = textColorArgb
                            textSize = 10.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            this.isFakeBoldText = true
                            this.typeface = typeface
                        }
                    )
                }
            }

            drawContext.canvas.nativeCanvas.drawText(
                item.label,
                centerX,
                size.height - 4.dp.toPx(),
                Paint().apply {
                    this.color = textColorArgb
                    textSize = 9.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    this.typeface = typeface
                }
            )
        }
    }
}
