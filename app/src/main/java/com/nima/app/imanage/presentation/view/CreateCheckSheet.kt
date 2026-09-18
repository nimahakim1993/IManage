package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CheckCounterpartyEntity
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.ui.component.CheckCounterpartyPicker
import com.nima.app.imanage.ui.component.RequiredFieldError
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.showRequiredFieldsToast
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCheckSheet(
    counterparties: List<CheckCounterpartyEntity>,
    onAddCounterparty: (String) -> Unit,
    editing: CheckEntity?,
    onDismiss: () -> Unit,
    onSave: (CheckEntity) -> Unit
) {
    val isEdit = editing != null
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var type by remember { mutableStateOf(CheckEntity.TYPE_RECEIVED) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var state by remember { mutableStateOf(CheckEntity.STATE_IN_PROGRESS) }
    var checkNumber by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(ShamsiDate.todayMillis()) }
    var counterparty by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    LaunchedEffect(editing) {
        editing?.let {
            type = it.type
            amount = TextFieldValue(NumberFormatUtils.format(it.amount))
            state = it.state
            checkNumber = it.checkNumber
            dueDate = it.dueDate
            counterparty = it.counterparty
            description = it.description
        }
    }

    val amountError = showErrors && NumberFormatUtils.parseToLong(amount.text) <= 0
    val numberError = showErrors && checkNumber.isBlank()
    val counterpartyError = showErrors && counterparty.isBlank()

    if (showDatePicker) {
        ShamsiDatePicker(
            initialDate = dueDate,
            title = stringResource(R.string.check_due_date),
            onConfirm = { dueDate = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(if (isEdit) R.string.edit_check_title else R.string.create_check_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.size(16.dp))
            CheckDropdown(
                label = stringResource(R.string.check_type),
                value = checkTypeLabel(type),
                options = listOf(
                    CheckEntity.TYPE_RECEIVED to stringResource(R.string.check_received),
                    CheckEntity.TYPE_PAYABLE to stringResource(R.string.check_payable)
                ),
                onSelected = { type = it }
            )
            Spacer(Modifier.size(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = NumberFormatUtils.formatWithCursor(it) },
                label = { Text(stringResource(R.string.check_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {
                    { RequiredFieldError(visible = true) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.size(12.dp))
            CheckDropdown(
                label = stringResource(R.string.check_state),
                value = checkStateLabel(state),
                options = checkStates().map { it.first to it.second },
                onSelected = { state = it }
            )
            Spacer(Modifier.size(12.dp))
            OutlinedTextField(
                value = checkNumber,
                onValueChange = { checkNumber = it },
                label = { Text(stringResource(R.string.check_number)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = numberError,
                supportingText = if (numberError) {
                    { RequiredFieldError(visible = true) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.size(12.dp))
            Box(Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = ShamsiDate.format(dueDate),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.check_due_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            CheckCounterpartyPicker(
                counterparties = counterparties,
                selectedTitle = counterparty,
                onSelected = { counterparty = it },
                onAdd = onAddCounterparty,
                isError = counterpartyError,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                placeholder = { Text(stringResource(R.string.check_description_hint)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.size(20.dp))
            Button(
                onClick = {
                    val parsedAmount = NumberFormatUtils.parseToLong(amount.text)
                    if (parsedAmount <= 0 || checkNumber.isBlank() || counterparty.isBlank()) {
                        showErrors = true
                        showRequiredFieldsToast(context)
                        return@Button
                    }
                    onSave(
                        CheckEntity(
                            id = editing?.id ?: 0,
                            type = type,
                            amount = parsedAmount,
                            state = state,
                            checkNumber = checkNumber.trim(),
                            dueDate = dueDate,
                            counterparty = counterparty.trim(),
                            description = description.trim(),
                            settled = editing?.settled ?: false,
                            settledAt = editing?.settledAt ?: 0,
                            createdAt = editing?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text(stringResource(R.string.confirm)) }
            Spacer(Modifier.size(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = {
                    onSelected(key)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun checkTypeLabel(type: String): String = stringResource(
    if (type == CheckEntity.TYPE_RECEIVED) R.string.check_received else R.string.check_payable
)

@Composable
private fun checkStateLabel(state: String): String = stringResource(
    when (state) {
        CheckEntity.STATE_IN_BANK -> R.string.check_state_in_bank
        CheckEntity.STATE_COLLECTED -> R.string.check_state_collected
        CheckEntity.STATE_RETURNED -> R.string.check_state_returned
        CheckEntity.STATE_BOUNCED -> R.string.check_state_bounced
        else -> R.string.check_state_in_progress
    }
)

@Composable
private fun checkStates(): List<Pair<String, String>> = listOf(
    CheckEntity.STATE_IN_PROGRESS to stringResource(R.string.check_state_in_progress),
    CheckEntity.STATE_IN_BANK to stringResource(R.string.check_state_in_bank),
    CheckEntity.STATE_COLLECTED to stringResource(R.string.check_state_collected),
    CheckEntity.STATE_RETURNED to stringResource(R.string.check_state_returned),
    CheckEntity.STATE_BOUNCED to stringResource(R.string.check_state_bounced)
)
