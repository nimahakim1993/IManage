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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.presentation.viewmodel.CategoryAmount
import com.nima.app.imanage.presentation.viewmodel.CategoryComparison
import com.nima.app.imanage.presentation.viewmodel.ExpenseReportViewModel
import com.nima.app.imanage.presentation.viewmodel.MonthComparison
import com.nima.app.imanage.presentation.viewmodel.MonthlyTrend
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.ColorUtils
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: ExpenseReportViewModel = koinViewModel()
) {
    val reportTitle = stringResource(R.string.expense_report_title)

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

    // Category comparison month selectors
    val (todayJy, todayJm, _) = ShamsiDate.today()
    val prevJm = if (todayJm > 1) todayJm - 1 else 12
    val prevJy = if (todayJm > 1) todayJy else todayJy - 1

    var comparisonMonth1 by remember { mutableStateOf<Pair<Int, Int>?>(todayJm to todayJy) }
    var comparisonMonth2 by remember { mutableStateOf<Pair<Int, Int>?>(prevJm to prevJy) }
    var showComparisonPicker1 by remember { mutableStateOf(false) }
    var showComparisonPicker2 by remember { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                ExpenseReportFilterChipRow(
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
                    ExpenseReportCustomRangeRow(
                        fromDate = customFrom,
                        toDate = customTo,
                        onFromClick = { showCustomFromPicker = true },
                        onToClick = { showCustomToPicker = true }
                    )
                }
            }

            item {
                ExpenseIncomeDonutCard(
                    totalExpenses = data.totalExpenses,
                    totalIncomes = data.totalIncomes
                )
            }

            if (data.categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.expense_category_distribution),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                }
                item {
                    CategoryDonutChartCard(
                        categories = data.categoryBreakdown,
                        total = data.totalExpenses
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.expense_month_comparison),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }
            item {
                MonthComparisonBarCard(monthComparison = data.monthComparison)
            }

            if (data.categoryComparison.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.expense_category_month_comparison),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                }
                item {
                    CategoryComparisonBarCard(
                        comparisons = data.categoryComparison,
                        month1Label = comparisonMonth1?.let { (m, y) ->
                            "${ShamsiDate.getMonthName(m)} ${ShamsiDate.toPersianDigits(y.toString())}"
                        } ?: "",
                        month2Label = comparisonMonth2?.let { (m, y) ->
                            "${ShamsiDate.getMonthName(m)} ${ShamsiDate.toPersianDigits(y.toString())}"
                        } ?: "",
                        onMonth1Click = { showComparisonPicker1 = true },
                        onMonth2Click = { showComparisonPicker2 = true }
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.expense_monthly_trend),
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

    if (showComparisonPicker1) {
        ShamsiMonthYearPicker(
            initialMonth = comparisonMonth1?.first,
            initialYear = comparisonMonth1?.second,
            onConfirm = { month, year ->
                if (month != null) {
                    comparisonMonth1 = month to year
                    viewModel.setCategoryComparisonMonths(comparisonMonth1, comparisonMonth2)
                }
                showComparisonPicker1 = false
            },
            onDismiss = { showComparisonPicker1 = false }
        )
    }

    if (showComparisonPicker2) {
        ShamsiMonthYearPicker(
            initialMonth = comparisonMonth2?.first,
            initialYear = comparisonMonth2?.second,
            onConfirm = { month, year ->
                if (month != null) {
                    comparisonMonth2 = month to year
                    viewModel.setCategoryComparisonMonths(comparisonMonth1, comparisonMonth2)
                }
                showComparisonPicker2 = false
            },
            onDismiss = { showComparisonPicker2 = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseReportFilterChipRow(
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
private fun ExpenseReportCustomRangeRow(
    fromDate: Long,
    toDate: Long,
    onFromClick: () -> Unit,
    onToClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ExpenseReportDateField(
            label = stringResource(R.string.report_from_date),
            date = fromDate,
            onClick = onFromClick,
            modifier = Modifier.weight(1f)
        )
        ExpenseReportDateField(
            label = stringResource(R.string.report_to_date),
            date = toDate,
            onClick = onToClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExpenseReportDateField(
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
private fun ExpenseIncomeDonutCard(
    totalExpenses: Long,
    totalIncomes: Long
) {
    val isDark = LocalIsDarkTheme.current
    val expenseColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val incomeColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val total = totalExpenses + totalIncomes

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
            Text(
                text = stringResource(R.string.expense_vs_income),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (total > 0) {
                        val strokeWidth = size.minDimension * 0.22f
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                        val expenseAngle =
                            if (total > 0) (totalExpenses.toDouble() / total.toDouble()) * 360.0 else 0.0
                        val incomeAngle = 360.0 - expenseAngle

                        var startAngle = -90f
                        drawArc(
                            color = expenseColor,
                            startAngle = startAngle,
                            sweepAngle = expenseAngle.toFloat(),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += expenseAngle.toFloat()
                        drawArc(
                            color = incomeColor,
                            startAngle = startAngle,
                            sweepAngle = incomeAngle.toFloat(),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val netBalance = totalIncomes - totalExpenses
                    Text(
                        text = NumberFormatUtils.format(netBalance),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = scaledSp(16f),
                        fontFamily = vazirFontFamily,
                        color = if (netBalance >= 0) incomeColor else expenseColor
                    )
                    Text(
                        text = stringResource(R.string.home_net_balance),
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
                    color = expenseColor,
                    label = stringResource(R.string.report_expense),
                    value = NumberFormatUtils.format(totalExpenses)
                )
                LegendItem(
                    color = incomeColor,
                    label = stringResource(R.string.report_income),
                    value = NumberFormatUtils.format(totalIncomes)
                )
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
private fun CategoryDonutChartCard(
    categories: List<CategoryAmount>,
    total: Long
) {
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
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = size.minDimension * 0.22f
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                    var startAngle = -90f
                    categories.forEachIndexed { index, cat ->
                        val sweepAngle = (cat.percent / 100f) * 360f
                        val palette = ColorUtils.palettes.getOrElse(cat.colorIndex) {
                            ColorUtils.palettes.first()
                        }
                        drawArc(
                            color = palette.primary,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = NumberFormatUtils.format(total),
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

            Spacer(modifier = Modifier.height(12.dp))

            categories.forEachIndexed { index, cat ->
                val palette = ColorUtils.palettes.getOrElse(cat.colorIndex) {
                    ColorUtils.palettes.first()
                }
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
                            .background(palette.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cat.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = vazirFontFamily,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${ShamsiDate.toPersianDigits(cat.percent.toInt().toString())}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = NumberFormatUtils.format(cat.amount),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthComparisonBarCard(monthComparison: MonthComparison) {
    val isDark = LocalIsDarkTheme.current
    val currentColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E3A8A)
    val lastYearColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF4C1D95)
    val prevColor = if (isDark) Color(0xFFFB923C) else Color(0xFF7C2D12)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val items = listOf(
        Triple(monthComparison.currentMonthLabel, monthComparison.currentMonthAmount, currentColor),
        Triple(
            monthComparison.lastYearMonthLabel,
            monthComparison.sameMonthLastYearAmount,
            lastYearColor
        ),
        Triple(monthComparison.previousMonthLabel, monthComparison.previousMonthAmount, prevColor)
    )
    val maxVal = items.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartHeight = size.height - 40.dp.toPx()
                val barCount = items.size
                val groupWidth = size.width / barCount.coerceAtLeast(1)
                val barWidth = (groupWidth * 0.5f).coerceAtMost(48.dp.toPx())

                for (i in 0..4) {
                    val y = chartHeight * i / 4
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                items.forEachIndexed { index, (label, amount, color) ->
                    val centerX = groupWidth * index + groupWidth / 2
                    val barHeight = (amount.toFloat() / maxVal * chartHeight).coerceAtLeast(0f)

                    drawRect(
                        color = color,
                        topLeft = Offset(centerX - barWidth / 2, chartHeight - barHeight),
                        size = Size(barWidth, barHeight)
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        NumberFormatUtils.format(amount),
                        centerX,
                        chartHeight - barHeight - 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            this.color = textColor.hashCode()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            this.isFakeBoldText = true
                        }
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        centerX,
                        size.height - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            this.color = textColor.hashCode()
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { (label, _, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
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

@Composable
private fun CategoryComparisonBarCard(
    comparisons: List<CategoryComparison>,
    month1Label: String,
    month2Label: String,
    onMonth1Click: () -> Unit,
    onMonth2Click: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val currentColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E3A8A)
    val lastColor = if (isDark) Color(0xFFFB923C) else Color(0xFF7C2D12)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val maxVal = comparisons.maxOf {
        it.currentMonthAmount.coerceAtLeast(it.lastMonthAmount)
    }.coerceAtLeast(1L)

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
                        .background(currentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = month1Label,
                    fontSize = scaledSp(11f),
                    fontFamily = vazirFontFamily,
                    color = currentColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onMonth1Click)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(lastColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = month2Label,
                    fontSize = scaledSp(11f),
                    fontFamily = vazirFontFamily,
                    color = lastColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onMonth2Click)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            comparisons.forEach { comp ->
                val palette = ColorUtils.palettes.getOrElse(comp.colorIndex) {
                    ColorUtils.palettes.first()
                }
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
                            .background(palette.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = comp.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = vazirFontFamily,
                        modifier = Modifier.width(60.dp),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val currentFrac =
                            (comp.currentMonthAmount.toFloat() / maxVal).coerceIn(0f, 1f)
                        val lastFrac = (comp.lastMonthAmount.toFloat() / maxVal).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(gridColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(currentFrac)
                                    .fillMaxSize()
                                    .background(currentColor, RoundedCornerShape(6.dp))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(gridColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(lastFrac)
                                    .fillMaxSize()
                                    .background(lastColor, RoundedCornerShape(6.dp))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = NumberFormatUtils.format(comp.currentMonthAmount),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = vazirFontFamily,
                            color = currentColor
                        )
                        Text(
                            text = NumberFormatUtils.format(comp.lastMonthAmount),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = vazirFontFamily,
                            color = lastColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendLineCard(trend: List<MonthlyTrend>) {
    val isDark = LocalIsDarkTheme.current
    val lineColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E3A8A)
    val fillColor =
        if (isDark) Color(0xFF60A5FA).copy(alpha = 0.15f) else Color(0xFF1E3A8A).copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val dotColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E3A8A)

    val maxVal = trend.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
    val hasData = trend.any { it.amount > 0 }

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
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val chartHeight = size.height - 30.dp.toPx()
                    val chartWidth = size.width
                    val stepX = chartWidth / (trend.size - 1).coerceAtLeast(1)

                    for (i in 0..4) {
                        val y = chartHeight * i / 4
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    val points = trend.mapIndexed { index, t ->
                        val x = stepX * index
                        val y =
                            chartHeight - (t.amount.toFloat() / maxVal * chartHeight).coerceAtLeast(
                                0f
                            )
                        Offset(x, y)
                    }

                    if (points.size >= 2) {
                        val fillPath = Path().apply {
                            moveTo(points.first().x, chartHeight)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, chartHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            color = fillColor
                        )

                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = lineColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        points.forEach { point ->
                            drawCircle(
                                color = dotColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }

                    trend.forEachIndexed { index, t ->
                        val x = stepX * index
                        drawContext.canvas.nativeCanvas.drawText(
                            ShamsiDate.getMonthName(t.month).take(3),
                            x,
                            size.height - 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                this.color = textColor.hashCode()
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}
