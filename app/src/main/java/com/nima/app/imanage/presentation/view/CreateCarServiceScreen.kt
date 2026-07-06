package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.CarServiceViewModel
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.TextInputDropDown
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateCarServiceScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    serviceId: Int = -1,
    viewModel: CarServiceViewModel = koinViewModel()
) {

    val createTitle = stringResource(R.string.create_car_service_title)
    val editTitle = stringResource(R.string.edit_car_service_title)

    val serviceTypes = listOf(
        stringResource(R.string.car_type_oil_change),
        stringResource(R.string.car_type_tire_change),
        stringResource(R.string.car_type_brake_pad),
        stringResource(R.string.car_type_filter),
        stringResource(R.string.car_type_belt),
        stringResource(R.string.car_type_lamp),
        stringResource(R.string.car_type_battery),
        stringResource(R.string.car_type_engine),
        stringResource(R.string.car_type_general),
        stringResource(R.string.car_type_insurance),
        stringResource(R.string.car_type_other)
    )

    LaunchedEffect(serviceId) {
        if (serviceId != -1) {
            viewModel.loadService(serviceId)
        }
    }

    LaunchedEffect(serviceId) {
        setToolbar(
            ToolbarConfig(
                title = if (serviceId == -1) createTitle else editTitle,
                showBack = true
            )
        )
    }

    var serviceTypeText by remember { mutableStateOf(serviceTypes[0]) }
    var serviceTypeKey by remember { mutableIntStateOf(0) }
    var serviceDate by remember { mutableStateOf(ShamsiDate.todayMillis()) }
    var serviceKilometer by remember { mutableStateOf(TextFieldValue("")) }
    var nextServiceDate by remember { mutableStateOf(ShamsiDate.todayMillis()) }
    var nextServiceKilometer by remember { mutableStateOf(TextFieldValue("")) }
    var amountPaid by remember { mutableStateOf(TextFieldValue("")) }
    var productBrand by remember { mutableStateOf("") }
    var partName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showServiceDatePicker by remember { mutableStateOf(false) }
    var showNextServiceDatePicker by remember { mutableStateOf(false) }

    val selectedService by viewModel.selectedService.collectAsState()
    LaunchedEffect(selectedService) {
        selectedService?.let { service ->
            serviceTypeKey = service.serviceType
            serviceTypeText = serviceTypes.getOrElse(service.serviceType) { serviceTypes[0] }
            serviceDate = service.serviceDate
            serviceKilometer =
                TextFieldValue(NumberFormatUtils.format(service.serviceKilometer.toLong()))
            nextServiceDate = service.nextServiceDate
            nextServiceKilometer =
                TextFieldValue(NumberFormatUtils.format(service.nextServiceKilometer.toLong()))
            amountPaid = TextFieldValue(NumberFormatUtils.format(service.amountPaid))
            productBrand = service.productBrand
            partName = service.partName
            description = service.description
        }
    }

    if (showServiceDatePicker) {
        ShamsiDatePicker(
            initialDate = serviceDate,
            title = stringResource(R.string.car_select_service_date),
            onConfirm = { newDate ->
                serviceDate = newDate
                showServiceDatePicker = false
            },
            onDismiss = { showServiceDatePicker = false }
        )
    }

    if (showNextServiceDatePicker) {
        ShamsiDatePicker(
            initialDate = nextServiceDate,
            title = stringResource(R.string.car_select_next_service_date),
            onConfirm = { newDate ->
                nextServiceDate = newDate
                showNextServiceDatePicker = false
            },
            onDismiss = { showNextServiceDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextInputDropDown(
            label = stringResource(R.string.car_service_type_label),
            items = serviceTypes,
            selectedItem = serviceTypeText,
            onItemSelected = { key, name ->
                serviceTypeText = name
                serviceTypeKey = key
            }
        )

        Spacer(modifier = Modifier.size(10.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { showServiceDatePicker = true }) {
            OutlinedTextField(
                value = ShamsiDate.format(serviceDate),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.car_service_date_label)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = serviceKilometer,
            onValueChange = { serviceKilometer = NumberFormatUtils.formatWithCursor(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.car_service_km_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { showNextServiceDatePicker = true }) {
            OutlinedTextField(
                value = ShamsiDate.format(nextServiceDate),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.car_next_service_date_label)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = nextServiceKilometer,
            onValueChange = { nextServiceKilometer = NumberFormatUtils.formatWithCursor(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.car_next_service_km_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = amountPaid,
            onValueChange = { amountPaid = NumberFormatUtils.formatWithCursor(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.car_amount_paid_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = productBrand,
            onValueChange = { productBrand = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.car_product_brand_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = partName,
            onValueChange = { partName = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.car_part_name_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.car_description_label)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            onClick = {
                val service = CarServiceEntity(
                    id = if (serviceId != -1) serviceId else 0,
                    serviceType = serviceTypeKey,
                    serviceDate = serviceDate,
                    serviceKilometer = NumberFormatUtils.parseToLong(serviceKilometer.text).toInt(),
                    nextServiceDate = nextServiceDate,
                    nextServiceKilometer = NumberFormatUtils.parseToLong(nextServiceKilometer.text)
                        .toInt(),
                    amountPaid = NumberFormatUtils.parseToLong(amountPaid.text),
                    productBrand = productBrand,
                    partName = partName,
                    description = description
                )
                viewModel.saveService(service)
                navController.popBackStack()
            }
        ) {
            Text(stringResource(R.string.confirm), fontWeight = FontWeight.Bold)
        }
    }
}
