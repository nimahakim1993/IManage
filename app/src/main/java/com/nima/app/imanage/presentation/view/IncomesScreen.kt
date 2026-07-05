package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.IncomeEntity
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.IncomeViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
import com.nima.app.imanage.ui.theme.NoteBoxPalettes
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: IncomeViewModel = koinViewModel()
) {
    val incomes by viewModel.incomes.collectAsState()
    val sources by viewModel.sources.collectAsState()

    val incomesTitle = stringResource(R.string.incomes_title)
    val addDesc = stringResource(R.string.add)
    val filterDesc = stringResource(R.string.filter)
    val manageDesc = stringResource(R.string.manage_sources)
    val editDesc = stringResource(R.string.edit)

    var toggleEditMode by rememberSaveable { mutableStateOf(false) }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    var editingIncome by remember { mutableStateOf<IncomeEntity?>(null) }
    var removingIncome by remember { mutableStateOf<IncomeEntity?>(null) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSourceFilters by remember {
        mutableStateOf<Set<Int>>(emptySet())
    }
    var showNoSource by rememberSaveable { mutableStateOf(true) }
    var selectedMonthYear by rememberSaveable { mutableStateOf<Pair<Int, Int>?>(null) }
    var showMonthYearPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(incomes.isEmpty()) {
        if (incomes.isEmpty()) toggleEditMode = false
    }

    LaunchedEffect(toggleEditMode, incomes.isEmpty()) {
        val actions = mutableListOf(
            ToolbarAction(
                icon = Icons.Default.Add,
                contentDescription = addDesc,
                onClick = {
                    editingIncome = null
                    showCreateSheet = true
                }
            ),
            ToolbarAction(
                icon = Icons.Default.FilterAlt,
                contentDescription = filterDesc,
                onClick = { showFilterDialog = true }
            ),
            ToolbarAction(
                icon = Icons.Default.Tune,
                contentDescription = manageDesc,
                onClick = { navController.navigate(Screen.IncomeSources.route) }
            )
        )
        if (incomes.isNotEmpty()) {
            actions.add(
                ToolbarAction(
                    icon = if (toggleEditMode) Icons.Default.EditOff else Icons.Default.Edit,
                    contentDescription = editDesc,
                    onClick = { toggleEditMode = !toggleEditMode }
                )
            )
        }
        setToolbar(
            ToolbarConfig(
                title = incomesTitle,
                showBack = true,
                actions = actions
            )
        )
    }

    val filteredIncomes = remember(
        incomes,
        sources,
        searchQuery,
        selectedSourceFilters,
        showNoSource,
        selectedMonthYear
    ) {
        val activeFilter = selectedSourceFilters.isNotEmpty() || !showNoSource
        incomes.filter { income ->
            val sourceMatch = if (!activeFilter) {
                true
            } else {
                when {
                    income.sourceId == null -> showNoSource
                    else -> income.sourceId in selectedSourceFilters
                }
            }
            val queryMatch = if (searchQuery.isBlank()) {
                true
            } else {
                income.title.contains(searchQuery, ignoreCase = true) ||
                        income.description.contains(searchQuery, ignoreCase = true) ||
                        (sources.firstOrNull { it.id == income.sourceId }?.title
                            ?.contains(searchQuery, ignoreCase = true) ?: false)
            }
            val dateMatch = selectedMonthYear?.let { (month, year) ->
                val (jy, jm, _) = ShamsiDate.fromMillis(income.incomeDate)
                jy == year && jm == month
            } ?: true
            sourceMatch && queryMatch && dateMatch
        }
    }

    val totalAmount = remember(filteredIncomes) {
        filteredIncomes.sumOf { it.amount }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TotalsCard(
            modifier = Modifier.padding(16.dp),
            total = totalAmount,
            count = filteredIncomes.size,
            selectedMonthYear = selectedMonthYear,
            onDateFilterClick = { showMonthYearPicker = true },
            onClearDateFilter = { selectedMonthYear = null }
        )

        if (incomes.isEmpty()) {
            EmptyState(
                icon = Icons.Default.TrendingUp,
                title = stringResource(R.string.empty_incomes),
                hint = stringResource(R.string.empty_incomes_hint),
                actionLabel = stringResource(R.string.add),
                onAction = {
                    editingIncome = null
                    showCreateSheet = true
                }
            )
        } else if (filteredIncomes.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FilterAlt,
                title = stringResource(R.string.empty_incomes_filtered),
                hint = stringResource(R.string.empty_incomes_filtered_hint)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredIncomes, key = { it.id }) { income ->
                    val source = sources.firstOrNull { it.id == income.sourceId }
                    IncomeItem(
                        income = income,
                        source = source,
                        editMode = toggleEditMode,
                        onClick = {
                            editingIncome = income
                            showCreateSheet = true
                        },
                        onEdit = {
                            editingIncome = income
                            showCreateSheet = true
                        },
                        onDelete = { removingIncome = income }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateIncomeSheet(
            sources = sources,
            editing = editingIncome,
            onDismiss = {
                showCreateSheet = false
                editingIncome = null
            },
            onSave = { income ->
                viewModel.saveIncome(income)
                showCreateSheet = false
                editingIncome = null
            },
            onAddSource = { title, colorIndex ->
                viewModel.addSource(title, colorIndex)
            }
        )
    }

    removingIncome?.let { income ->
        ActionDialog(
            onDismiss = { removingIncome = null },
            onPositiveClicked = {
                viewModel.removeIncome(income)
                removingIncome = null
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            sources = sources,
            selectedSourceFilters = selectedSourceFilters,
            onSourceToggle = { id ->
                selectedSourceFilters =
                    if (id in selectedSourceFilters)
                        selectedSourceFilters - id
                    else
                        selectedSourceFilters + id
            },
            showNoSource = showNoSource,
            onShowNoSourceChange = { showNoSource = it },
            onDismiss = { showFilterDialog = false },
            onClear = {
                searchQuery = ""
                selectedSourceFilters = emptySet()
                showNoSource = true
            }
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
private fun TotalsCard(
    modifier: Modifier = Modifier,
    total: Long,
    count: Int,
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
                            text = stringResource(R.string.total_incomes),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = vazirFontFamily
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = NumberFormatUtils.format(total),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            fontFamily = vazirFontFamily
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.items_count),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = vazirFontFamily
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = NumberFormatUtils.format(count.toLong()),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
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
                        fontSize = 13.sp,
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
fun IncomeItem(
    income: IncomeEntity,
    source: IncomeSourceEntity?,
    editMode: Boolean = false,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val palette = if (source != null) {
        NoteBoxPalettes.getOrElse(source.colorIndex) { NoteBoxPalettes.first() }
    } else {
        NoteBoxPalettes.last()
    }
    val accentColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f)

    val gradient = Brush.linearGradient(
        colors = listOf(palette.primary, palette.secondary.copy(alpha = 0.78f)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
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
                        Text(
                            text = income.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            fontFamily = vazirFontFamily,
                            maxLines = 1
                        )
                        if (source != null) {
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
                                    text = source.title,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontFamily = vazirFontFamily
                                )
                            }
                        }
                    }
                    Text(
                        text = NumberFormatUtils.format(income.amount),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        fontFamily = vazirFontFamily
                    )
                }

                if (income.description.isNotBlank()) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = income.description,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                            fontFamily = vazirFontFamily,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = ShamsiDate.format(income.incomeDate),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontFamily = vazirFontFamily
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
private fun FilterDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sources: List<IncomeSourceEntity>,
    selectedSourceFilters: Set<Int>,
    onSourceToggle: (Int) -> Unit,
    showNoSource: Boolean,
    onShowNoSourceChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
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
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(R.string.filter_by_source),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showNoSource,
                            onCheckedChange = onShowNoSourceChange
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = stringResource(R.string.no_source),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    sources.forEach { source ->
                        val palette = NoteBoxPalettes.getOrElse(source.colorIndex) { NoteBoxPalettes.first() }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = source.id in selectedSourceFilters,
                                onCheckedChange = { onSourceToggle(source.id) }
                            )
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = source.title,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
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
