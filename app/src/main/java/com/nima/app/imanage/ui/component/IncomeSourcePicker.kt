package com.nima.app.imanage.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import com.nima.app.imanage.ui.theme.NoteBoxPalettes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeSourcePicker(
    sources: List<IncomeSourceEntity>,
    selectedSourceId: Int?,
    onSourceSelected: (Int?) -> Unit,
    onAddSource: (title: String, colorIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.income_source_label)
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingTitle by remember { mutableStateOf<String?>(null) }

    val selectedSource = sources.firstOrNull { it.id == selectedSourceId }
    val displayText = selectedSource?.title ?: stringResource(R.string.no_source)

    LaunchedEffect(sources, pendingTitle) {
        val title = pendingTitle ?: return@LaunchedEffect
        val found = sources.firstOrNull { it.title.equals(title, ignoreCase = true) }
        if (found != null) {
            onSourceSelected(found.id)
            pendingTitle = null
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                val palette = selectedSource?.let {
                    NoteBoxPalettes.getOrElse(it.colorIndex) { NoteBoxPalettes.first() }
                } ?: NoteBoxPalettes.first()
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(palette.primary)
                        .border(1.dp, palette.accent, CircleShape)
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(stringResource(R.string.no_source))
                    }
                },
                onClick = {
                    onSourceSelected(null)
                    expanded = false
                }
            )
            sources.forEach { source ->
                val palette = NoteBoxPalettes.getOrElse(source.colorIndex) { NoteBoxPalettes.first() }
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                                    .border(1.dp, palette.accent, CircleShape)
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(source.title)
                        }
                    },
                    onClick = {
                        onSourceSelected(source.id)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = stringResource(R.string.add_new_source),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                onClick = {
                    expanded = false
                    showAddDialog = true
                }
            )
        }
    }

    if (showAddDialog) {
        AddSourceDialog(
            sources = sources,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, colorIndex ->
                onAddSource(title, colorIndex)
                pendingTitle = title
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddSourceDialog(
    sources: List<IncomeSourceEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, colorIndex: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var duplicateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_source)) },
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
                    val exists = sources.any { it.title.equals(trimmedTitle, ignoreCase = true) }
                    if (exists) {
                        duplicateError = true
                        return@TextButton
                    }
                    onConfirm(trimmedTitle, colorIndex)
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
