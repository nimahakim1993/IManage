package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CheckCounterpartyEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.CheckViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import org.koin.androidx.compose.koinViewModel

@Composable
fun CheckCounterpartiesScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    viewModel: CheckViewModel = koinViewModel()
) {
    val counterparties by viewModel.counterparties.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CheckCounterpartyEntity?>(null) }
    var deleting by remember { mutableStateOf<CheckCounterpartyEntity?>(null) }
    val title = stringResource(R.string.check_manage_counterparties)
    val addDescription = stringResource(R.string.add)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = title,
                showBack = true,
                actions = listOf(
                    ToolbarAction(Icons.Default.Add, addDescription) {
                        showAdd = true
                    }
                )
            )
        )
    }

    if (counterparties.isEmpty()) {
        EmptyState(
            icon = Icons.Default.People,
            title = stringResource(R.string.empty_check_counterparties),
            hint = stringResource(R.string.empty_check_counterparties_hint),
            actionLabel = stringResource(R.string.add),
            onAction = { showAdd = true }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(counterparties, key = { it.id }) { counterparty ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            counterparty.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { editing = counterparty }) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit))
                        }
                        IconButton(onClick = { deleting = counterparty }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete))
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        CounterpartyDialog(
            existing = counterparties,
            initialTitle = "",
            onDismiss = { showAdd = false },
            onConfirm = {
                viewModel.addCounterparty(it)
                showAdd = false
            }
        )
    }
    editing?.let { item ->
        CounterpartyDialog(
            existing = counterparties,
            editingId = item.id,
            initialTitle = item.title,
            onDismiss = { editing = null },
            onConfirm = {
                viewModel.updateCounterparty(item.copy(title = it.trim()))
                editing = null
            }
        )
    }
    deleting?.let { item ->
        ActionDialog(
            onDismiss = { deleting = null },
            onPositiveClicked = {
                viewModel.deleteCounterparty(item)
                deleting = null
            }
        )
    }
}

@Composable
private fun CounterpartyDialog(
    existing: List<CheckCounterpartyEntity>,
    editingId: Int? = null,
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var duplicate by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.check_counterparty)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; duplicate = false },
                    label = { Text(stringResource(R.string.check_counterparty)) },
                    isError = duplicate,
                    supportingText = if (duplicate) {
                        { Text(stringResource(R.string.duplicate_check_counterparty)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = title.trim()
                if (value.isBlank()) return@TextButton
                if (existing.any { it.id != editingId && it.title.equals(value, true) }) {
                    duplicate = true
                } else {
                    onConfirm(value)
                }
            }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
