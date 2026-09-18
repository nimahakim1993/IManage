package com.nima.app.imanage.ui.component.chart

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nima.app.imanage.ui.theme.rememberVazirTextComponent
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

data class BarSeries(
    val xValues: List<Float>,
    val yValues: List<Float>,
    val color: Color,
    val label: String? = null
)

@Composable
fun VicoBarChart(
    series: List<BarSeries>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    valueFormatter: (Float) -> String = { it.toLong().toString() },
    enableAnimation: Boolean = true,
    animationDuration: Int = 800
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(series) {
        if (series.isNotEmpty()) {
            modelProducer.runTransaction {
                columnModel {
                    series.forEachIndexed { index, barSeries ->
                        series(
                            x = barSeries.xValues.map { it.toDouble() },
                            y = barSeries.yValues.map { it.toDouble() },
                            key = index
                        )
                    }
                }
            }
        }
    }

    val labelComponent = rememberVazirTextComponent(fontSize = 9f)
    val valueComponent = rememberVazirTextComponent(fontSize = 9f)

    if (series.isNotEmpty()) {
        val columnLayer = rememberColumnCartesianLayer(
            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                series.map { barSeries ->
                    rememberLineComponent(
                        fill = Fill(barSeries.color),
                        thickness = 24.dp
                    )
                }
            ),
            mergeMode = { ColumnCartesianLayer.MergeMode.Grouped() }
        )

        val bottomAxis = HorizontalAxis.rememberBottom(
            label = labelComponent,
            valueFormatter = CartesianValueFormatter { context, value, _ ->
                val index = value.toInt()
                xLabels.getOrNull(index) ?: ""
            },
            itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                addExtremeLabelPadding = false
            ),
            guideline = null
        )

        val startAxis = VerticalAxis.rememberStart(
            label = valueComponent,
            valueFormatter = CartesianValueFormatter { _, value, _ ->
                valueFormatter(value.toFloat())
            },
            itemPlacer = VerticalAxis.ItemPlacer.count(
                count = { 5 }
            ),
            guideline = null
        )

        CartesianChartHost(
            chart = rememberCartesianChart(
                columnLayer,
                startAxis = startAxis,
                bottomAxis = bottomAxis
            ),
            modelProducer = modelProducer,
            modifier = modifier,
            animationSpec = if (enableAnimation) {
                tween(durationMillis = animationDuration)
            } else null
        )
    }
}
