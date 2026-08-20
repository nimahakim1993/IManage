package com.nima.app.imanage.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CheckCounterpartyEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckCounterpartyPicker(
    counterparties: List<CheckCounterpartyEntity>,
    selectedTitle: String,
    onSelected: (String) -> Unit,
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(counterparties, pendingTitle) {
        val title = pendingTitle ?: return@LaunchedEffect
        if (counterparties.any { it.title.equals(title, ignoreCase = true) }) {
            onSelected(title)
            pendingTitle = null
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTitle,
            onValueChange = {},
            readOnly = true,
            isError = isError,
            label = { Text(stringResource(R.string.check_counterparty)) },
            placeholder = { Text(stringResource(R.string.check_counterparty_hint)) },
            leadingIcon = {
                Icon(
                    Icons.Default.People,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            counterparties.forEach { counterparty ->
                DropdownMenuItem(
                    text = { Text(counterparty.title) },
                    onClick = { onSelected(counterparty.title); expanded = false }
                )
            }
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.add_new_check_counterparty),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                leadingIcon = { Icon(Icons.Default.Add, null) },
                onClick = { expanded = false; showAddDialog = true }
            )
        }
    }

    if (showAddDialog) {
        AddCheckCounterpartyDialog(
            counterparties = counterparties,
            onDismiss = { showAddDialog = false },
            onConfirm = { title -> pendingTitle = title; onAdd(title); showAddDialog = false }
        )
    }
}

@Composable
private fun AddCheckCounterpartyDialog(
    counterparties: List<CheckCounterpartyEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var duplicate by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_check_counterparty)) },
        text = {
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
        },
        confirmButton = {
            TextButton(onClick = {
                val value = title.trim()
                if (value.isBlank()) return@TextButton
                if (counterparties.any { it.title.equals(value, true) }) duplicate =
                    true else onConfirm(value)
            }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
