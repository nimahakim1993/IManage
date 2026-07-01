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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.InstallmentViewModel
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.TextInputDropDown
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

private fun digitsOnlyWithCursor(value: TextFieldValue): TextFieldValue {
    val oldText = value.text
    val newText = oldText.filter { it.isDigit() }
    if (newText == oldText) return value
    val oldCursor = value.selection.end.coerceIn(0, oldText.length)
    val digitsBeforeCursor = oldText.substring(0, oldCursor).count { it.isDigit() }
    val newCursor = if (digitsBeforeCursor <= 0) 0
    else {
        var seen = 0
        var pos = 0
        for (c in newText) {
            if (c.isDigit()) {
                seen++
                if (seen == digitsBeforeCursor) {
                    pos++
                    break
                }
            }
            pos++
        }
        pos.coerceIn(0, newText.length)
    }
    return TextFieldValue(text = newText, selection = TextRange(newCursor))
}

@Composable
fun CreateInstallmentScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    installmentId: Int = -1,
    viewModel: InstallmentViewModel = koinViewModel()
) {
    val titleStr = if (installmentId == -1)
        stringResource(R.string.create_installment_title)
    else
        stringResource(R.string.edit_installment_title)

    LaunchedEffect(installmentId) {
        if (installmentId != -1) {
            viewModel.loadInstallment(installmentId)
        }
    }

    LaunchedEffect(Unit) {
        setToolbar(ToolbarConfig(title = titleStr, showBack = true))
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var numInstallments by remember { mutableStateOf(TextFieldValue("")) }
    var periodType by remember { mutableIntStateOf(InstallmentEntity.PERIOD_MONTHLY) }
    var periodDays by remember { mutableStateOf(TextFieldValue("")) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf(ShamsiDate.todayMillis()) }
    var showStartDatePicker by remember { mutableStateOf(false) }

    val periodTypeText = when (periodType) {
        InstallmentEntity.PERIOD_MONTHLY -> stringResource(R.string.period_monthly)
        InstallmentEntity.PERIOD_WEEKLY -> stringResource(R.string.period_weekly)
        else -> stringResource(R.string.period_custom)
    }

    val selectedInstallment by viewModel.selectedInstallment.collectAsState()
    LaunchedEffect(selectedInstallment) {
        selectedInstallment?.let { inst ->
            title = inst.title
            description = inst.description
            numInstallments = TextFieldValue(inst.numberOfInstallments.toString())
            periodType = inst.periodType
            periodDays = TextFieldValue(inst.periodDays.toString())
            amount = TextFieldValue(NumberFormatUtils.format(inst.amount))
            startDate = inst.startDate
        }
    }

    val showPeriodDaysField = periodType == InstallmentEntity.PERIOD_CUSTOM

    val periodTypeItems = listOf(
        stringResource(R.string.period_monthly),
        stringResource(R.string.period_weekly),
        stringResource(R.string.period_custom)
    )

    if (showStartDatePicker) {
        ShamsiDatePicker(
            initialDate = startDate,
            title = stringResource(R.string.select_start_date),
            onConfirm = { newDate ->
                startDate = newDate
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.installment_title_label)) },
            placeholder = { Text(stringResource(R.string.installment_title_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = NumberFormatUtils.formatWithCursor(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.amount)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = numInstallments,
            onValueChange = { newValue ->
                numInstallments = digitsOnlyWithCursor(newValue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.number_of_installments)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        TextInputDropDown(
            label = stringResource(R.string.period_type),
            items = periodTypeItems,
            selectedItem = periodTypeText,
            onItemSelected = { index, _ ->
                periodType = index
            }
        )

        if (showPeriodDaysField) {
            Spacer(modifier = Modifier.size(10.dp))

            OutlinedTextField(
                value = periodDays,
                onValueChange = { newValue ->
                    periodDays = digitsOnlyWithCursor(newValue)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.period_days)) },
                placeholder = { Text(stringResource(R.string.period_days_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        Box(modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true }) {
            OutlinedTextField(
                value = ShamsiDate.format(startDate),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.start_date)) },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            onClick = {
                val num = numInstallments.text.toIntOrNull() ?: 0
                val period = if (periodType == InstallmentEntity.PERIOD_CUSTOM) {
                    periodDays.text.toIntOrNull() ?: 30
                } else {
                    30
                }
                if (title.isBlank() || num <= 0) return@Button

                val installment = InstallmentEntity(
                    id = if (installmentId != -1) installmentId else 0,
                    title = title.trim(),
                    description = description.trim(),
                    numberOfInstallments = num,
                    periodType = periodType,
                    periodDays = period,
                    amount = NumberFormatUtils.parseToLong(amount.text),
                    startDate = startDate,
                    createdAt = selectedInstallment?.createdAt ?: 0
                )
                viewModel.saveInstallment(installment)
                navController.popBackStack()
            }
        ) {
            Text(stringResource(R.string.confirm), fontWeight = FontWeight.Bold)
        }
    }
}
