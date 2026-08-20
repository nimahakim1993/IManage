package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.ColorUtils
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
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedState by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCounterparty by rememberSaveable { mutableStateOf<String?>(null) }

    val title = stringResource(R.string.checks)
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
        (selectedType == null || check.type == selectedType) &&
                (selectedState == null || check.state == selectedState) &&
                (selectedCounterparty == null || check.counterparty == selectedCounterparty)
    }

    Column(Modifier.fillMaxSize()) {
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { check ->
                    CheckItem(
                        check = check,
                        editMode = editMode,
                        onEdit = { editing = check; showSheet = true },
                        onDelete = { deleting = check },
                        onToggleSettled = { viewModel.toggleSettled(check) }
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
    if (showFilter) {
        CheckFilterDialog(
            checks = checks,
            selectedType = selectedType,
            selectedState = selectedState,
            selectedCounterparty = selectedCounterparty,
            onTypeChanged = { selectedType = it },
            onStateChanged = { selectedState = it },
            onCounterpartyChanged = { selectedCounterparty = it },
            onClear = {
                selectedType = null
                selectedState = null
                selectedCounterparty = null
            },
            onDismiss = { showFilter = false }
        )
    }
}

@Composable
private fun CheckItem(
    check: CheckEntity,
    editMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleSettled: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val settledColor = if (isDark) Color(0xFF1565C0) else Color(0xFF1976D2)
    val palette = if (check.type == CheckEntity.TYPE_RECEIVED) {
        ColorUtils.palettes[9]
    } else {
        ColorUtils.palettes[7]
    }
    val accentColor =
        if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f)
    val stateColor = checkStateColor(check.state)
    val baseColor by animateColorAsState(
        targetValue = if (check.settled) settledColor else palette.primary,
        animationSpec = tween(700),
        label = "checkBaseColor"
    )
    val gradient = Brush.linearGradient(
        colors = listOf(
            baseColor,
            if (check.settled) settledColor.copy(alpha = 0.78f) else palette.secondary.copy(alpha = 0.78f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = editMode, onClick = onEdit),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = check.counterparty,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = scaledSp(16f),
                                fontFamily = vazirFontFamily,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (check.settled) {
                                Spacer(modifier = Modifier.size(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.settled),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f))
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = checkTypeLabel(check.type),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = scaledSp(11f),
                                fontFamily = vazirFontFamily
                            )
                        }
                    }
                    Text(
                        text = NumberFormatUtils.format(check.amount),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = scaledSp(22f),
                        fontFamily = vazirFontFamily
                    )
                }

                if (check.description.isNotBlank()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = check.description,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = scaledSp(13f),
                            fontFamily = vazirFontFamily,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.check_number_value, check.checkNumber),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = scaledSp(11f),
                        fontFamily = vazirFontFamily
                    )
                    Box(
                        modifier = Modifier
                            .background(stateColor.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = checkStateLabel(check.state),
                            color = Color.White,
                            fontSize = scaledSp(11f),
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.size(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = ShamsiDate.format(check.dueDate),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = scaledSp(11f),
                        fontFamily = vazirFontFamily
                    )
                }

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
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(
                            if (check.settled) R.string.settled_on else R.string.mark_settled
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
                                stringResource(R.string.edit),
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.delete),
                                tint = Color.White
                            )
                        }
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
    selectedState: String?,
    selectedCounterparty: String?,
    onTypeChanged: (String?) -> Unit,
    onStateChanged: (String?) -> Unit,
    onCounterpartyChanged: (String?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.check_filter)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterDropdown(
                    stringResource(R.string.check_filter_type),
                    selectedType?.let { checkTypeLabel(it) }
                        ?: stringResource(R.string.check_filter_all),
                    listOf(
                        null to stringResource(R.string.check_filter_all),
                        CheckEntity.TYPE_RECEIVED to stringResource(R.string.check_received),
                        CheckEntity.TYPE_PAYABLE to stringResource(R.string.check_payable)
                    ),
                    onTypeChanged
                )
                FilterDropdown(
                    stringResource(R.string.check_state),
                    selectedState?.let { checkStateLabel(it) }
                        ?: stringResource(R.string.check_filter_all),
                    listOf(null to stringResource(R.string.check_filter_all)) + checkStates(),
                    onStateChanged
                )
                FilterDropdown(
                    stringResource(R.string.check_filter_counterparty),
                    selectedCounterparty ?: stringResource(R.string.check_filter_all),
                    listOf(null to stringResource(R.string.check_filter_all)) + checks.map { it.counterparty }
                        .distinct().map { it to it },
                    onCounterpartyChanged
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = onClear) { Text(stringResource(R.string.check_clear_filters)) } }
    )
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
    CheckEntity.STATE_IN_PROGRESS -> Color(0xFFFFA000)
    CheckEntity.STATE_IN_BANK -> Color(0xFF1976D2)
    CheckEntity.STATE_COLLECTED -> Color(0xFF388E3C)
    CheckEntity.STATE_RETURNED -> Color(0xFF7B1FA2)
    else -> Color(0xFFD32F2F)
}

@Composable
private fun checkStates(): List<Pair<String?, String>> = listOf(
    CheckEntity.STATE_IN_PROGRESS to stringResource(R.string.check_state_in_progress),
    CheckEntity.STATE_IN_BANK to stringResource(R.string.check_state_in_bank),
    CheckEntity.STATE_COLLECTED to stringResource(R.string.check_state_collected),
    CheckEntity.STATE_RETURNED to stringResource(R.string.check_state_returned),
    CheckEntity.STATE_BOUNCED to stringResource(R.string.check_state_bounced)
)
