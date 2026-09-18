package com.nima.app.imanage.ui.component.chart

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.nima.app.imanage.R

data class LineCanvasSeries(
    val values: List<Float>,
    val lineColor: Color,
    val fillColor: Color? = null,
    val showDots: Boolean = true,
    val dotRadiusDp: Float = 4f,
    val innerDotRadiusDp: Float = 2f,
    val innerDotColor: Color = Color.White
)

@Composable
fun LineCanvasChart(
    series: List<LineCanvasSeries>,
    xLabels: List<String>,
    textColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier,
    showGridLines: Boolean = true,
    lineWidthDp: Float = 3f,
    bottomPaddingDp: Float = 30f,
    topPaddingDp: Float = 0f
) {
    val context = LocalContext.current
    val typeface = remember { ResourcesCompat.getFont(context, R.font.vazir_bold) }
    val textColorArgb = textColor.hashCode()

    Canvas(modifier = modifier) {
        val chartHeight = size.height - bottomPaddingDp.dp.toPx() - topPaddingDp.dp.toPx()
        val chartWidth = size.width

        val allValues = series.flatMap { it.values }
        val maxVal = allValues.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val hasData = allValues.any { it > 0f }

        if (!hasData) return@Canvas

        val stepX = chartWidth / (xLabels.size - 1).coerceAtLeast(1)

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

        series.forEach { lineSeries ->
            val points = lineSeries.values.mapIndexed { index, value ->
                val x = stepX * index
                val y =
                    topPaddingDp.dp.toPx() + chartHeight - (value / maxVal * chartHeight).coerceAtLeast(
                        0f
                    )
                Offset(x, y)
            }

            if (points.size >= 2) {
                lineSeries.fillColor?.let { fillColor ->
                    val fillPath = Path().apply {
                        moveTo(points.first().x, topPaddingDp.dp.toPx() + chartHeight)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, topPaddingDp.dp.toPx() + chartHeight)
                        close()
                    }
                    drawPath(path = fillPath, color = fillColor)
                }

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineSeries.lineColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = lineWidthDp.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                if (lineSeries.showDots) {
                    points.forEach { point ->
                        drawCircle(
                            color = lineSeries.lineColor,
                            radius = lineSeries.dotRadiusDp.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = lineSeries.innerDotColor,
                            radius = lineSeries.innerDotRadiusDp.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }

        xLabels.forEachIndexed { index, label ->
            val x = stepX * index
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
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
