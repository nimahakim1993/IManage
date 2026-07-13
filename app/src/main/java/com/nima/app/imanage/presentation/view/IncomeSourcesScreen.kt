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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.IncomeViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.NoteBoxPalettes
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncomeSourcesScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: IncomeViewModel = koinViewModel()
) {
    val sources by viewModel.sources.collectAsState()
    val title = stringResource(R.string.income_sources_title)
    val addDesc = stringResource(R.string.add)

    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<IncomeSourceEntity?>(null) }
    var removing by remember { mutableStateOf<IncomeSourceEntity?>(null) }

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = title,
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.Add,
                        contentDescription = addDesc,
                        onClick = { showAddDialog = true }
                    )
                )
            )
        )
    }

    if (sources.isEmpty()) {
        EmptyState(
            icon = Icons.Default.TrendingUp,
            title = stringResource(R.string.empty_sources),
            hint = stringResource(R.string.empty_sources_hint),
            actionLabel = stringResource(R.string.add),
            onAction = { showAddDialog = true }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sources, key = { it.id }) { source ->
                SourceItem(
                    source = source,
                    onEdit = { editing = source },
                    onDelete = { removing = source }
                )
            }
        }
    }

    if (showAddDialog) {
        SourceEditDialog(
            sources = sources,
            editingSourceId = null,
            initialTitle = "",
            initialColorIndex = 0,
            confirmLabel = stringResource(R.string.add),
            onDismiss = { showAddDialog = false },
            onConfirm = { newTitle, colorIndex ->
                viewModel.addSource(newTitle, colorIndex)
                showAddDialog = false
            }
        )
    }

    editing?.let { source ->
        SourceEditDialog(
            sources = sources,
            editingSourceId = source.id,
            initialTitle = source.title,
            initialColorIndex = source.colorIndex,
            confirmLabel = stringResource(R.string.confirm),
            onDismiss = { editing = null },
            onConfirm = { newTitle, colorIndex ->
                viewModel.updateSource(
                    source.copy(title = newTitle.trim(), colorIndex = colorIndex)
                )
                editing = null
            }
        )
    }

    removing?.let { source ->
        ActionDialog(
            onDismiss = { removing = null },
            onPositiveClicked = {
                viewModel.removeSourceAndClearFromIncomes(source)
                removing = null
            }
        )
    }
}

@Composable
private fun SourceItem(
    source: IncomeSourceEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = NoteBoxPalettes.getOrElse(source.colorIndex) { NoteBoxPalettes.first() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.primary)
                    .border(2.dp, palette.accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = source.title,
                fontFamily = vazirFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = scaledSp(16f),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun SourceEditDialog(
    sources: List<IncomeSourceEntity>,
    editingSourceId: Int?,
    initialTitle: String,
    initialColorIndex: Int,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, colorIndex: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var colorIndex by remember { mutableIntStateOf(initialColorIndex) }
    var duplicateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.income_source_label)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        duplicateError = false
                    },
                    label = { Text(stringResource(R.string.source_title)) },
                    singleLine = true,
                    isError = duplicateError,
                    supportingText = if (duplicateError) {
                        {
                            Text(
                                stringResource(R.string.duplicate_source_title),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.theme),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NoteBoxPalettes.forEachIndexed { index, palette ->
                        val isSelected = index == colorIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 40.dp else 32.dp)
                                .clip(CircleShape)
                                .background(palette.primary)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground
                                    else palette.accent,
                                    shape = CircleShape
                                )
                                .clickable { colorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isBlank()) return@TextButton
                    val exists = sources.any {
                        it.id != editingSourceId && it.title.equals(trimmedTitle, ignoreCase = true)
                    }
                    if (exists) {
                        duplicateError = true
                        return@TextButton
                    }
                    onConfirm(trimmedTitle, colorIndex)
                }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
