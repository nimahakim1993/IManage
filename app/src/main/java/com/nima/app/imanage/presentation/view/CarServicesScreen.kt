package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import com.nima.app.imanage.data.db.entity.CarServiceIconType
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.CarServiceViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

private val serviceTypeColors = listOf(
    Color(0xFFF44336),
    Color(0xFF9C27B0),
    Color(0xFF3F51B5),
    Color(0xFF03A9F4),
    Color(0xFF009688),
    Color(0xFFFF9800),
    Color(0xFF795548),
    Color(0xFF607D8B),
    Color(0xFF4CAF50),
    Color(0xFFE91E63),
    Color(0xFF2196F3)
)

@Composable
fun CarServicesScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    viewModel: CarServiceViewModel = koinViewModel()
) {

    val services by viewModel.services.collectAsState()
    val carTitle = stringResource(R.string.car_services)
    val addDesc = stringResource(R.string.add)
    val filterDesc = stringResource(R.string.filter)
    val editDesc = stringResource(R.string.edit)

    var toggleEditMode by rememberSaveable { mutableStateOf(false) }
    var removingService by remember { mutableStateOf<CarServiceEntity?>(null) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var filterYear by rememberSaveable { mutableStateOf("") }
    var filterServiceTypes by rememberSaveable { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(services.isEmpty()) {
        if (services.isEmpty()) toggleEditMode = false
    }

    LaunchedEffect(toggleEditMode, services.isEmpty()) {
        val actions = mutableListOf(
            ToolbarAction(
                icon = Icons.Default.Add,
                contentDescription = addDesc,
                onClick = { navController.navigate(Screen.CreateCarService.createRoute()) }
            ),
            ToolbarAction(
                icon = Icons.Default.FilterAlt,
                contentDescription = filterDesc,
                onClick = { showFilterDialog = true }
            )
        )
        if (services.isNotEmpty()) {
            actions.add(
                ToolbarAction(
                    icon = if (toggleEditMode) Icons.Default.EditOff else Icons.Default.Edit,
                    contentDescription = editDesc,
                    onClick = { toggleEditMode = !toggleEditMode }
                )
            )
        }
        setToolbar(ToolbarConfig(title = carTitle, showBack = true, actions = actions))
    }

    val filteredServices = remember(services, filterYear, filterServiceTypes) {
        val yearInt = filterYear.toIntOrNull()
        services.filter { service ->
            val yearMatch =
                yearInt == null || ShamsiDate.fromMillis(service.serviceDate).first == yearInt
            val typeMatch =
                filterServiceTypes.isEmpty() || service.serviceType in filterServiceTypes
            yearMatch && typeMatch
        }
    }

    val totalAmount = remember(filteredServices) {
        filteredServices.sumOf { it.amountPaid }
    }

    val typeSlices = remember(filteredServices) {
        filteredServices.groupBy { it.serviceType }
            .map { (type, items) ->
                val sum = items.sumOf { it.amountPaid }
                Triple(type, sum, items.size)
            }
            .filter { it.second > 0 }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (services.isEmpty()) {
            EmptyState(
                icon = Icons.Default.DirectionsCar,
                title = stringResource(R.string.empty_car_services),
                hint = stringResource(R.string.empty_car_services_hint),
                actionLabel = stringResource(R.string.add),
                onAction = { navController.navigate(Screen.CreateCarService.createRoute()) }
            )
        } else if (filteredServices.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FilterAlt,
                title = stringResource(R.string.empty_car_services_filtered),
                hint = stringResource(R.string.empty_car_services_filtered_hint)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (typeSlices.isNotEmpty() && totalAmount > 0) {
                    item {
                        DonutCard(
                            typeSlices = typeSlices,
                            totalAmount = totalAmount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                items(filteredServices, key = { it.id }) { service ->
                    CarServiceItem(
                        service = service,
                        editMode = toggleEditMode,
                        onEdit = {
                            navController.navigate(Screen.CreateCarService.createRoute(service.id))
                        },
                        onDelete = {
                            removingService = service
                        }
                    )
                }
            }
        }
    }

    removingService?.let { service ->
        ActionDialog(
            onDismiss = { removingService = null },
            onPositiveClicked = {
                viewModel.removeService(service)
                removingService = null
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            filterYear = filterYear,
            onFilterYearChange = { filterYear = it },
            filterServiceTypes = filterServiceTypes,
            onFilterServiceTypeChange = { type, checked ->
                filterServiceTypes =
                    if (checked) filterServiceTypes + type else filterServiceTypes - type
            },
            onDismiss = { showFilterDialog = false },
            onClear = {
                filterYear = ""
                filterServiceTypes = emptySet()
            }
        )
    }
}

@Composable
private fun DonutCard(
    typeSlices: List<Triple<Int, Long, Int>>,
    totalAmount: Long,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.85f else 1f)
    val subTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    val slices = typeSlices.map { (type, amount, _) ->
        val angle =
            if (totalAmount > 0) (amount.toDouble() / totalAmount.toDouble()) * 360.0 else 0.0
        Triple(type, amount, angle)
    }

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
                    text = stringResource(R.string.car_total_expenses),
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
                        slices.forEach { slice ->
                            val color = serviceTypeColors[slice.first % serviceTypeColors.size]
                            val sweepAngle = slice.third.toFloat()
                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = color,
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
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = NumberFormatUtils.format(totalAmount),
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

                val legendItems = slices.map { slice ->
                    val iconType = CarServiceIconType.fromValue(slice.first)
                    val percent = if (totalAmount > 0)
                        ((slice.second.toDouble() / totalAmount.toDouble()) * 100).toInt()
                    else 0
                    val color = serviceTypeColors[slice.first % serviceTypeColors.size]
                    Triple(
                        stringResource(iconType.labelRes()),
                        percent,
                        color
                    )
                }.chunked(2)

                legendItems.forEach { row ->
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
fun CarServiceItem(
    service: CarServiceEntity,
    editMode: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val iconType = CarServiceIconType.fromValue(service.serviceType)
    val accentColor = serviceTypeColors[service.serviceType % serviceTypeColors.size]

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor =
        if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isDark) accentColor.copy(alpha = 0.25f)
                                else accentColor.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconType.icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(iconType.labelRes()),
                            color = onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            fontFamily = vazirFontFamily
                        )
                        if (service.productBrand.isNotBlank()) {
                            Text(
                                text = service.productBrand,
                                color = onSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = vazirFontFamily
                            )
                        }
                    }
                }
                if (service.amountPaid > 0) {
                    Text(
                        text = NumberFormatUtils.format(service.amountPaid),
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        fontFamily = vazirFontFamily
                    )
                }
            }

            if (service.partName.isNotBlank() || service.description.isNotBlank()) {
                Spacer(modifier = Modifier.size(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dividerColor)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        if (service.partName.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.car_part_label) + ": " + service.partName,
                                color = onSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = vazirFontFamily
                            )
                        }
                        if (service.description.isNotBlank()) {
                            Text(
                                text = service.description,
                                color = onSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = vazirFontFamily,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(
                            R.string.car_service_date,
                            ShamsiDate.format(service.serviceDate)
                        ),
                        color = onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = vazirFontFamily
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = stringResource(
                            R.string.car_service_km,
                            NumberFormatUtils.format(service.serviceKilometer.toLong())
                        ),
                        color = onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = vazirFontFamily
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(
                            R.string.car_next_service_date,
                            ShamsiDate.format(service.nextServiceDate)
                        ),
                        color = onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = vazirFontFamily
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = stringResource(
                            R.string.car_next_service_km,
                            NumberFormatUtils.format(service.nextServiceKilometer.toLong())
                        ),
                        color = onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = vazirFontFamily
                    )
                }
            }

            AnimatedVisibility(visible = editMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDialog(
    filterYear: String,
    onFilterYearChange: (String) -> Unit,
    filterServiceTypes: Set<Int>,
    onFilterServiceTypeChange: (Int, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.filter)) },
            text = {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = filterYear,
                        onValueChange = onFilterYearChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stringResource(R.string.car_filter_year)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.car_filter_type),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    CarServiceIconType.entries.forEach { iconType ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = iconType.value in filterServiceTypes,
                                onCheckedChange = {
                                    onFilterServiceTypeChange(iconType.value, it)
                                }
                            )
                            Text(
                                text = stringResource(iconType.labelRes()),
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

private fun CarServiceIconType.labelRes(): Int {
    return when (this) {
        CarServiceIconType.OIL_CHANGE -> R.string.car_type_oil_change
        CarServiceIconType.TIRE_CHANGE -> R.string.car_type_tire_change
        CarServiceIconType.BRAKE_PAD -> R.string.car_type_brake_pad
        CarServiceIconType.FILTER -> R.string.car_type_filter
        CarServiceIconType.BELT -> R.string.car_type_belt
        CarServiceIconType.LAMP -> R.string.car_type_lamp
        CarServiceIconType.BATTERY -> R.string.car_type_battery
        CarServiceIconType.ENGINE -> R.string.car_type_engine
        CarServiceIconType.GENERAL -> R.string.car_type_general
        CarServiceIconType.INSURANCE -> R.string.car_type_insurance
        CarServiceIconType.DEFAULT -> R.string.car_type_other
    }
}
