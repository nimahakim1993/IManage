package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.CheckViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
import com.nima.app.imanage.ui.theme.LocalAppColors
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecksScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: CheckViewModel = koinViewModel()
) {
    val checks by viewModel.checks.collectAsState()
    val counterparties by viewModel.counterparties.collectAsState()
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var editMode by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CheckEntity?>(null) }
    var deleting by remember { mutableStateOf<CheckEntity?>(null) }
    var changingState by remember { mutableStateOf<CheckEntity?>(null) }
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedStates by remember { mutableStateOf(emptySet<String>()) }
    var selectedCounterparty by rememberSaveable { mutableStateOf<String?>(null) }
    var checkNumberQuery by rememberSaveable { mutableStateOf("") }
    var selectedMonthYear by rememberSaveable { mutableStateOf<Pair<Int, Int>?>(null) }
    var showMonthYearPicker by rememberSaveable { mutableStateOf(false) }

    val title = stringResource(R.string.check)
    val addDescription = stringResource(R.string.add)
    val filterDescription = stringResource(R.string.filter)
    val editDescription = stringResource(R.string.edit)
    val counterParties = stringResource(R.string.check_manage_counterparties)
    LaunchedEffect(checks.size, editMode) {
        setToolbar(
            ToolbarConfig(
                title = title,
                showBack = true,
                actions = listOf(
                    ToolbarAction(Icons.Default.Add, addDescription) {
                        editing = null
                        showSheet = true
                    },
                    ToolbarAction(Icons.Default.FilterAlt, filterDescription) {
                        showFilter = true
                    },
                    ToolbarAction(Icons.Default.Tune, counterParties) {
                        navController.navigate(Screen.CheckCounterparties.route)
                    },
                    ToolbarAction(
                        if (editMode) Icons.Default.EditOff else Icons.Default.Edit,
                        editDescription
                    ) { editMode = !editMode }
                )
            )
        )
    }

    val filtered = checks.filter { check ->
        val dateMatch = selectedMonthYear?.let { (month, year) ->
            val (jy, jm, _) = ShamsiDate.fromMillis(check.dueDate)
            jy == year && jm == month
        } ?: true
        (selectedType == null || check.type == selectedType) &&
                (selectedStates.isEmpty() || check.state in selectedStates) &&
                (selectedCounterparty == null || check.counterparty == selectedCounterparty) &&
                (checkNumberQuery.isBlank() ||
                        check.checkNumber.contains(checkNumberQuery.trim(), ignoreCase = true)) &&
                dateMatch
    }

    val totalReceived = remember(filtered) {
        filtered.filter { it.type == CheckEntity.TYPE_RECEIVED }.sumOf { it.amount }
    }
    val totalPayable = remember(filtered) {
        filtered.filter { it.type == CheckEntity.TYPE_PAYABLE }.sumOf { it.amount }
    }
    val receivedCount = remember(filtered) {
        filtered.count { it.type == CheckEntity.TYPE_RECEIVED }
    }
    val payableCount = remember(filtered) {
        filtered.count { it.type == CheckEntity.TYPE_PAYABLE }
    }

    Column(Modifier.fillMaxSize()) {
        ChecksTotalsCard(
            modifier = Modifier.padding(16.dp),
            totalReceived = totalReceived,
            totalPayable = totalPayable,
            receivedCount = receivedCount,
            payableCount = payableCount,
            selectedMonthYear = selectedMonthYear,
            onDateFilterClick = { showMonthYearPicker = true },
            onClearDateFilter = { selectedMonthYear = null }
        )

        if (checks.isEmpty()) {
            EmptyState(
                icon = Icons.Default.ReceiptLong,
                title = stringResource(R.string.empty_checks),
                hint = stringResource(R.string.empty_checks_hint),
                actionLabel = stringResource(R.string.add),
                onAction = { editing = null; showSheet = true }
            )
        } else if (filtered.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FilterAlt,
                title = stringResource(R.string.empty_checks_filtered),
                hint = stringResource(R.string.empty_checks_filtered)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { check ->
                    CheckItem(
                        check = check,
                        editMode = editMode,
                        onEdit = { editing = check; showSheet = true },
                        onDelete = { deleting = check },
                        onChangeState = { changingState = check }
                    )
                }
            }
        }
    }

    if (showSheet) {
        CreateCheckSheet(
            counterparties = counterparties,
            onAddCounterparty = viewModel::addCounterparty,
            editing = editing,
            onDismiss = { showSheet = false },
            onSave = { viewModel.save(it); showSheet = false }
        )
    }
    deleting?.let { check ->
        ActionDialog(
            onDismiss = { deleting = null },
            onPositiveClicked = { viewModel.delete(check); deleting = null }
        )
    }
    changingState?.let { check ->
        ChangeStateDialog(
            currentState = check.state,
            onSelect = { state -> viewModel.updateState(check, state); changingState = null },
            onDismiss = { changingState = null }
        )
    }
    if (showFilter) {
        CheckFilterDialog(
            checks = checks,
            selectedType = selectedType,
            selectedStates = selectedStates,
            selectedCounterparty = selectedCounterparty,
            checkNumberQuery = checkNumberQuery,
            onTypeChanged = { selectedType = it },
            onStateToggle = { state ->
                selectedStates =
                    if (state in selectedStates) selectedStates - state else selectedStates + state
            },
            onCounterpartyChanged = { selectedCounterparty = it },
            onCheckNumberChanged = { checkNumberQuery = it },
            onClear = {
                selectedType = null
                selectedStates = emptySet()
                selectedCounterparty = null
                checkNumberQuery = ""
            },
            onDismiss = { showFilter = false }
        )
    }
    if (showMonthYearPicker) {
        ShamsiMonthYearPicker(
            initialMonth = selectedMonthYear?.first,
            initialYear = selectedMonthYear?.second,
            onConfirm = { month, year ->
                selectedMonthYear = Pair(month, year)
                showMonthYearPicker = false
            },
            onDismiss = { showMonthYearPicker = false }
        )
    }
}

