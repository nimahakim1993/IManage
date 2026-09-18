package com.nima.app.imanage.presentation.view

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.presentation.viewmodel.FinancialReportViewModel
import com.nima.app.imanage.presentation.viewmodel.MonthlyFinancialData
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
import com.nima.app.imanage.ui.component.chart.BarCanvasChart
import com.nima.app.imanage.ui.component.chart.BarCanvasItem
import com.nima.app.imanage.ui.component.chart.DonutSegment
import com.nima.app.imanage.ui.component.chart.LineCanvasChart
import com.nima.app.imanage.ui.component.chart.LineCanvasSeries
import com.nima.app.imanage.ui.component.chart.VicoDonutChart
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: FinancialReportViewModel = koinViewModel()
) {
    val reportTitle = stringResource(R.string.financial_report_title)

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            FinancialReportFilterChipRow(
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
                FinancialReportCustomRangeRow(
                    fromDate = customFrom,
                    toDate = customTo,
                    onFromClick = { showCustomFromPicker = true },
                    onToClick = { showCustomToPicker = true }
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.financial_report_module_overview),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
        item {
            ModuleOverviewBarCard(data = data.moduleOverview)
        }

        item {
            Text(
                text = stringResource(R.string.financial_report_payable_breakdown),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
        item {
            PayableBreakdownDonutCard(data = data.payableBreakdown)
        }

        item {
            Text(
                text = stringResource(R.string.financial_report_receivable_breakdown),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
        item {
            ReceivableBreakdownDonutCard(data = data.receivableBreakdown)
        }

        item {
            Text(
                text = stringResource(R.string.financial_report_settled_vs_unsettled),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
        item {
            SettledVsUnsettledBarCard(data = data.settledVsUnsettled)
        }

        item {
            Text(
                text = stringResource(R.string.financial_report_monthly_trend),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
        item {
            MonthlyTrendLineCard(trend = data.monthlyTrend)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
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
private fun FinancialReportFilterChipRow(
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
private fun FinancialReportCustomRangeRow(
    fromDate: Long,
    toDate: Long,
    onFromClick: () -> Unit,
    onToClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FinancialReportDateField(
            label = stringResource(R.string.report_from_date),
            date = fromDate,
            onClick = onFromClick,
            modifier = Modifier.weight(1f)
        )
        FinancialReportDateField(
            label = stringResource(R.string.report_to_date),
            date = toDate,
            onClick = onToClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FinancialReportDateField(
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
private fun ModuleOverviewBarCard(data: com.nima.app.imanage.presentation.viewmodel.ModuleOverviewData) {
    val isDark = LocalIsDarkTheme.current
    val expenseColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val incomeColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val debtColor = Color(0xFFFF9800)
    val receivableColor = Color(0xFF2196F3)
    val installmentColor = Color(0xFF009688)
    val payableCheckColor = if (isDark) Color(0xFFCE93D8) else Color(0xFF9C27B0)
    val receivedCheckColor = if (isDark) Color(0xFF80DEEA) else Color(0xFF00BCD4)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val barItems = listOf(
        BarCanvasItem(
            label = stringResource(R.string.expense),
            values = listOf(data.expense.toFloat() to expenseColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.income),
            values = listOf(data.income.toFloat() to incomeColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.debt),
            values = listOf(data.debt.toFloat() to debtColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.receivable),
            values = listOf(data.receivable.toFloat() to receivableColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.installment),
            values = listOf(data.installment.toFloat() to installmentColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.financial_report_payable_checks_short),
            values = listOf(data.payableChecks.toFloat() to payableCheckColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.financial_report_received_checks_short),
            values = listOf(data.receivedChecks.toFloat() to receivedCheckColor)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            BarCanvasChart(
                items = barItems,
                textColor = textColor,
                gridColor = gridColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                barWidthRatio = 0.6f,
                maxBarWidthDp = 32f
            )

            Spacer(modifier = Modifier.height(12.dp))

            val legendItems = listOf(
                stringResource(R.string.expense) to expenseColor,
                stringResource(R.string.income) to incomeColor,
                stringResource(R.string.debt) to debtColor,
                stringResource(R.string.receivable) to receivableColor,
                stringResource(R.string.installment) to installmentColor,
                stringResource(R.string.financial_report_payable_checks_short) to payableCheckColor,
                stringResource(R.string.financial_report_received_checks_short) to receivedCheckColor
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                legendItems.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (label, color) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = vazirFontFamily,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayableBreakdownDonutCard(
    data: com.nima.app.imanage.presentation.viewmodel.PayableBreakdownData
) {
    val isDark = LocalIsDarkTheme.current
    val expenseColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val debtColor = Color(0xFFFF9800)
    val installmentColor = Color(0xFF009688)
    val payableCheckColor = if (isDark) Color(0xFFCE93D8) else Color(0xFF9C27B0)

    val segments = listOf(
        DonutSegment(
            value = data.expenses.toFloat(),
            color = expenseColor,
            label = stringResource(R.string.expense),
            id = "expense"
        ),
        DonutSegment(
            value = data.debtLoans.toFloat(),
            color = debtColor,
            label = stringResource(R.string.debt),
            id = "debt"
        ),
        DonutSegment(
            value = data.payableChecks.toFloat(),
            color = payableCheckColor,
            label = stringResource(R.string.financial_report_payable_checks),
            id = "payable_checks"
        ),
        DonutSegment(
            value = data.installments.toFloat(),
            color = installmentColor,
            label = stringResource(R.string.installment),
            id = "installment"
        )
    )

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
            if (data.total == 0L) {
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
                VicoDonutChart(
                    segments = segments,
                    modifier = Modifier.size(180.dp),
                    innerSize = 0.6f,
                    enableAnimation = true,
                    enableSelection = true,
                    animationDuration = 1200,
                    segmentSpacing = 2f,
                    centerContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = NumberFormatUtils.format(data.total),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = scaledSp(16f),
                                fontFamily = vazirFontFamily
                            )
                            Text(
                                text = stringResource(R.string.toman),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = vazirFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                val breakdownItems = listOf(
                    Triple(stringResource(R.string.expense), data.expenses, expenseColor),
                    Triple(stringResource(R.string.debt), data.debtLoans, debtColor),
                    Triple(
                        stringResource(R.string.financial_report_payable_checks),
                        data.payableChecks,
                        payableCheckColor
                    ),
                    Triple(
                        stringResource(R.string.installment),
                        data.installments,
                        installmentColor
                    )
                )

                breakdownItems.forEach { (label, amount, color) ->
                    val percent = if (data.total > 0) (amount.toFloat() / data.total * 100) else 0f
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
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = vazirFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${ShamsiDate.toPersianDigits(percent.toInt().toString())}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = vazirFontFamily
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = NumberFormatUtils.format(amount),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceivableBreakdownDonutCard(
    data: com.nima.app.imanage.presentation.viewmodel.ReceivableBreakdownData
) {
    val isDark = LocalIsDarkTheme.current
    val incomeColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val receivableColor = Color(0xFF2196F3)
    val receivedCheckColor = if (isDark) Color(0xFF80DEEA) else Color(0xFF00BCD4)

    val segments = listOf(
        DonutSegment(
            value = data.incomes.toFloat(),
            color = incomeColor,
            label = stringResource(R.string.income),
            id = "income"
        ),
        DonutSegment(
            value = data.receivableLoans.toFloat(),
            color = receivableColor,
            label = stringResource(R.string.receivable),
            id = "receivable"
        ),
        DonutSegment(
            value = data.receivedChecks.toFloat(),
            color = receivedCheckColor,
            label = stringResource(R.string.financial_report_received_checks),
            id = "received_checks"
        )
    )

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
            if (data.total == 0L) {
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
                VicoDonutChart(
                    segments = segments,
                    modifier = Modifier.size(180.dp),
                    innerSize = 0.6f,
                    enableAnimation = true,
                    enableSelection = true,
                    animationDuration = 1200,
                    segmentSpacing = 2f,
                    centerContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = NumberFormatUtils.format(data.total),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = scaledSp(16f),
                                fontFamily = vazirFontFamily
                            )
                            Text(
                                text = stringResource(R.string.toman),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = vazirFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                val breakdownItems = listOf(
                    Triple(stringResource(R.string.income), data.incomes, incomeColor),
                    Triple(
                        stringResource(R.string.receivable),
                        data.receivableLoans,
                        receivableColor
                    ),
                    Triple(
                        stringResource(R.string.financial_report_received_checks),
                        data.receivedChecks,
                        receivedCheckColor
                    )
                )

                breakdownItems.forEach { (label, amount, color) ->
                    val percent = if (data.total > 0) (amount.toFloat() / data.total * 100) else 0f
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
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = vazirFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${ShamsiDate.toPersianDigits(percent.toInt().toString())}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = vazirFontFamily
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = NumberFormatUtils.format(amount),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettledVsUnsettledBarCard(
    data: com.nima.app.imanage.presentation.viewmodel.SettledVsUnsettledData
) {
    val settledColor = if (LocalIsDarkTheme.current) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val unsettledColor = if (LocalIsDarkTheme.current) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val barItems = listOf(
        BarCanvasItem(
            label = stringResource(R.string.installment),
            values = listOf(
                data.installmentSettled.toFloat() to settledColor,
                data.installmentUnsettled.toFloat() to unsettledColor
            )
        ),
        BarCanvasItem(
            label = stringResource(R.string.check),
            values = listOf(
                data.checkSettled.toFloat() to settledColor,
                data.checkUnsettled.toFloat() to unsettledColor
            )
        ),
        BarCanvasItem(
            label = stringResource(R.string.loan),
            values = listOf(
                data.loanSettled.toFloat() to settledColor,
                data.loanUnsettled.toFloat() to unsettledColor
            )
        )
    )

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
                        .background(settledColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.financial_report_settled),
                    fontSize = scaledSp(11f),
                    fontFamily = vazirFontFamily,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(unsettledColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.financial_report_unsettled),
                    fontSize = scaledSp(11f),
                    fontFamily = vazirFontFamily,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            BarCanvasChart(
                items = barItems,
                textColor = textColor,
                gridColor = gridColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                barWidthRatio = 0.4f,
                maxBarWidthDp = 32f
            )
        }
    }
}

@Composable
private fun MonthlyTrendLineCard(trend: List<MonthlyFinancialData>) {
    val isDark = LocalIsDarkTheme.current
    val expenseColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val incomeColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val expenseFill =
        if (isDark) expenseColor.copy(alpha = 0.15f) else expenseColor.copy(alpha = 0.1f)
    val incomeFill = if (isDark) incomeColor.copy(alpha = 0.15f) else incomeColor.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val hasData = trend.any { it.expense > 0 || it.income > 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!hasData) {
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
                val series = listOf(
                    LineCanvasSeries(
                        values = trend.map { it.expense.toFloat() },
                        lineColor = expenseColor,
                        fillColor = expenseFill
                    ),
                    LineCanvasSeries(
                        values = trend.map { it.income.toFloat() },
                        lineColor = incomeColor,
                        fillColor = incomeFill
                    )
                )
                val xLabels = trend.map { ShamsiDate.getMonthName(it.month).take(3) }

                LineCanvasChart(
                    series = series,
                    xLabels = xLabels,
                    textColor = textColor,
                    gridColor = gridColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 3.dp)
                                .background(expenseColor, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = stringResource(R.string.report_expense),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            fontSize = scaledSp(10f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 3.dp)
                                .background(incomeColor, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = stringResource(R.string.report_income),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            fontSize = scaledSp(10f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
