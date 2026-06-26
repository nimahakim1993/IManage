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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import com.nima.app.imanage.ui.theme.DebtDark
import com.nima.app.imanage.ui.theme.DebtLight
import com.nima.app.imanage.ui.theme.IncomeDark
import com.nima.app.imanage.ui.theme.IncomeLight
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    loanViewModel: LoanViewModel = koinViewModel(),
    bankCardViewModel: BankCardViewModel = koinViewModel()
) {

    val settingsDesc = stringResource(R.string.settings)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = "", actions = listOf(
                ToolbarAction(
                    icon = Icons.Outlined.Settings,
                    contentDescription = settingsDesc,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            ))
        )
    }

    val loans by loanViewModel.loans.collectAsState()
    val cards by bankCardViewModel.cards.collectAsState()

    val totalDebt = remember(loans) {
        loans.filter { it.type == LoanEntity.TYPE_DEBT }.sumOf { it.price }
    }
    val totalReceivable = remember(loans) {
        loans.filter { it.type == LoanEntity.TYPE_RECEIVABLE }.sumOf { it.price }
    }
    val netBalance = totalReceivable - totalDebt

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReportCard(
            totalDebt = totalDebt,
            totalReceivable = totalReceivable,
            netBalance = netBalance,
            bankAccountCount = cards.size
        )

        DashboardGrid(navController = navController)
    }
}

@Composable
private fun ReportCard(
    totalDebt: Long,
    totalReceivable: Long,
    netBalance: Long,
    bankAccountCount: Int
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val isDark = isSystemInDarkTheme()
    val debtColor = if (isDark) DebtDark else DebtLight
    val incomeColor = if (isDark) IncomeDark else IncomeLight
    val netBalanceColor = if (netBalance >= 0) incomeColor else debtColor

    val gradient = Brush.linearGradient(
        colors = listOf(
            primary,
            secondary.copy(alpha = 0.85f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = stringResource(R.string.home_report_title),
                            color = onPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = vazirFontFamily
                        )
                    }
                    Text(
                        text = stringResource(R.string.home_bank_accounts) + ": ${NumberFormatUtils.format(bankAccountCount.toLong())}",
                        color = onPrimary.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = vazirFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.home_net_balance),
                    color = onPrimary.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = vazirFontFamily
                )
                Text(
                    text = NumberFormatUtils.format(netBalance),
                    color = netBalanceColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    fontFamily = vazirFontFamily
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportStatTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.total_debt),
                        value = NumberFormatUtils.format(totalDebt),
                        accent = Color(0xFFFFCDD2)
                    )
                    ReportStatTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.total_receivable),
                        value = NumberFormatUtils.format(totalReceivable),
                        accent = Color(0xFFB3E5FC)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportStatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = vazirFontFamily
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = vazirFontFamily
        )
    }
}

@Composable
private fun DashboardGrid(navController: NavHostController) {
    val items = listOf(
        DashboardEntry(stringResource(R.string.financial_title), Icons.Outlined.Money) {
            navController.navigate(Screen.Financial.route)
        },
        DashboardEntry(stringResource(R.string.bank_account_home), Icons.Default.CreditCard) {
            navController.navigate(Screen.BankCards.route)
        },
        DashboardEntry(stringResource(R.string.shared_trip), Icons.Outlined.MonetizationOn) {},
        DashboardEntry(stringResource(R.string.note), Icons.Default.NoteAlt) {
            navController.navigate(Screen.Notes.route)
        },
        DashboardEntry(stringResource(R.string.assets), Icons.Default.Inventory2) {},
        DashboardEntry(stringResource(R.string.report), Icons.Default.Report) {},
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { entry ->
                    DashboardItem(
                        modifier = Modifier.weight(1f),
                        title = entry.title,
                        icon = entry.icon,
                        onClick = entry.onClick
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class DashboardEntry(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun DashboardItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontFamily = vazirFontFamily,
                maxLines = 1
            )
        }
    }
}
