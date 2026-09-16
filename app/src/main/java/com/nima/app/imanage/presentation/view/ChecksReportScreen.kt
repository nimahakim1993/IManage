package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.presentation.viewmodel.ChecksReportViewModel
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecksReportScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: ChecksReportViewModel = koinViewModel()
) {
    val reportTitle = stringResource(R.string.checks_report_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = reportTitle,
                showBack = true
            )
        )
    }

    val data by viewModel.reportData.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showCustomFromPicker by remember { mutableStateOf(false) }
    var showCustomToPicker by remember { mutableStateOf(false) }
    var customFrom by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var customTo by remember { mutableLongStateOf(System.currentTimeMillis()) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                ChecksReportFilterChipRow(
                    currentMode = filterMode,
                    selectedLabel = when (filterMode) {
                        FilterMode.MONTHLY -> selectedMonthYear?.let { (m, y) ->
                            "${ShamsiDate.getMonthName(m)} ${ShamsiDate.toPersianDigits(y.toString())}"
                        } ?: stringResource(R.string.report_select_month)

                        FilterMode.YEARLY -> selectedYear?.let {
                            ShamsiDate.toPersianDigits(it.toString())
                        } ?: stringResource(R.string.report_select_year)

                        FilterMode.CUSTOM -> stringResource(R.string.report_custom_range)
                        else -> null
                    },
                    onModeSelected = { mode ->
                        viewModel.setFilterMode(mode)
                        when (mode) {
                            FilterMode.MONTHLY -> showMonthYearPicker = true
                            FilterMode.YEARLY -> showYearPicker = true
                            else -> {}
                        }
                    }
                )
            }

            if (filterMode == FilterMode.CUSTOM) {
                item {
                    ChecksReportCustomRangeRow(
                        fromDate = customFrom,
                        toDate = customTo,
                        onFromClick = { showCustomFromPicker = true },
                        onToClick = { showCustomToPicker = true }
                    )
                }
            }

            item {
                ChecksOverviewCard(
                    totalReceived = data.totalReceived,
                    totalPayable = data.totalPayable,
                    checkCount = data.checkCount
                )
            }

            item {
                Text(
                    text = stringResource(R.string.checks_report_received_vs_payable),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }
            item {
                ReceivedPayableDonutCard(
                    totalReceived = data.totalReceived,
                    totalPayable = data.totalPayable
                )
            }

            item {
                Text(
                    text = stringResource(R.string.checks_report_yearly_comparison),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }
            item {
                YearlyComparisonBarCard(yearlyData = data.yearlyComparison)
            }

            if (data.counterpartyBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.checks_report_counterparty_breakdown),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                }
                item {
                    CounterpartyBreakdownCard(counterpartyData = data.counterpartyBreakdown)
                }
            }

            if (data.stateBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.checks_report_state_breakdown),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                }
                item {
                    StateBreakdownCard(stateData = data.stateBreakdown)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showMonthYearPicker) {
        ShamsiMonthYearPicker(
            initialMonth = selectedMonthYear?.first,
            initialYear = selectedMonthYear?.second,
            onConfirm = { month, year ->
                if (month != null) viewModel.setMonthYear(month, year)
                showMonthYearPicker = false
            },
            onDismiss = { showMonthYearPicker = false }
        )
    }

    if (showYearPicker) {
        YearPickerDialog(
            currentYear = selectedYear ?: ShamsiDate.today().first,
            onConfirm = { year ->
                viewModel.setYear(year)
                showYearPicker = false
            },
            onDismiss = { showYearPicker = false }
        )
    }

    if (showCustomFromPicker) {
        ShamsiDatePicker(
            initialDate = customFrom,
            onConfirm = { date ->
                customFrom = date
                showCustomFromPicker = false
                viewModel.setCustomRange(date, customTo)
            },
            onDismiss = { showCustomFromPicker = false }
        )
    }

    if (showCustomToPicker) {
        ShamsiDatePicker(
            initialDate = customTo,
            onConfirm = { date ->
                customTo = date
                showCustomToPicker = false
                viewModel.setCustomRange(customFrom, date)
            },
            onDismiss = { showCustomToPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChecksReportFilterChipRow(
    currentMode: FilterMode,
    selectedLabel: String?,
    onModeSelected: (FilterMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentMode == FilterMode.CURRENT_YEAR,
            onClick = { onModeSelected(FilterMode.CURRENT_YEAR) },
            label = {
                Text(
                    stringResource(R.string.report_filter_current_year),
                    fontFamily = vazirFontFamily,
                    fontSize = scaledSp(12f)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(20.dp)
        )
        FilterChip(
            selected = currentMode == FilterMode.MONTHLY,
            onClick = { onModeSelected(FilterMode.MONTHLY) },
            label = {
                Text(
                    selectedLabel?.takeIf { currentMode == FilterMode.MONTHLY }
                        ?: stringResource(R.string.report_filter_monthly),
                    fontFamily = vazirFontFamily,
                    fontSize = scaledSp(12f)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(20.dp)
        )
        FilterChip(
            selected = currentMode == FilterMode.YEARLY,
            onClick = { onModeSelected(FilterMode.YEARLY) },
            label = {
                Text(
                    selectedLabel?.takeIf { currentMode == FilterMode.YEARLY }
                        ?: stringResource(R.string.report_filter_yearly),
                    fontFamily = vazirFontFamily,
                    fontSize = scaledSp(12f)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(20.dp)
        )
        FilterChip(
            selected = currentMode == FilterMode.CUSTOM,
            onClick = { onModeSelected(FilterMode.CUSTOM) },
            label = {
                Text(
                    selectedLabel?.takeIf { currentMode == FilterMode.CUSTOM }
                        ?: stringResource(R.string.report_filter_custom),
                    fontFamily = vazirFontFamily,
                    fontSize = scaledSp(12f)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ChecksReportCustomRangeRow(
    fromDate: Long,
    toDate: Long,
    onFromClick: () -> Unit,
    onToClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChecksReportDateField(
            label = stringResource(R.string.report_from_date),
            date = fromDate,
            onClick = onFromClick,
            modifier = Modifier.weight(1f)
        )
        ChecksReportDateField(
            label = stringResource(R.string.report_to_date),
            date = toDate,
            onClick = onToClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ChecksReportDateField(
    label: String,
    date: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    fontSize = scaledSp(11f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = vazirFontFamily
                )
                Text(
                    text = ShamsiDate.format(date),
                    fontSize = scaledSp(13f),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ChecksOverviewCard(
    totalReceived: Long,
    totalPayable: Long,
    checkCount: Int
) {
    val isDark = LocalIsDarkTheme.current
    val receivedColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val payableColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.checks_report_total),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = ShamsiDate.toPersianDigits(checkCount.toString()),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = vazirFontFamily
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.checks_report_received),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatUtils.format(totalReceived),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(14f),
                        fontFamily = vazirFontFamily,
                        color = receivedColor
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.checks_report_payable),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatUtils.format(totalPayable),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(14f),
                        fontFamily = vazirFontFamily,
                        color = payableColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val netBalance = totalReceived - totalPayable
            Text(
                text = stringResource(R.string.checks_report_net_balance),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = vazirFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = NumberFormatUtils.format(netBalance),
                fontWeight = FontWeight.ExtraBold,
                fontSize = scaledSp(18f),
                fontFamily = vazirFontFamily,
                color = if (netBalance >= 0) receivedColor else payableColor
            )
        }
    }
}

@Composable
private fun ReceivedPayableDonutCard(
    totalReceived: Long,
    totalPayable: Long
) {
    val isDark = LocalIsDarkTheme.current
    val receivedColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val payableColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val total = totalReceived + totalPayable

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (total == 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.report_no_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = vazirFontFamily
                    )
                }
            } else {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = size.minDimension * 0.22f
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                        val receivedAngle =
                            if (total > 0) (totalReceived.toDouble() / total.toDouble()) * 360.0 else 0.0
                        val payableAngle = 360.0 - receivedAngle

                        var startAngle = -90f
                        drawArc(
                            color = receivedColor,
                            startAngle = startAngle,
                            sweepAngle = receivedAngle.toFloat(),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += receivedAngle.toFloat()
                        drawArc(
                            color = payableColor,
                            startAngle = startAngle,
                            sweepAngle = payableAngle.toFloat(),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val netBalance = totalReceived - totalPayable
                        Text(
                            text = NumberFormatUtils.format(netBalance),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = scaledSp(16f),
                            fontFamily = vazirFontFamily,
                            color = if (netBalance >= 0) receivedColor else payableColor
                        )
                        Text(
                            text = stringResource(R.string.checks_report_net_balance),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(
                        color = receivedColor,
                        label = stringResource(R.string.checks_report_received),
                        value = NumberFormatUtils.format(totalReceived)
                    )
                    LegendItem(
                        color = payableColor,
                        label = stringResource(R.string.checks_report_payable),
                        value = NumberFormatUtils.format(totalPayable)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = vazirFontFamily
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
    }
}

@Composable
private fun YearlyComparisonBarCard(
    yearlyData: List<com.nima.app.imanage.presentation.viewmodel.YearlyCheckData>
) {
    val isDark = LocalIsDarkTheme.current
    val receivedColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val payableColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val maxVal = yearlyData.maxOfOrNull {
        maxOf(it.receivedAmount, it.payableAmount)
    }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(receivedColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.checks_report_received),
                    fontSize = scaledSp(11f),
                    fontFamily = vazirFontFamily,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(payableColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.checks_report_payable),
                    fontSize = scaledSp(11f),
                    fontFamily = vazirFontFamily,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartHeight = size.height - 40.dp.toPx()
                val barCount = yearlyData.size
                val groupWidth = size.width / barCount.coerceAtLeast(1)
                val barWidth = (groupWidth * 0.3f).coerceAtMost(24.dp.toPx())

                for (i in 0..4) {
                    val y = chartHeight * i / 4
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                yearlyData.forEachIndexed { index, data ->
                    val centerX = groupWidth * index + groupWidth / 2

                    val receivedHeight =
                        (data.receivedAmount.toFloat() / maxVal * chartHeight).coerceAtLeast(0f)
                    val payableHeight =
                        (data.payableAmount.toFloat() / maxVal * chartHeight).coerceAtLeast(0f)

                    drawRect(
                        color = receivedColor,
                        topLeft = Offset(
                            centerX - barWidth - 2.dp.toPx(),
                            chartHeight - receivedHeight
                        ),
                        size = Size(barWidth, receivedHeight)
                    )
                    drawRect(
                        color = payableColor,
                        topLeft = Offset(centerX + 2.dp.toPx(), chartHeight - payableHeight),
                        size = Size(barWidth, payableHeight)
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        ShamsiDate.toPersianDigits(data.year.toString()),
                        centerX,
                        size.height - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            this.color = textColor.hashCode()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterpartyBreakdownCard(
    counterpartyData: List<com.nima.app.imanage.presentation.viewmodel.CounterpartyCheckData>
) {
    val isDark = LocalIsDarkTheme.current
    val receivedColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val payableColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            counterpartyData.take(10).forEach { data ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = data.counterparty,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${
                                ShamsiDate.toPersianDigits(
                                    data.percent.toInt().toString()
                                )
                            }%",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = NumberFormatUtils.format(data.receivedAmount),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            color = receivedColor
                        )
                        Text(
                            text = NumberFormatUtils.format(data.payableAmount),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            color = payableColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StateBreakdownCard(
    stateData: List<com.nima.app.imanage.presentation.viewmodel.StateCheckData>
) {
    val stateColors = mapOf(
        CheckEntity.STATE_IN_PROGRESS to Color(0xFF2196F3),
        CheckEntity.STATE_IN_BANK to Color(0xFFFF9800),
        CheckEntity.STATE_COLLECTED to Color(0xFF4CAF50),
        CheckEntity.STATE_RETURNED to Color(0xFF9C27B0),
        CheckEntity.STATE_BOUNCED to Color(0xFFF44336)
    )

    val inProgress = stringResource(R.string.check_state_in_progress)
    val inBank = stringResource(R.string.check_state_in_bank)
    val collected = stringResource(R.string.check_state_collected)
    val returned = stringResource(R.string.check_state_returned)
    val bounced = stringResource(R.string.check_state_bounced)

    fun getStateLabel(state: String): String {
        return when (state) {
            CheckEntity.STATE_IN_PROGRESS -> inProgress
            CheckEntity.STATE_IN_BANK -> inBank
            CheckEntity.STATE_COLLECTED -> collected
            CheckEntity.STATE_RETURNED -> returned
            CheckEntity.STATE_BOUNCED -> bounced
            else -> state
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            stateData.forEach { data ->
                val color = stateColors[data.state] ?: Color.Gray
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getStateLabel(data.state),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = vazirFontFamily,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ShamsiDate.toPersianDigits(data.count.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = NumberFormatUtils.format(data.amount),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
