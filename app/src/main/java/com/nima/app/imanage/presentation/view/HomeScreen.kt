package com.nima.app.imanage.presentation.view

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import com.nima.app.imanage.presentation.viewmodel.SettingsViewModel
import com.nima.app.imanage.ui.theme.DebtDark
import com.nima.app.imanage.ui.theme.DebtLight
import com.nima.app.imanage.ui.theme.IncomeDark
import com.nima.app.imanage.ui.theme.IncomeLight
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.BiometricHelper
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.SecurityManager
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun HomeScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    loanViewModel: LoanViewModel = koinViewModel(),
    bankCardViewModel: BankCardViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {

    val settingsDesc = stringResource(R.string.settings)
    val backupDesc = stringResource(R.string.backup_data)
    val aboutDesc = stringResource(R.string.about)
    val context = LocalContext.current

    var showAboutSheet by remember { mutableStateOf(false) }

    val createBackupFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { settingsViewModel.export(context, it) }
    }

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = "", actions = listOf(
                ToolbarAction(
                    icon = Icons.Default.Backup,
                    contentDescription = backupDesc,
                    onClick = {
                        val filename = "imanage_backup_${LocalDate.now()}.json"
                        createBackupFile.launch(filename)
                    }
                ),
                ToolbarAction(
                    icon = Icons.Outlined.Info,
                    contentDescription = aboutDesc,
                    onClick = { showAboutSheet = true }
                ),
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
        loans.filter { it.type == LoanEntity.TYPE_DEBT && !it.settled }.sumOf { it.price }
    }
    val totalReceivable = remember(loans) {
        loans.filter { it.type == LoanEntity.TYPE_RECEIVABLE && !it.settled }.sumOf { it.price }
    }
    val netBalance = totalReceivable - totalDebt

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

    if (showAboutSheet) {
        AboutSheet(onDismiss = { showAboutSheet = false })
    }
}

@Composable
private fun ReportCard(
    totalDebt: Long,
    totalReceivable: Long,
    netBalance: Long,
    bankAccountCount: Int
) {
    val isDark = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    val secondary =
        if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f) else MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = 0.85f
        )
    val onPrimary = MaterialTheme.colorScheme.onPrimary

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
    val context = LocalContext.current

    val biometricTitle = stringResource(R.string.biometric_module_title)
    val biometricSubtitle = stringResource(R.string.biometric_module_subtitle)

    val modules = SecurityManager.modules.map { module ->
        val isProtected = SecurityManager.isModuleProtected(context, module.key)
        DashboardEntry(
            title = stringResource(module.titleRes),
            icon = module.icon,
            color = module.color,
            onClick = {
                if (isProtected) {
                    val activity = context.findFragmentActivity()
                    if (activity != null) {
                        BiometricHelper.authenticate(
                            activity = activity,
                            title = biometricTitle,
                            subtitle = biometricSubtitle,
                            onSuccess = { navController.navigate(module.screen.route) },
                            onError = { }
                        )
                    } else {
                        navController.navigate(module.screen.route)
                    }
                } else {
                    navController.navigate(module.screen.route)
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        modules.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { entry ->
                    DashboardItem(
                        modifier = Modifier.weight(1f),
                        title = entry.title,
                        icon = entry.icon,
                        color = entry.color,
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
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun DashboardItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = vazirFontFamily,
                maxLines = 1
            )
        }
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? =
    when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> this.baseContext.findFragmentActivity()
        else -> null
    }
