package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.domain.model.MonthAmount
import com.nima.app.imanage.domain.model.ReportData
import com.nima.app.imanage.presentation.viewmodel.ReportViewModel
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
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

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    text = stringResource(R.string.report_income_vs_expense),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }

            item {
                BarChartCard(
                    monthlyExpenses = data.monthlyExpenses,
                    monthlyIncomes = data.monthlyIncomes
                )
            }

            item {
                Text(
                    text = stringResource(R.string.report_category_breakdown),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }

            item { CategoryBreakdownCard(data = data) }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showMonthYearPicker) {
        ShamsiMonthYearPicker(
            initialMonth = selectedMonthYear?.first,
            initialYear = selectedMonthYear?.second,
            onConfirm = { month, year ->
                viewModel.setMonthYear(month, year)
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
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = vazirFontFamily
                )
                Text(
                    text = ShamsiDate.format(date),
                    fontSize = 13.sp,
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
                    fontSize = 12.sp
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
                    fontSize = 12.sp
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
                    fontSize = 12.sp
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
                    fontSize = 12.sp
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
    val isDark = isSystemInDarkTheme()
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
                    fontSize = 30.sp,
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
            NumberFormatUtils.format(data.totalIncomes),
            Icons.Default.TrendingUp,
            Color(0xFF4CAF50)
        ),
        StatItem(
            stringResource(R.string.report_expense),
            NumberFormatUtils.format(data.totalExpenses),
            Icons.Default.TrendingDown,
            Color(0xFFF44336)
        ),
        StatItem(
            stringResource(R.string.report_debt),
            NumberFormatUtils.format(data.totalDebt),
            Icons.Default.MoneyOff,
            Color(0xFFFF9800)
        ),
        StatItem(
            stringResource(R.string.report_receivable),
            NumberFormatUtils.format(data.totalReceivable),
            Icons.Default.Savings,
            Color(0xFF2196F3)
        ),
        StatItem(
            stringResource(R.string.report_trips),
            NumberFormatUtils.format(data.tripCount),
            Icons.Default.Groups,
            MaterialTheme.colorScheme.primary
        ),
        StatItem(
            stringResource(R.string.report_cards),
            NumberFormatUtils.format(data.bankCardCount),
            Icons.Default.AccountBalance,
            MaterialTheme.colorScheme.tertiary
        ),
        StatItem(
            stringResource(R.string.report_car),
            NumberFormatUtils.format(data.totalCarExpenses),
            Icons.Default.DirectionsCar,
            Color(0xFF9C27B0)
        ),
        StatItem(
            stringResource(R.string.report_assets),
            NumberFormatUtils.format(data.assetCount),
            Icons.Default.AccountBalanceWallet,
            Color(0xFF009688)
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
    val value: String,
    val icon: ImageVector,
    val color: Color
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    fontFamily = vazirFontFamily
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BarChartCard(
    monthlyExpenses: List<MonthAmount>,
    monthlyIncomes: List<MonthAmount>
) {
    val isDark = isSystemInDarkTheme()
    val expenseColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val incomeColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val allMonths = (monthlyExpenses.map { Pair(it.month, it.year) } + monthlyIncomes.map {
        Pair(
            it.month,
            it.year
        )
    })
        .distinct()
        .sortedWith(compareBy({ it.second }, { it.first }))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.7f
            )
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
                        .background(incomeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.report_income),
                    fontSize = 11.sp,
                    fontFamily = vazirFontFamily,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(expenseColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.report_expense),
                    fontSize = 11.sp,
                    fontFamily = vazirFontFamily,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (allMonths.isEmpty()) {
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
                val maxVal =
                    (monthlyExpenses.maxOfOrNull { it.amount } ?: 0L)
                        .coerceAtLeast(monthlyIncomes.maxOfOrNull { it.amount } ?: 0L)
                        .coerceAtLeast(1L)

                val density = LocalDensity.current

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height - 30.dp.toPx()
                    val barCount = allMonths.size
                    val groupWidth = chartWidth / barCount.coerceAtLeast(1)
                    val barWidth = (groupWidth * 0.35f).coerceAtMost(24.dp.toPx())

                    for (i in 0..4) {
                        val y = chartHeight * i / 4
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1f
                        )
                        val labelVal = (maxVal * (4 - i) / 4)
                        drawContext.canvas.nativeCanvas.drawText(
                            NumberFormatUtils.format(labelVal),
                            4.dp.toPx(),
                            y + 12.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                    }

                    allMonths.forEachIndexed { index, (month, _) ->
                        val centerX = groupWidth * index + groupWidth / 2
                        val expAmount =
                            monthlyExpenses.find { it.month == month && it.year == allMonths[index].second }?.amount
                                ?: 0L
                        val incAmount =
                            monthlyIncomes.find { it.month == month && it.year == allMonths[index].second }?.amount
                                ?: 0L

                        val expHeight =
                            (expAmount.toFloat() / maxVal * chartHeight).coerceAtLeast(0f)
                        val incHeight =
                            (incAmount.toFloat() / maxVal * chartHeight).coerceAtLeast(0f)

                        drawRect(
                            color = expenseColor,
                            topLeft = Offset(centerX - barWidth, chartHeight - expHeight),
                            size = Size(barWidth, expHeight)
                        )
                        drawRect(
                            color = incomeColor,
                            topLeft = Offset(centerX + 2.dp.toPx(), chartHeight - incHeight),
                            size = Size(barWidth, incHeight)
                        )

                        val monthName = ShamsiDate.getMonthName(month)
                        drawContext.canvas.nativeCanvas.drawText(
                            monthName.take(3),
                            centerX,
                            size.height - 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = 10.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(data: ReportData) {
    val items = buildList {
        if (data.totalIncomes > 0) add(
            Triple(
                stringResource(R.string.report_income),
                data.totalIncomes,
                Color(0xFF4CAF50)
            )
        )
        if (data.totalExpenses > 0) add(
            Triple(
                stringResource(R.string.report_expense),
                data.totalExpenses,
                Color(0xFFF44336)
            )
        )
        if (data.totalDebt > 0) add(
            Triple(
                stringResource(R.string.report_debt),
                data.totalDebt,
                Color(0xFFFF9800)
            )
        )
        if (data.totalReceivable > 0) add(
            Triple(
                stringResource(R.string.report_receivable),
                data.totalReceivable,
                Color(0xFF2196F3)
            )
        )
        if (data.totalTripExpenses > 0) add(
            Triple(
                stringResource(R.string.report_trip_expenses),
                data.totalTripExpenses,
                Color(0xFF9C27B0)
            )
        )
        if (data.totalCarExpenses > 0) add(
            Triple(
                stringResource(R.string.report_car),
                data.totalCarExpenses,
                Color(0xFFE91E63)
            )
        )
        if (data.totalInstallments > 0) add(
            Triple(
                stringResource(R.string.report_installments),
                data.totalInstallments,
                Color(0xFF795548)
            )
        )
        if (data.totalAssetValue > 0) add(
            Triple(
                stringResource(R.string.report_assets_value),
                data.totalAssetValue,
                Color(0xFF009688)
            )
        )
    }

    val total = items.sumOf { it.second }.coerceAtLeast(1L)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.7f
            )
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (items.isEmpty()) {
                Text(
                    stringResource(R.string.report_no_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = vazirFontFamily
                )
            } else {
                items.forEach { (label, amount, color) ->
                    val fraction = amount.toFloat() / total
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
                            modifier = Modifier.weight(0.35f),
                            fontSize = 12.sp,
                            fontFamily = vazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction.coerceAtMost(1f))
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(color, color.copy(alpha = 0.6f))
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = NumberFormatUtils.toLocalizedDigits("%${(fraction * 100).toInt()}"),
                            modifier = Modifier.weight(0.15f),
                            fontSize = 11.sp,
                            fontFamily = vazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = NumberFormatUtils.format(amount),
                            modifier = Modifier.weight(0.25f),
                            fontSize = 11.sp,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearPickerDialog(
    currentYear: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val today = ShamsiDate.today()
    val currentYearVal = today.first
    val years = (currentYearVal - 10..currentYearVal + 1).toList().reversed()

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
