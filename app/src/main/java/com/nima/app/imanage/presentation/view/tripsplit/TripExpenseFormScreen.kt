package com.nima.app.imanage.presentation.view.tripsplit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.SplitType
import com.nima.app.imanage.presentation.viewmodel.TripDetailViewModel
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.TextInputDropDown
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripExpenseFormScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    tripId: Int,
    expenseId: Int?,
    viewModel: TripDetailViewModel = koinViewModel()
) {
    LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

    val participants by viewModel.participants.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    val existing = expenseId?.let { eid -> expenses.find { it.id == eid } }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var amount by remember {
        mutableStateOf(TextFieldValue(existing?.amount?.let {
            NumberFormatUtils.format(
                it.toLong()
            )
        } ?: ""))
    }
    var date by remember { mutableLongStateOf(existing?.date ?: System.currentTimeMillis()) }
    var payerId by remember { mutableStateOf(existing?.payerParticipantId ?: 0) }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var involvedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEdit = expenseId != null
    var editInitialized by remember { mutableStateOf(false) }

    val toolbarTitle =
        stringResource(if (isEdit) R.string.expense_edit_title else R.string.expense_create_title)

    LaunchedEffect(existing) {
        if (isEdit && existing != null && !editInitialized) {
            title = existing.title
            amount = TextFieldValue(NumberFormatUtils.format(existing.amount.toLong()))
            date = existing.date
            payerId = existing.payerParticipantId
            description = existing.description
            editInitialized = true
        }
    }

    LaunchedEffect(participants) {
        if (!isEdit && participants.isNotEmpty() && involvedIds.isEmpty()) {
            involvedIds = participants.map { it.id }.toSet()
            if (payerId == 0) payerId = participants.first().id
        }
    }

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = toolbarTitle,
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.Check,
                        contentDescription = "Save",
                        onClick = {
                            val amountVal = NumberFormatUtils.parseToLong(amount.text).toDouble()
                            if (title.isBlank() || involvedIds.isEmpty() || payerId == 0 || amountVal <= 0) return@ToolbarAction
                            if (isEdit && expenseId != null) {
                                viewModel.updateExpense(
                                    expenseId = expenseId,
                                    title = title,
                                    amount = amountVal,
                                    date = date,
                                    payerParticipantId = payerId,
                                    description = description,
                                    splitType = splitType,
                                    involvedParticipantIds = involvedIds.toList()
                                )
                            } else {
                                viewModel.createExpense(
                                    title = title,
                                    amount = amountVal,
                                    date = date,
                                    payerParticipantId = payerId,
                                    description = description,
                                    splitType = splitType,
                                    involvedParticipantIds = involvedIds.toList()
                                )
                            }
                            navController.popBackStack()
                        }
                    )
                )
            )
        )
    }

    if (showDatePicker) {
        ShamsiDatePicker(
            initialDate = date,
            onConfirm = { newDate ->
                date = newDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.expense_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = NumberFormatUtils.formatWithCursor(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.expense_amount_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = ShamsiDate.format(date),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.expense_date_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (participants.isNotEmpty()) {
                TextInputDropDown(
                    label = stringResource(R.string.expense_payer_label),
                    items = participants.map { it.name },
                    selectedItem = participants.find { it.id == payerId }?.name ?: "",
                    onItemSelected = { index, _ ->
                        payerId = participants.getOrNull(index)?.id ?: 0
                    }
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.expense_description_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            Text(
                text = stringResource(R.string.expense_involved_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )

            participants.forEach { participant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            involvedIds = if (participant.id in involvedIds)
                                involvedIds - participant.id
                            else
                                involvedIds + participant.id
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = participant.id in involvedIds,
                        onCheckedChange = { checked ->
                            involvedIds = if (checked)
                                involvedIds + participant.id
                            else
                                involvedIds - participant.id
                        }
                    )
                    Text(
                        text = participant.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = vazirFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
