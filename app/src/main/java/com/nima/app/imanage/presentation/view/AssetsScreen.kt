package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.AssetEntity
import com.nima.app.imanage.data.db.entity.AssetIconType
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.AssetViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

private val assetChartColors = listOf(
    Color(0xFF4FC3F7),
    Color(0xFFFFB74D),
    Color(0xFFAED581),
    Color(0xFFE57373),
    Color(0xFFBA68C8),
    Color(0xFF4DD0E1),
    Color(0xFFFFF176),
    Color(0xFFA1887F),
    Color(0xFF90A4AE),
    Color(0xFF7986CB),
)

@Composable
fun AssetsScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: AssetViewModel = koinViewModel()
) {
    val assets by viewModel.assets.collectAsState()

    val assetsTitle = stringResource(R.string.assets_title)
    val addDesc = stringResource(R.string.add)
    val editDesc = stringResource(R.string.edit)

    var toggleEditMode by rememberSaveable { mutableStateOf(false) }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<AssetEntity?>(null) }
    var removingAsset by remember { mutableStateOf<AssetEntity?>(null) }

    LaunchedEffect(assets.isEmpty()) {
        if (assets.isEmpty()) toggleEditMode = false
    }

    LaunchedEffect(toggleEditMode, assets.isEmpty()) {
        val actions = mutableListOf(
            ToolbarAction(
                icon = Icons.Default.Add,
                contentDescription = addDesc,
                onClick = {
                    editingAsset = null
                    showCreateSheet = true
                }
            )
        )
        if (assets.isNotEmpty()) {
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
                title = assetsTitle,
                showBack = true,
                actions = actions
            )
        )
    }

    val totalValue = remember(assets) {
        assets.sumOf { (it.unitCount * it.pricePerUnit).toLong() }
    }

    if (assets.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Inventory2,
            title = stringResource(R.string.empty_assets),
            hint = stringResource(R.string.empty_assets_hint),
            actionLabel = stringResource(R.string.add),
            onAction = {
                editingAsset = null
                showCreateSheet = true
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    if (assets.size > 1) {
                        DonutChart(
                            assets = assets,
                            totalValue = totalValue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
                items(
                    count = assets.size,
                    key = { index -> assets[index].id }
                ) { index ->
                    val asset = assets[index]
                    val assetValue = (asset.unitCount * asset.pricePerUnit).toLong()
                    AssetRectangle(
                        asset = asset,
                        assetValue = assetValue,
                        accentColor = assetChartColors[index % assetChartColors.size],
                        editMode = toggleEditMode,
                        onClick = {
                            editingAsset = asset
                            showCreateSheet = true
                        },
                        onEdit = {
                            editingAsset = asset
                            showCreateSheet = true
                        },
                        onDelete = { removingAsset = asset }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateAssetSheet(
            editing = editingAsset,
            onDismiss = {
                showCreateSheet = false
                editingAsset = null
            },
            onSave = { asset ->
                viewModel.saveAsset(asset)
                showCreateSheet = false
                editingAsset = null
            }
        )
    }

    removingAsset?.let { asset ->
        ActionDialog(
            onDismiss = { removingAsset = null },
            onPositiveClicked = {
                viewModel.removeAsset(asset)
                removingAsset = null
            }
        )
    }
}

@Composable
private fun DonutChart(
    assets: List<AssetEntity>,
    totalValue: Long,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val slices = assets.map { asset ->
        val value = (asset.unitCount * asset.pricePerUnit).toLong()
        val angle = if (totalValue > 0) (value.toDouble() / totalValue.toDouble()) * 360.0 else 0.0
        Triple(asset.name, value, angle)
    }

    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.85f else 1f)
    val subTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.asset_distribution),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = vazirFontFamily
                )

                Spacer(modifier = Modifier.size(12.dp))

                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = size.minDimension * 0.22f
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        )

                        var startAngle = -90f
                        slices.forEachIndexed { index, slice ->
                            val sweepAngle = slice.third.toFloat()
                            drawArc(
                                color = assetChartColors[index % assetChartColors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = NumberFormatUtils.format(totalValue),
                            color = textColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            fontFamily = vazirFontFamily
                        )
                        Text(
                            text = stringResource(R.string.toman),
                            color = subTextColor,
                            fontSize = 10.sp,
                            fontFamily = vazirFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                val gridItems = slices.mapIndexed { index, slice ->
                    val percent = if (totalValue > 0)
                        ((slice.second.toDouble() / totalValue.toDouble()) * 100).toInt()
                    else 0
                    Triple(slice.first, percent, assetChartColors[index % assetChartColors.size])
                }.chunked(2)

                gridItems.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (name, percent, color) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "%$percent",
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    fontFamily = vazirFontFamily
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = name,
                                    color = subTextColor,
                                    fontSize = 11.sp,
                                    fontFamily = vazirFontFamily,
                                    maxLines = 1
                                )
                            }
                        }
                        repeat(2 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetRectangle(
    asset: AssetEntity,
    assetValue: Long,
    editMode: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val iconType = AssetIconType.fromValue(asset.iconType)

    val glassBorder = if (isDark)
        Color.White.copy(alpha = 0.18f)
    else
        Color.White.copy(alpha = 0.95f)

    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val textMuted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        if (editMode) 0.dp else 1.5.dp,
                        glassBorder,
                        RoundedCornerShape(
                            if (editMode) 20.dp else 20.dp
                        )
                    )
                    .then(
                        if (editMode)
                            Modifier.border(
                                1.5.dp,
                                glassBorder,
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 0.dp
                                )
                            )
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = if (isDark) 0.22f else 0.18f))
                            .border(
                                1.5.dp,
                                accentColor.copy(alpha = 0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconType.icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = asset.name,
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = vazirFontFamily,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        val unitDisplay = if (asset.unitCount == asset.unitCount.toLong().toDouble())
                            NumberFormatUtils.format(asset.unitCount.toLong())
                        else
                            String.format(Locale.ENGLISH, "%.3f", asset.unitCount)
                                .trimEnd('0').trimEnd('.')
                        Text(
                            text = unitDisplay + " " + stringResource(R.string.unit),
                            color = textSecondary,
                            fontSize = 12.sp,
                            fontFamily = vazirFontFamily
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Text(
                                    text = stringResource(R.string.price_per_unit),
                                    color = textMuted,
                                    fontSize = 11.sp,
                                    fontFamily = vazirFontFamily
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = NumberFormatUtils.format(asset.pricePerUnit),
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = vazirFontFamily
                                )
                            }
                        }
                    }

                    Text(
                        text = NumberFormatUtils.format(assetValue),
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        fontFamily = vazirFontFamily,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (editMode) {
                val editDesc = stringResource(R.string.edit)
                val deleteDesc = stringResource(R.string.delete)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            )
                        )
                        .background(
                            if (isDark)
                                Color.White.copy(alpha = 0.06f)
                            else
                                Color.Black.copy(alpha = 0.03f)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                            RoundedCornerShape(
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            )
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = editDesc,
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = editDesc,
                                color = textSecondary,
                                fontSize = 13.sp,
                                fontFamily = vazirFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = deleteDesc,
                                tint = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = deleteDesc,
                                color = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828),
                                fontSize = 13.sp,
                                fontFamily = vazirFontFamily
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
private fun CreateAssetSheet(
    editing: AssetEntity?,
    onDismiss: () -> Unit,
    onSave: (AssetEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = editing != null

    val sheetTitle = stringResource(if (isEdit) R.string.edit_asset_title else R.string.create_asset_title)

    var name by remember { mutableStateOf("") }
    var iconType by remember { mutableStateOf(AssetIconType.DEFAULT.value) }
    var unitCount by remember { mutableStateOf(TextFieldValue("")) }
    var pricePerUnit by remember { mutableStateOf(TextFieldValue("")) }

    val unitCountFocusRequester = remember { FocusRequester() }
    val pricePerUnitFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(editing?.id) {
        editing?.let { asset ->
            name = asset.name
            iconType = asset.iconType
            unitCount = TextFieldValue(formatUnitCountForInput(asset.unitCount))
            pricePerUnit = TextFieldValue(NumberFormatUtils.format(asset.pricePerUnit))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = sheetTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.asset_name_label)) },
                placeholder = { Text(stringResource(R.string.asset_name_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { unitCountFocusRequester.requestFocus() }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = stringResource(R.string.asset_icon_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                fontFamily = vazirFontFamily,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val iconTypes = AssetIconType.entries
            val rows = iconTypes.chunked(5)
            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { type ->
                        val isSelected = iconType == type.value
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { iconType = type.value }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    repeat(5 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.size(4.dp))
            }

            Spacer(modifier = Modifier.size(12.dp))

            OutlinedTextField(
                value = unitCount,
                onValueChange = { newValue ->
                    val filtered = newValue.text.filter { c ->
                        (c in '0'..'9') || c == '.'
                    }
                    if (filtered.count { it == '.' } <= 1) {
                        unitCount = TextFieldValue(
                            text = filtered,
                            selection = newValue.selection
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { pricePerUnitFocusRequester.requestFocus() }),
                label = { Text(stringResource(R.string.unit_count)) },
                placeholder = { Text(stringResource(R.string.unit_count_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(unitCountFocusRequester)
            )

            Spacer(modifier = Modifier.size(12.dp))

            OutlinedTextField(
                value = pricePerUnit,
                onValueChange = { pricePerUnit = NumberFormatUtils.formatWithCursor(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                label = { Text(stringResource(R.string.price_per_unit) + " (" + stringResource(R.string.toman) + ")") },
                placeholder = { Text(stringResource(R.string.price_per_unit_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(pricePerUnitFocusRequester)
            )

            Spacer(modifier = Modifier.size(20.dp))

            Button(
                onClick = {
                    val finalName = name.trim()
                    if (finalName.isBlank()) return@Button
                    val cleanUnitCount = unitCount.text.trimEnd('.')
                    val unitCountVal = cleanUnitCount.toDoubleOrNull() ?: 0.0
                    val priceVal = NumberFormatUtils.parseToLong(pricePerUnit.text)

                    val now = System.currentTimeMillis()
                    val asset = AssetEntity(
                        id = editing?.id ?: 0,
                        name = finalName,
                        iconType = iconType,
                        unitCount = unitCountVal,
                        pricePerUnit = priceVal,
                        createdAt = editing?.createdAt ?: now,
                        updatedAt = now
                    )
                    onSave(asset)
                },
                enabled = name.isNotBlank() && pricePerUnit.text.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.confirm),
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }

            Spacer(modifier = Modifier.size(12.dp))
        }
    }
}

private fun formatUnitCountForInput(value: Double): String {
    if (value == value.toLong().toDouble()) {
        return value.toLong().toString()
    }
    val str = String.format(Locale.ENGLISH, "%.4f", value)
    return str.trimEnd('0').trimEnd('.')
}
