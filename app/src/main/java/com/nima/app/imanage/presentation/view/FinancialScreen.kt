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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.FinancialSummaryViewModel
import com.nima.app.imanage.ui.component.chart.BarCanvasChart
import com.nima.app.imanage.ui.component.chart.BarCanvasItem
import com.nima.app.imanage.ui.component.chart.LineCanvasChart
import com.nima.app.imanage.ui.component.chart.LineCanvasSeries
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel


@Composable
fun FinancialScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: FinancialSummaryViewModel = koinViewModel()
) {

    val ledgerTitle = stringResource(R.string.financial_ledger)
    val reportDesc = stringResource(R.string.financial_report_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = ledgerTitle,
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.BarChart,
                        contentDescription = reportDesc,
                        onClick = { navController.navigate(Screen.FinancialReport.route) }
                    )
                )
            )
        )
    }

    val summaryData by viewModel.summaryData.collectAsState()

    val items = listOf(
        FinancialEntry(
            title = stringResource(R.string.debt) + " " + stringResource(R.string.receivable),
            subtitle = stringResource(R.string.financial_subtitle_debt),
            icon = Icons.Default.Handshake,
            color = Color(0xFFFF9800),
            onClick = { navController.navigate(Screen.Loans.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.expense),
            subtitle = stringResource(R.string.financial_subtitle_expense),
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFFF44336),
            onClick = { navController.navigate(Screen.Expenses.route) },
            showReportButton = true,
            onReportClick = { navController.navigate(Screen.ExpenseReport.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.income),
            subtitle = stringResource(R.string.financial_subtitle_income),
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50),
            onClick = { navController.navigate(Screen.Incomes.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.installment),
            subtitle = stringResource(R.string.financial_subtitle_installment),
            icon = Icons.Default.CalendarMonth,
            color = Color(0xFF009688),
            onClick = { navController.navigate(Screen.Installments.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.check),
            subtitle = stringResource(R.string.financial_subtitle_check),
            icon = Icons.Default.ReceiptLong,
            color = Color(0xFF5E35B1),
            onClick = { navController.navigate(Screen.Checks.route) },
            showReportButton = true,
            onReportClick = { navController.navigate(Screen.ChecksReport.route) }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(4.dp))

        FinancialSummaryCard(data = summaryData)

        items.forEach { entry ->
            FinancialItem(
                title = entry.title,
                subtitle = entry.subtitle,
                icon = entry.icon,
                color = entry.color,
                onClick = entry.onClick,
                showReportButton = entry.showReportButton,
                onReportClick = entry.onReportClick
            )
        }
    }
}

@Composable
private fun FinancialSummaryCard(
    data: com.nima.app.imanage.presentation.viewmodel.FinancialSummaryData
) {
    val isDark = LocalIsDarkTheme.current
    val expenseColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFE53935)
    val incomeColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF43A047)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val debtColor = Color(0xFFFF9800)
    val receivableColor = Color(0xFF2196F3)
    val installmentColor = Color(0xFF009688)
    val checkColor = Color(0xFF5E35B1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.financial_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
                Text(
                    text = ShamsiDate.toPersianDigits(
                        data.monthlyData.firstOrNull()?.year?.toString() ?: ""
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val hasData = data.monthlyData.any { it.expense > 0 || it.income > 0 }

            if (!hasData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
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
                        values = data.monthlyData.map { it.expense.toFloat() },
                        lineColor = expenseColor,
                        fillColor = expenseColor.copy(alpha = 0.1f),
                        dotRadiusDp = 3f,
                        innerDotRadiusDp = 1.5f
                    ),
                    LineCanvasSeries(
                        values = data.monthlyData.map { it.income.toFloat() },
                        lineColor = incomeColor,
                        fillColor = incomeColor.copy(alpha = 0.1f),
                        dotRadiusDp = 3f,
                        innerDotRadiusDp = 1.5f
                    )
                )
                val xLabels = data.monthlyData.map { ShamsiDate.getMonthName(it.month).take(1) }

                LineCanvasChart(
                    series = series,
                    xLabels = xLabels,
                    textColor = labelColor,
                    gridColor = gridColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    lineWidthDp = 2.5f,
                    bottomPaddingDp = 24f
                )
            }

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

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            val moduleItems = listOf(
                ModuleBarItem(
                    label = stringResource(R.string.report_debt),
                    value = data.moduleData.debt,
                    color = debtColor
                ),
                ModuleBarItem(
                    label = stringResource(R.string.report_receivable),
                    value = data.moduleData.receivable,
                    color = receivableColor
                ),
                ModuleBarItem(
                    label = stringResource(R.string.installment),
                    value = data.moduleData.installment,
                    color = installmentColor
                ),
                ModuleBarItem(
                    label = stringResource(R.string.check),
                    value = data.moduleData.check,
                    color = checkColor
                )
            )

            val barItems = moduleItems.map { item ->
                BarCanvasItem(
                    label = item.label,
                    values = listOf(item.value.toFloat() to item.color)
                )
            }

            BarCanvasChart(
                items = barItems,
                textColor = labelColor,
                gridColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                showGridLines = false,
                roundedCorners = true,
                bottomPaddingDp = 20f,
                topPaddingDp = 0f
            )
        }
    }
}

private data class ModuleBarItem(
    val label: String,
    val value: Long,
    val color: Color
)

private data class FinancialEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
    val showReportButton: Boolean = false,
    val onReportClick: () -> Unit = {}
)

@Composable
fun FinancialItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    showReportButton: Boolean = false,
    onReportClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showReportButton) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.12f))
                        .clickable(onClick = onReportClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = stringResource(R.string.financial_expense_report),
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
