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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.domain.model.ReportData
import com.nima.app.imanage.presentation.viewmodel.ReportViewModel
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
import com.nima.app.imanage.ui.component.chart.BarCanvasChart
import com.nima.app.imanage.ui.component.chart.BarCanvasItem
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: ReportViewModel = koinViewModel()
) {
    val reportTitle = stringResource(R.string.report_title)

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
                FilterChipRow(
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
                    CustomRangeRow(
                        fromDate = customFrom,
                        toDate = customTo,
                        onFromClick = { showCustomFromPicker = true },
                        onToClick = { showCustomToPicker = true }
                    )
                }
            }

            item { ReportOverviewCard(data = data) }

            item { StatsGrid(data = data) }

            item {
                Text(
                    text = stringResource(R.string.report_finance_overview),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }

            item {
                FinanceBarChartCard(data = data)
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

@Composable
private fun CustomRangeRow(
    fromDate: Long,
    toDate: Long,
    onFromClick: () -> Unit,
    onToClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DateField(
            label = stringResource(R.string.report_from_date),
            date = fromDate,
            onClick = onFromClick,
            modifier = Modifier.weight(1f)
        )
        DateField(
            label = stringResource(R.string.report_to_date),
            date = toDate,
            onClick = onToClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DateField(
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
private fun FilterChipRow(
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
private fun ReportOverviewCard(data: ReportData) {
    val isDark = LocalIsDarkTheme.current
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.report_overview),
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = vazirFontFamily
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.report_net_balance),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = vazirFontFamily
                )
                val netColor = if (data.netBalance >= 0) Color(0xFFB9F6CA) else Color(0xFFFFCDD2)
                Text(
                    text = NumberFormatUtils.format(data.netBalance),
                    color = netColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = scaledSp(30f),
                    fontFamily = vazirFontFamily
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(data: ReportData) {
    val stats = listOf(
        StatItem(
            stringResource(R.string.report_income),
            NumberFormatUtils.format(data.incomeCount),
            Icons.Default.TrendingUp,
            Color(0xFF4CAF50),
            NumberFormatUtils.format(data.totalIncomes)
        ),
        StatItem(
            stringResource(R.string.report_expense),
            NumberFormatUtils.format(data.expenseCount),
            Icons.Default.TrendingDown,
            Color(0xFFF44336),
            NumberFormatUtils.format(data.totalExpenses)
        ),
        StatItem(
            stringResource(R.string.report_debt),
            NumberFormatUtils.format(data.debtCount),
            Icons.Default.MoneyOff,
            Color(0xFFFF9800),
            NumberFormatUtils.format(data.totalDebt)
        ),
        StatItem(
            stringResource(R.string.report_receivable),
            NumberFormatUtils.format(data.receivableCount),
            Icons.Default.Savings,
            Color(0xFF2196F3),
            NumberFormatUtils.format(data.totalReceivable)
        ),
        StatItem(
            stringResource(R.string.report_payable_checks),
            NumberFormatUtils.format(data.payableCheckCount),
            Icons.Default.ReceiptLong,
            Color(0xFFE91E63),
            NumberFormatUtils.format(data.totalPayableChecks)
        ),
        StatItem(
            stringResource(R.string.report_received_checks),
            NumberFormatUtils.format(data.receivedCheckCount),
            Icons.Default.Receipt,
            Color(0xFF00BCD4),
            NumberFormatUtils.format(data.totalReceivedChecks)
        ),
        StatItem(
            stringResource(R.string.report_installments),
            NumberFormatUtils.format(data.installmentCount),
            Icons.Default.CalendarMonth,
            Color(0xFF795548),
            NumberFormatUtils.format(data.totalInstallments)
        ),
        StatItem(
            stringResource(R.string.report_car),
            NumberFormatUtils.format(data.carServiceCount),
            Icons.Default.DirectionsCar,
            Color(0xFF9C27B0),
            NumberFormatUtils.format(data.totalCarExpenses)
        ),
        StatItem(
            stringResource(R.string.report_trips),
            NumberFormatUtils.format(data.tripCount),
            Icons.Default.Groups,
            MaterialTheme.colorScheme.primary,
            NumberFormatUtils.format(data.totalTripExpenses)
        ),
        StatItem(
            stringResource(R.string.report_assets),
            NumberFormatUtils.format(data.assetCount),
            Icons.Default.AccountBalanceWallet,
            Color(0xFF009688),
            NumberFormatUtils.format(data.totalAssetValue)
        ),
        StatItem(
            stringResource(R.string.report_cards),
            NumberFormatUtils.format(data.bankCardCount),
            Icons.Default.AccountBalance,
            MaterialTheme.colorScheme.tertiary
        ),
        StatItem(
            stringResource(R.string.report_passwords),
            NumberFormatUtils.format(data.passwordCount),
            Icons.Default.Lock,
            Color(0xFF607D8B)
        ),
        StatItem(
            stringResource(R.string.report_noteboxes),
            NumberFormatUtils.format(data.noteBoxCount),
            Icons.Default.Note,
            Color(0xFFFF5722)
        ),
        StatItem(
            stringResource(R.string.report_office_notes),
            NumberFormatUtils.format(data.officeNoteCount),
            Icons.Default.StickyNote2,
            Color(0xFF3F51B5)
        ),
        StatItem(
            stringResource(R.string.report_office_reminders),
            NumberFormatUtils.format(data.officeReminderCount),
            Icons.Default.Alarm,
            Color(0xFFCDDC39)
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { stat ->
                    StatCard(stat = stat, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private data class StatItem(
    val label: String,
    val count: String,
    val icon: ImageVector,
    val color: Color,
    val amount: String? = null
)

@Composable
private fun StatCard(stat: StatItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(stat.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        stat.icon,
                        contentDescription = null,
                        tint = stat.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = vazirFontFamily,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stat.count,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (stat.amount != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stat.amount,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = vazirFontFamily,
                    color = stat.color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FinanceBarChartCard(data: ReportData) {
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
            values = listOf(data.totalExpenses.toFloat() to expenseColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.income),
            values = listOf(data.totalIncomes.toFloat() to incomeColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.debt),
            values = listOf(data.totalDebt.toFloat() to debtColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.receivable),
            values = listOf(data.totalReceivable.toFloat() to receivableColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.installment),
            values = listOf(data.totalInstallments.toFloat() to installmentColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.financial_report_payable_checks_short),
            values = listOf(data.totalPayableChecks.toFloat() to payableCheckColor)
        ),
        BarCanvasItem(
            label = stringResource(R.string.financial_report_received_checks_short),
            values = listOf(data.totalReceivedChecks.toFloat() to receivedCheckColor)
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
                showYAxisLabels = true,
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
internal fun YearPickerDialog(
    currentYear: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val today = ShamsiDate.today()
    val currentYearVal = today.first
    val years = (currentYearVal - 5..currentYearVal + 1).toList().reversed()

    var selectedYear by remember { mutableStateOf(currentYear) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                stringResource(R.string.report_select_year),
                fontFamily = vazirFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                items(years.size) { index ->
                    val year = years[index]
                    val isSelected = year == selectedYear
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable { selectedYear = year }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = isSelected,
                            onClick = { selectedYear = year }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = ShamsiDate.toPersianDigits(year.toString()),
                            fontFamily = vazirFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onConfirm(selectedYear) }) {
                Text(
                    stringResource(R.string.confirm),
                    fontFamily = vazirFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontFamily = vazirFontFamily)
            }
        }
    )
}
