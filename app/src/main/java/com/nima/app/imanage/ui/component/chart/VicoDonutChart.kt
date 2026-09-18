package com.nima.app.imanage.ui.component.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import kotlin.math.atan2

data class DonutSegment(
    val value: Float,
    val color: Color,
    val label: String? = null,
    val id: Any? = null
)

@Composable
fun VicoDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    innerSize: Float = 0.6f,
    showLabels: Boolean = false,
    enableAnimation: Boolean = true,
    enableSelection: Boolean = false,
    selectedSegmentIndex: Int? = null,
    onSegmentSelected: ((Int, DonutSegment) -> Unit)? = null,
    animationDuration: Int = 800,
    segmentSpacing: Float = 2f,
    centerContent: @Composable (() -> Unit)? = null
) {
    val modelProducer = remember { PieChartModelProducer() }

    LaunchedEffect(segments) {
        if (segments.isNotEmpty()) {
            modelProducer.runTransaction {
                pieModel {
                    series(segments.map { it.value })
                }
            }
        }
    }

    var internalSelectedIndex by remember { mutableIntStateOf(-1) }
    val selectedIndex = selectedSegmentIndex ?: internalSelectedIndex
    val total = remember(segments) { segments.sumOf { it.value.toDouble() }.toFloat() }

    val scale by animateFloatAsState(
        targetValue = if (selectedIndex >= 0) 1.05f else 1f,
        animationSpec = tween(200)
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (enableSelection && segments.isNotEmpty()) {
                    Modifier.pointerInput(segments, total) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle =
                                Math.toDegrees(atan2(dx.toDouble(), (-dy).toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            var cumulative = 0f
                            for (i in segments.indices) {
                                cumulative += (segments[i].value / total) * 360f
                                if (angle <= cumulative) {
                                    internalSelectedIndex =
                                        if (internalSelectedIndex == i) -1 else i
                                    onSegmentSelected?.invoke(i, segments[i])
                                    break
                                }
                            }
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (segments.isNotEmpty()) {
            val innerSizeDp = (innerSize * 160).dp

            PieChartHost(
                chart = rememberPieChart(
                    sliceProvider = PieChart.SliceProvider.series(
                        segments.mapIndexed { index, segment ->
                            PieChart.Slice(
                                fill = Fill(
                                    if (selectedIndex >= 0 && selectedIndex != index)
                                        segment.color.copy(alpha = 0.45f)
                                    else segment.color
                                )
                            )
                        }
                    ),
                    innerSize = PieSize.Inner.fixed(innerSizeDp),
                    spacing = segmentSpacing.dp
                ),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxSize(),
                animateIn = enableAnimation,
                animationSpec = if (enableAnimation) {
                    tween(durationMillis = animationDuration)
                } else null
            )
        }

        centerContent?.invoke()
    }
}