@Composable
private fun ChecksTotalsCard(
    modifier: Modifier = Modifier,
    totalReceived: Long,
    totalPayable: Long,
    receivedCount: Int,
    payableCount: Int,
    selectedMonthYear: Pair<Int, Int>?,
    onDateFilterClick: () -> Unit,
    onClearDateFilter: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    val gradient = Brush.linearGradient(
        colors = listOf(primary, secondary.copy(alpha = 0.85f)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val dateFilterLabel = selectedMonthYear?.let { (month, year) ->
        "${ShamsiDate.getMonthName(month)} ${ShamsiDate.toPersianDigits(year.toString())}"
    } ?: stringResource(R.string.no_filter)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.check_received) +
                                    " (" + NumberFormatUtils.toLocalizedDigits(receivedCount.toString()) + ")",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = vazirFontFamily
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = NumberFormatUtils.format(totalReceived),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = scaledSp(22f),
                            fontFamily = vazirFontFamily
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.check_payable) +
                                    " (" + NumberFormatUtils.toLocalizedDigits(payableCount.toString()) + ")",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = vazirFontFamily
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = NumberFormatUtils.format(totalPayable),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = scaledSp(22f),
                            fontFamily = vazirFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onDateFilterClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = dateFilterLabel,
                        color = Color.White,
                        fontSize = scaledSp(13f),
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedMonthYear != null) {
                        Icon(
                            imageVector = Icons.Default.EditOff,
                            contentDescription = stringResource(R.string.clear),
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onClearDateFilter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckItem(
    check: CheckEntity,
    editMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeState: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val typeColor = if (check.type == CheckEntity.TYPE_RECEIVED) {
        LocalAppColors.current.income
    } else {
        LocalAppColors.current.debt
    }
    val typeIcon = if (check.type == CheckEntity.TYPE_RECEIVED) {
        Icons.Default.TrendingUp
    } else {
        Icons.Default.TrendingDown
    }
    val stateColor = checkStateColor(check.state)

    val glassBorder =
        if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.95f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val textMuted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, glassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(typeColor.copy(alpha = if (isDark) 0.22f else 0.18f))
                                .border(1.5.dp, typeColor.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = check.counterparty,
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = scaledSp(16f),
                                fontFamily = vazirFontFamily,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = checkTypeLabel(check.type),
                                    color = typeColor,
                                    fontSize = scaledSp(12f),
                                    fontFamily = vazirFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Text(
                            text = NumberFormatUtils.format(check.amount),
                            color = typeColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = scaledSp(18f),
                            fontFamily = vazirFontFamily,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (check.description.isNotBlank()) {
                        Spacer(modifier = Modifier.size(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isDark) Color.White.copy(alpha = 0.08f)
                                    else Color.Black.copy(alpha = 0.05f)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = check.description,
                                color = textSecondary,
                                fontSize = scaledSp(12f),
                                fontFamily = vazirFontFamily,
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.check_number_value, check.checkNumber),
                            color = textMuted,
                            fontSize = scaledSp(11f),
                            fontFamily = vazirFontFamily
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = ShamsiDate.format(check.dueDate),
                                color = textMuted,
                                fontSize = scaledSp(11f),
                                fontFamily = vazirFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(stateColor.copy(alpha = if (isDark) 0.22f else 0.16f))
                            .border(1.dp, stateColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onChangeState)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(stateColor)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = checkStateLabel(check.state),
                                color = stateColor,
                                fontSize = scaledSp(12f),
                                fontFamily = vazirFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = stateColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (editMode) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckFilterDialog(
    checks: List<CheckEntity>,
    selectedType: String?,
    selectedStates: Set<String>,
    selectedCounterparty: String?,
    checkNumberQuery: String,
    onTypeChanged: (String?) -> Unit,
    onStateToggle: (String) -> Unit,
    onCounterpartyChanged: (String?) -> Unit,
    onCheckNumberChanged: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.check_filter)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.check_filter_type),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = vazirFontFamily
                    )
                    listOf(
                        null to stringResource(R.string.check_filter_all),
                        CheckEntity.TYPE_RECEIVED to stringResource(R.string.check_received),
                        CheckEntity.TYPE_PAYABLE to stringResource(R.string.check_payable)
                    ).forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onTypeChanged(key) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == key,
                                onClick = { onTypeChanged(key) }
                            )
                            Text(
                                text = label,
                                fontFamily = vazirFontFamily,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(4.dp))

                    Text(
                        text = stringResource(R.string.check_state),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = vazirFontFamily
                    )
                    checkStates().forEach { (state, label) ->
                        val key = state ?: return@forEach
                        val color = checkStateColor(key)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = key in selectedStates,
                                onCheckedChange = { onStateToggle(key) }
                            )
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = label,
                                fontFamily = vazirFontFamily,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(4.dp))

                    FilterDropdown(
                        stringResource(R.string.check_filter_counterparty),
                        selectedCounterparty ?: stringResource(R.string.check_filter_all),
                        listOf(null to stringResource(R.string.check_filter_all)) +
                                checks.map { it.counterparty }.distinct().map { it to it },
                        onCounterpartyChanged
                    )

                    OutlinedTextField(
                        value = checkNumberQuery,
                        onValueChange = onCheckNumberChanged,
                        label = { Text(stringResource(R.string.check_number)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.confirm)) } },
            dismissButton = { TextButton(onClick = onClear) { Text(stringResource(R.string.check_clear_filters)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String?, String>>,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = { onSelected(key); expanded = false })
            }
        }
    }
}

