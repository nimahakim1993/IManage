package com.nima.app.imanage.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.nima.app.imanage.data.db.entity.CarServiceIcons
import com.nima.app.imanage.data.db.entity.CarServiceTypeEntity
import com.nima.app.imanage.util.ColorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTypePicker(
    serviceTypes: List<CarServiceTypeEntity>,
    selectedTypeId: Int?,
    onTypeSelected: (Int?) -> Unit,
    onAddType: (title: String, colorIndex: Int, iconIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.car_service_type_label)
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingTitle by remember { mutableStateOf<String?>(null) }

    val selectedType = serviceTypes.firstOrNull { it.id == selectedTypeId }
    val displayText = selectedType?.title ?: ""

    LaunchedEffect(serviceTypes, pendingTitle) {
        val title = pendingTitle ?: return@LaunchedEffect
        val found = serviceTypes.firstOrNull { it.title.equals(title, ignoreCase = true) }
        if (found != null) {
            onTypeSelected(found.id)
            pendingTitle = null
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            shape = RoundedCornerShape(12.dp),
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                val palette = selectedType?.let {
                    ColorUtils.palettes.getOrElse(it.colorIndex) { ColorUtils.palettes.first() }
                } ?: ColorUtils.palettes.first()
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
            serviceTypes.forEach { type ->
                val palette =
                    ColorUtils.palettes.getOrElse(type.colorIndex) { ColorUtils.palettes.first() }
                val icon = CarServiceIcons.fromIndex(type.iconIndex)
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
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(type.title)
                        }
                    },
                    onClick = {
                        onTypeSelected(type.id)
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
                            text = stringResource(R.string.add_new_service_type),
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
        AddServiceTypeDialog(
            serviceTypes = serviceTypes,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, colorIndex, iconIndex ->
                onAddType(title, colorIndex, iconIndex)
                pendingTitle = title
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddServiceTypeDialog(
    serviceTypes: List<CarServiceTypeEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, colorIndex: Int, iconIndex: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var iconIndex by remember { mutableIntStateOf(0) }
    var duplicateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_service_type)) },
        text = {
            Column {
                OutlinedTextField(
                    shape = RoundedCornerShape(12.dp),
                    value = title,
                    onValueChange = {
                        title = it
                        duplicateError = false
                    },
                    label = { Text(stringResource(R.string.car_service_type_name_label)) },
                    singleLine = true,
                    isError = duplicateError,
                    supportingText = if (duplicateError) {
                        {
                            Text(
                                stringResource(R.string.duplicate_car_service_type),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.car_service_type_icon_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(8.dp))
                IconPickerRow(
                    selectedIndex = iconIndex,
                    onSelect = { iconIndex = it }
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.theme),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(8.dp))
                ColorPaletteGrid(
                    selectedIndex = colorIndex,
                    onSelect = { colorIndex = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isBlank()) return@TextButton
                    val exists =
                        serviceTypes.any { it.title.equals(trimmedTitle, ignoreCase = true) }
                    if (exists) {
                        duplicateError = true
                        return@TextButton
                    }
                    onConfirm(trimmedTitle, colorIndex, iconIndex)
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

@Composable
private fun IconPickerRow(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CarServiceIcons.icons.forEachIndexed { index, icon ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (isSelected) 44.dp else 36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
