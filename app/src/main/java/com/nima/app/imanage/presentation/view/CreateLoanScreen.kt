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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import com.nima.app.imanage.ui.component.TextInputDropDown
import com.nima.app.imanage.util.NumberFormatUtils
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLoanScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    loanId: Int = -1,
    viewModel: LoanViewModel = koinViewModel()
) {

    val createLoanTitle = stringResource(R.string.create_loan_title)
    val selectText = stringResource(R.string.select)
    val debtLabel = stringResource(R.string.debt)
    val receivableLabel = stringResource(R.string.receivable)

    LaunchedEffect(loanId) {
        if (loanId != -1) {
            viewModel.loadLoan(loanId)
        }
    }

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = createLoanTitle, showBack = true)
        )
    }

    var type by remember { mutableStateOf(selectText) }
    var typeKey by remember { mutableIntStateOf(LoanEntity.TYPE_DEBT) }
    var personName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }
    var dateLoan by remember { mutableStateOf(System.currentTimeMillis()) }
    var dateReceiveBack by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDateLoanPicker by remember { mutableStateOf(false) }
    var showDateReceiveBackPicker by remember { mutableStateOf(false) }

    val selectedLoan by viewModel.selectedLoan.collectAsState()
    LaunchedEffect(selectedLoan) {
        selectedLoan?.let { loan ->
            type = if (loan.type == LoanEntity.TYPE_DEBT) debtLabel else receivableLabel
            typeKey = loan.type
            personName = loan.targetPersonName
            price = TextFieldValue(NumberFormatUtils.format(loan.price))
            description = loan.description
            dateLoan = loan.dateLoan
            dateReceiveBack = loan.dateReceiveBack
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    if (showDateLoanPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateLoan)
        DatePickerDialog(
            onDismissRequest = { showDateLoanPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateLoan = it }
                    showDateLoanPicker = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDateLoanPicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDateReceiveBackPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateReceiveBack)
        DatePickerDialog(
            onDismissRequest = { showDateReceiveBackPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateReceiveBack = it }
                    showDateReceiveBackPicker = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDateReceiveBackPicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextInputDropDown(
            label = stringResource(R.string.loan_type_label),
            items = listOf(stringResource(R.string.debt), stringResource(R.string.receivable)),
            selectedItem = type,
            onItemSelected = { key, name ->
                type = name
                typeKey = key
            }
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = personName,
            onValueChange = { personName = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.person_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = NumberFormatUtils.formatWithCursor(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(stringResource(R.string.amount)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(10.dp))

        Box(modifier = Modifier.fillMaxWidth().clickable { showDateLoanPicker = true }) {
            OutlinedTextField(
                value = dateFormat.format(Date(dateLoan)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.payment_date)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        Box(modifier = Modifier.fillMaxWidth().clickable { showDateReceiveBackPicker = true }) {
            OutlinedTextField(
                value = dateFormat.format(Date(dateReceiveBack)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.receive_date)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
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

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            onClick = {
                val loan = LoanEntity(
                    id = if (loanId != -1) loanId else 0,
                    type = typeKey,
                    price = NumberFormatUtils.parseToLong(price.text),
                    targetPersonName = personName,
                    description = description,
                    dateLoan = dateLoan,
                    dateReceiveBack = dateReceiveBack
                )
                viewModel.saveLoan(loan)
                navController.popBackStack()
            },

            ) {
            Text(stringResource(R.string.confirm), fontWeight = FontWeight.Bold)
        }


    }
}