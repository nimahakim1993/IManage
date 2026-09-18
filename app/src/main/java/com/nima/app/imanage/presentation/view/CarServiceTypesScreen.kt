package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.nima.app.imanage.data.db.entity.CarServiceIcons
import com.nima.app.imanage.data.db.entity.CarServiceTypeEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.CarServiceTypeViewModel
import com.nima.app.imanage.presentation.viewmodel.CarServiceViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.ColorPaletteGrid
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.ColorUtils
import org.koin.androidx.compose.koinViewModel

@Composable
fun CarServiceTypesScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: CarServiceTypeViewModel = koinViewModel(),
    carServiceViewModel: CarServiceViewModel = koinViewModel()
) {
    val types by viewModel.types.collectAsState()
    val title = stringResource(R.string.car_service_types_title)
    val addDesc = stringResource(R.string.add)

    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CarServiceTypeEntity?>(null) }
    var removing by remember { mutableStateOf<CarServiceTypeEntity?>(null) }

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

    if (types.isEmpty()) {
        EmptyState(
            icon = Icons.Default.CarRepair,
            title = stringResource(R.string.empty_car_service_types),
            hint = stringResource(R.string.empty_car_service_types_hint),
            actionLabel = stringResource(R.string.add),
            onAction = { showAddDialog = true }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(types, key = { it.id }) { type ->
                ServiceTypeItem(
                    type = type,
                    onEdit = { editing = type },
                    onDelete = { removing = type }
                )
            }
        }
    }

    if (showAddDialog) {
        ServiceTypeEditDialog(
            types = types,
            editingTypeId = null,
            initialTitle = "",
            initialColorIndex = 0,
            initialIconIndex = 0,
            confirmLabel = stringResource(R.string.add),
            onDismiss = { showAddDialog = false },
            onConfirm = { newTitle, colorIndex, iconIndex ->
                viewModel.addType(newTitle, colorIndex, iconIndex)
                showAddDialog = false
            }
        )
    }

    editing?.let { type ->
        ServiceTypeEditDialog(
            types = types,
            editingTypeId = type.id,
            initialTitle = type.title,
            initialColorIndex = type.colorIndex,
            initialIconIndex = type.iconIndex,
            confirmLabel = stringResource(R.string.confirm),
            onDismiss = { editing = null },
            onConfirm = { newTitle, colorIndex, iconIndex ->
                viewModel.updateType(
                    type.copy(
                        title = newTitle.trim(),
                        colorIndex = colorIndex,
                        iconIndex = iconIndex
                    )
                )
                editing = null
            }
        )
    }

    removing?.let { type ->
        ActionDialog(
            onDismiss = { removing = null },
            onPositiveClicked = {
                viewModel.removeType(type)
                removing = null
            }
        )
    }
}

@Composable
private fun ServiceTypeItem(
    type: CarServiceTypeEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = ColorUtils.palettes.getOrElse(type.colorIndex) { ColorUtils.palettes.first() }

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
                    imageVector = CarServiceIcons.fromIndex(type.iconIndex),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = type.title,
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
private fun ServiceTypeEditDialog(
    types: List<CarServiceTypeEntity>,
    editingTypeId: Int?,
    initialTitle: String,
    initialColorIndex: Int,
    initialIconIndex: Int,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, colorIndex: Int, iconIndex: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var colorIndex by remember { mutableIntStateOf(initialColorIndex) }
    var iconIndex by remember { mutableIntStateOf(initialIconIndex) }
    var duplicateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.car_service_type_label)) },
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
                IconPickerGrid(
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
                    val exists = types.any {
                        it.id != editingTypeId && it.title.equals(
                            trimmedTitle,
                            ignoreCase = true
                        )
                    }
                    if (exists) {
                        duplicateError = true
                        return@TextButton
                    }
                    onConfirm(trimmedTitle, colorIndex, iconIndex)
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

@Composable
private fun IconPickerGrid(
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
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground
                        else Color.Transparent,
                        shape = CircleShape
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
