package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.CarServiceTypeViewModel
import com.nima.app.imanage.presentation.viewmodel.CarServiceViewModel
import com.nima.app.imanage.ui.component.RequiredFieldError
import com.nima.app.imanage.ui.component.ServiceTypePicker
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.showRequiredFieldsToast
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateCarServiceScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    serviceId: Int = -1,
    viewModel: CarServiceViewModel = koinViewModel(),
    typeViewModel: CarServiceTypeViewModel = koinViewModel()
) {

    val createTitle = stringResource(R.string.create_car_service_title)
    val editTitle = stringResource(R.string.edit_car_service_title)

    val serviceTypes by typeViewModel.types.collectAsState()

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

    var serviceTypeId by remember { mutableIntStateOf(-1) }
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
    var showValidationErrors by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val serviceTypeError = showValidationErrors && serviceTypeId < 0
    val amountError = showValidationErrors && NumberFormatUtils.parseToLong(amountPaid.text) <= 0

    val selectedService by viewModel.selectedService.collectAsState()
    LaunchedEffect(selectedService) {
        selectedService?.let { service ->
            serviceTypeId = service.serviceType
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
        Column(modifier = Modifier.fillMaxWidth()) {
            ServiceTypePicker(
                serviceTypes = serviceTypes,
                selectedTypeId = serviceTypeId,
                onTypeSelected = { serviceTypeId = it ?: -1 },
                onAddType = { title, colorIndex, iconIndex ->
                    typeViewModel.addType(title, colorIndex, iconIndex)
                },
                modifier = Modifier.fillMaxWidth()
            )
            RequiredFieldError(
                visible = serviceTypeError,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { showServiceDatePicker = true }) {
            OutlinedTextField(
                shape = RoundedCornerShape(12.dp),
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
            shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
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
            shape = RoundedCornerShape(12.dp),
            value = nextServiceKilometer,
            onValueChange = { nextServiceKilometer = NumberFormatUtils.formatWithCursor(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.car_next_service_km_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            shape = RoundedCornerShape(12.dp),
            value = amountPaid,
            onValueChange = { amountPaid = NumberFormatUtils.formatWithCursor(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.car_amount_paid_label)) },
            isError = amountError,
            supportingText = if (amountError) {
                { RequiredFieldError(visible = true) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            shape = RoundedCornerShape(12.dp),
            value = productBrand,
            onValueChange = { productBrand = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.car_product_brand_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            shape = RoundedCornerShape(12.dp),
            value = partName,
            onValueChange = { partName = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.car_part_name_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            shape = RoundedCornerShape(12.dp),
            value = description,
            onValueChange = { description = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.car_description_label)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            onClick = {
                val finalAmount = NumberFormatUtils.parseToLong(amountPaid.text)
                if (serviceTypeId < 0 || finalAmount <= 0) {
                    showValidationErrors = true
                    showRequiredFieldsToast(context)
                    return@Button
                }
                val service = CarServiceEntity(
                    id = if (serviceId != -1) serviceId else 0,
                    serviceType = serviceTypeId,
                    serviceDate = serviceDate,
                    serviceKilometer = NumberFormatUtils.parseToLong(serviceKilometer.text).toInt(),
                    nextServiceDate = nextServiceDate,
                    nextServiceKilometer = NumberFormatUtils.parseToLong(nextServiceKilometer.text)
                        .toInt(),
                    amountPaid = finalAmount,
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
