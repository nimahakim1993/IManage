package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.LocalAppColors
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

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
    var showSettled by rememberSaveable { mutableStateOf(true) }
    var showUnsettled by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(loans.isEmpty()) {
        if (loans.isEmpty()) toggleEditMode = false
    }

    LaunchedEffect(toggleEditMode, loans.isEmpty()) {
        val actions = mutableListOf(
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
            )
        )
        if (loans.isNotEmpty()) {
            actions.add(
                ToolbarAction(
                    icon = if (toggleEditMode)
                        Icons.Default.EditOff
                    else
                        Icons.Default.Edit,
                    contentDescription = editDesc,
                    onClick = { toggleEditMode = !toggleEditMode }
                )
            )
        }
        setToolbar(ToolbarConfig(title = loansTitle, showBack = true, actions = actions))
    }

    val filteredLoans = remember(loans, searchQuery, showDebts, showReceivables, showSettled, showUnsettled) {
        loans.filter { loan ->
            val typeMatch = when (loan.type) {
                LoanEntity.TYPE_DEBT -> showDebts
                LoanEntity.TYPE_RECEIVABLE -> showReceivables
                else -> true
            }
            val settledMatch = when {
                loan.settled -> showSettled
                else -> showUnsettled
            }
            val queryMatch = if (searchQuery.isBlank()) {
                true
            } else {
                loan.targetPersonName.contains(searchQuery, ignoreCase = true) ||
                        loan.description.contains(searchQuery, ignoreCase = true)
            }
            typeMatch && settledMatch && queryMatch
        }
    }

    val (totalDebt, totalReceivable) = remember(loans) {
        val debt =
            loans.filter { it.type == LoanEntity.TYPE_DEBT && !it.settled }.sumOf { it.price }
        val receivable =
            loans.filter { it.type == LoanEntity.TYPE_RECEIVABLE && !it.settled }.sumOf { it.price }
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
                        },
                        onToggleSettled = {
                            viewModel.toggleSettled(loan)
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
            showSettled = showSettled,
            onShowSettledChange = { showSettled = it },
            showUnsettled = showUnsettled,
            onShowUnsettledChange = { showUnsettled = it },
            onDismiss = { showFilterDialog = false },
            onClear = {
                searchQuery = ""
                showDebts = true
                showReceivables = true
                showSettled = true
                showUnsettled = true
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
    val debtColor = LocalAppColors.current.debt
    val incomeColor = LocalAppColors.current.income

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
    onDelete: () -> Unit = {},
    onToggleSettled: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    val debtColor = LocalAppColors.current.debt
    val incomeColor = LocalAppColors.current.income
    val settledColor = if (isDark) Color(0xFF1565C0) else Color(0xFF1976D2)

    val targetBaseColor = when {
        loan.settled -> settledColor
        loan.type == LoanEntity.TYPE_DEBT -> debtColor
        else -> incomeColor
    }
    val baseColor by animateColorAsState(
        targetValue = targetBaseColor,
        animationSpec = tween(durationMillis = 700),
        label = "baseColor"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (loan.settled) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    val checkAlpha by animateFloatAsState(
        targetValue = if (loan.settled) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "checkAlpha"
    )

    val accentColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f)

    val gradient = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(alpha = 0.78f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val settledIconColor by animateColorAsState(
        targetValue = if (loan.settled) Color.White else Color.White.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 500),
        label = "settledIconColor"
    )

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = loan.targetPersonName,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = scaledSp(16f),
                            fontFamily = vazirFontFamily,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        AnimatedVisibility(
                            visible = loan.settled,
                            enter = scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + fadeIn(tween(200)),
                            exit = fadeOut(tween(150))
                        ) {
                            Row {
                                Spacer(modifier = Modifier.size(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.settled),
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .scale(checkScale)
                                )
                            }
                        }
                    }
                    Text(
                        text = NumberFormatUtils.format(loan.price),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = scaledSp(22f),
                        fontFamily = vazirFontFamily
                    )
                }

                if (loan.description.isNotBlank()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = loan.description,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = scaledSp(13f),
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
                        ShamsiDate.format(loan.dateLoan)
                    )
                )
                Spacer(modifier = Modifier.size(6.dp))
                DateRow(
                    iconTint = Color.White.copy(alpha = 0.85f),
                    text = stringResource(
                        R.string.receive_date_prefix,
                        ShamsiDate.format(loan.dateReceiveBack)
                    )
                )

                Spacer(modifier = Modifier.size(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onToggleSettled)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = settledIconColor,
                        modifier = Modifier
                            .size(18.dp)
                            .scale(if (loan.settled) checkScale else 1f)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(
                            if (loan.settled) R.string.settled_on else R.string.mark_settled
                        ),
                        color = Color.White,
                        fontSize = scaledSp(12f),
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }

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
            fontSize = scaledSp(11f),
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
    showSettled: Boolean,
    onShowSettledChange: (Boolean) -> Unit,
    showUnsettled: Boolean,
    onShowUnsettledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.filter)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text(stringResource(R.string.search_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.loan_type),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.status),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showSettled,
                            onCheckedChange = onShowSettledChange
                        )
                        Text(
                            text = stringResource(R.string.show_settled),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showUnsettled,
                            onCheckedChange = onShowUnsettledChange
                        )
                        Text(
                            text = stringResource(R.string.show_unsettled),
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