@Composable
private fun checkTypeLabel(type: String): String = stringResource(
    if (type == CheckEntity.TYPE_RECEIVED) R.string.check_received else R.string.check_payable
)

@Composable
private fun checkStateLabel(state: String): String = stringResource(
    when (state) {
        CheckEntity.STATE_IN_BANK -> R.string.check_state_in_bank
        CheckEntity.STATE_COLLECTED -> R.string.check_state_collected
        CheckEntity.STATE_RETURNED -> R.string.check_state_returned
        CheckEntity.STATE_BOUNCED -> R.string.check_state_bounced
        else -> R.string.check_state_in_progress
    }
)

private fun checkStateColor(state: String): Color = when (state) {
    CheckEntity.STATE_IN_PROGRESS -> Color(0xFFFFB74D)
    CheckEntity.STATE_IN_BANK -> Color(0xFF4FC3F7)
    CheckEntity.STATE_COLLECTED -> Color(0xFF43A047)
    CheckEntity.STATE_RETURNED -> Color(0xFF795548)
    else -> Color(0xFFD32F2F)
}

@Composable
private fun ChangeStateDialog(
    currentState: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.check_state)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                checkStates().forEach { (state, label) ->
                    val key = state ?: return@forEach
                    val color = checkStateColor(key)
                    val selected = key == currentState
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) color.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { onSelect(key) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = label,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = scaledSp(14f),
                            fontFamily = vazirFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.confirm)) } }
    )
}

@Composable
private fun checkStates(): List<Pair<String?, String>> = listOf(
    CheckEntity.STATE_IN_PROGRESS to stringResource(R.string.check_state_in_progress),
    CheckEntity.STATE_IN_BANK to stringResource(R.string.check_state_in_bank),
    CheckEntity.STATE_COLLECTED to stringResource(R.string.check_state_collected),
    CheckEntity.STATE_RETURNED to stringResource(R.string.check_state_returned),
    CheckEntity.STATE_BOUNCED to stringResource(R.string.check_state_bounced)
)
