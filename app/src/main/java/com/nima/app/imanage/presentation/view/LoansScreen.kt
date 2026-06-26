package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.DebtDark
import com.nima.app.imanage.ui.theme.DebtLight
import com.nima.app.imanage.ui.theme.IncomeDark
import com.nima.app.imanage.ui.theme.IncomeLight
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoansScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    viewModel: LoanViewModel = koinViewModel()
) {

    val loans by viewModel.loans.collectAsState()

    val loansTitle = stringResource(R.string.loans_title)
    val addDesc = stringResource(R.string.add)
    val filterDesc = stringResource(R.string.filter)
    val editDesc = stringResource(R.string.edit)

    var toggleEditMode by rememberSaveable { mutableStateOf(false) }
    var removingLoan by remember { mutableStateOf<LoanEntity?>(null) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showDebts by rememberSaveable { mutableStateOf(true) }
    var showReceivables by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(toggleEditMode) {
        setToolbar(
            ToolbarConfig(title = loansTitle, showBack = true, actions = listOf(
                ToolbarAction(
                    icon = Icons.Default.Add,
                    contentDescription = addDesc,
                    onClick = {
                        navController.navigate(Screen.CreateLoan.createRoute())
                    }
                ),
                ToolbarAction(
                    icon = Icons.Default.FilterAlt,
                    contentDescription = filterDesc,
                    onClick = {
                        showFilterDialog = true
                    }
                ),
                ToolbarAction(
                    icon = if (toggleEditMode)
                        Icons.Default.EditOff
                    else
                        Icons.Default.Edit,
                    contentDescription = editDesc,
                    onClick = { toggleEditMode = !toggleEditMode }
                ),
            ))
        )
    }

    val filteredLoans = remember(loans, searchQuery, showDebts, showReceivables) {
        loans.filter { loan ->
            val typeMatch = when (loan.type) {
                LoanEntity.TYPE_DEBT -> showDebts
                LoanEntity.TYPE_RECEIVABLE -> showReceivables
                else -> true
            }
            val queryMatch = if (searchQuery.isBlank()) {
                true
            } else {
                loan.targetPersonName.contains(searchQuery, ignoreCase = true) ||
                        loan.description.contains(searchQuery, ignoreCase = true)
            }
            typeMatch && queryMatch
        }
    }

    val (totalDebt, totalReceivable) = remember(filteredLoans) {
        val debt = filteredLoans.filter { it.type == LoanEntity.TYPE_DEBT }.sumOf { it.price }
        val receivable = filteredLoans.filter { it.type == LoanEntity.TYPE_RECEIVABLE }.sumOf { it.price }
        debt to receivable
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TotalsCard(
            modifier = Modifier.padding(16.dp),
            totalDebt = totalDebt,
            totalReceivable = totalReceivable
        )

        if (loans.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.empty_loans),
                hint = stringResource(R.string.empty_loans_hint),
                actionLabel = stringResource(R.string.add),
                onAction = { navController.navigate(Screen.CreateLoan.createRoute()) }
            )
        } else if (filteredLoans.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FilterAlt,
                title = stringResource(R.string.empty_loans_filtered),
                hint = stringResource(R.string.empty_loans_filtered_hint)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredLoans, key = { it.id }) { loan ->
                    LoanItem(
                        loan = loan,
                        editMode = toggleEditMode,
                        onEdit = {
                            navController.navigate(Screen.CreateLoan.createRoute(loan.id))
                        },
                        onDelete = {
                            removingLoan = loan
                        }
                    )
                }
            }
        }
    }

    removingLoan?.let { loan ->
        ActionDialog(
            onDismiss = { removingLoan = null },
            onPositiveClicked = {
                viewModel.removeLoan(loan)
                removingLoan = null
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            showDebts = showDebts,
            onShowDebtsChange = { showDebts = it },
            showReceivables = showReceivables,
            onShowReceivablesChange = { showReceivables = it },
            onDismiss = { showFilterDialog = false },
            onClear = {
                searchQuery = ""
                showDebts = true
                showReceivables = true
            }
        )
    }
}

@Composable
private fun TotalsCard(
    modifier: Modifier = Modifier,
    totalDebt: Long,
    totalReceivable: Long
) {
    val isDark = isSystemInDarkTheme()
    val debtColor = if (isDark) DebtDark else DebtLight
    val incomeColor = if (isDark) IncomeDark else IncomeLight

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.total_debt),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = NumberFormatUtils.format(totalDebt),
                    color = debtColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.total_receivable),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = NumberFormatUtils.format(totalReceivable),
                    color = incomeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoanItem(
    loan: LoanEntity,
    editMode: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val debtColor = if (isDark) DebtDark else DebtLight
    val incomeColor = if (isDark) IncomeDark else IncomeLight
    val baseColor = if (loan.type == LoanEntity.TYPE_DEBT) debtColor else incomeColor
    val accentColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f)

    val gradient = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(alpha = 0.78f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = loan.targetPersonName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        fontFamily = vazirFontFamily
                    )
                    Text(
                        text = NumberFormatUtils.format(loan.price),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        fontFamily = vazirFontFamily
                    )
                }

                if (loan.description.isNotBlank()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = loan.description,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                            fontFamily = vazirFontFamily,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.size(14.dp))

                DateRow(
                    iconTint = Color.White.copy(alpha = 0.85f),
                    text = stringResource(
                        R.string.payment_date_prefix,
                        dateFormat.format(Date(loan.dateLoan))
                    )
                )
                Spacer(modifier = Modifier.size(6.dp))
                DateRow(
                    iconTint = Color.White.copy(alpha = 0.85f),
                    text = stringResource(
                        R.string.receive_date_prefix,
                        dateFormat.format(Date(loan.dateReceiveBack))
                    )
                )

                AnimatedVisibility(visible = editMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRow(iconTint: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            color = iconTint,
            fontSize = 11.sp,
            fontFamily = vazirFontFamily
        )
    }
}

@Composable
private fun FilterDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showDebts: Boolean,
    onShowDebtsChange: (Boolean) -> Unit,
    showReceivables: Boolean,
    onShowReceivablesChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.filter_title)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text(stringResource(R.string.search_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showDebts,
                            onCheckedChange = onShowDebtsChange
                        )
                        Text(
                            text = stringResource(R.string.show_debts),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showReceivables,
                            onCheckedChange = onShowReceivablesChange
                        )
                        Text(
                            text = stringResource(R.string.show_receivables),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.clear))
                }
            }
        )
    }
}
