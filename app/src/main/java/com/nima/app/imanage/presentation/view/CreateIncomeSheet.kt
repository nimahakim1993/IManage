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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.IncomeEntity
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import com.nima.app.imanage.ui.component.IncomeSourcePicker
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIncomeSheet(
    sources: List<IncomeSourceEntity>,
    editing: IncomeEntity?,
    onDismiss: () -> Unit,
    onSave: (IncomeEntity) -> Unit,
    onAddSource: (title: String, colorIndex: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = editing != null

    val sheetTitle = stringResource(if (isEdit) R.string.edit_income_title else R.string.create_income_title)

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var sourceId by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var incomeDate by remember { mutableStateOf(ShamsiDate.todayMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(editing) {
        editing?.let { income ->
            title = income.title
            amount = TextFieldValue(NumberFormatUtils.format(income.amount))
            sourceId = income.sourceId
            description = income.description
            incomeDate = income.incomeDate
        }
    }

    if (showDatePicker) {
        ShamsiDatePicker(
            initialDate = incomeDate,
            title = stringResource(R.string.select_income_date),
            onConfirm = { newDate ->
                incomeDate = newDate
                showDatePicker = false
            },
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
                text = sheetTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.income_title_label)) },
                placeholder = { Text(stringResource(R.string.income_title_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = NumberFormatUtils.formatWithCursor(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.amount)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(12.dp))

            IncomeSourcePicker(
                sources = sources,
                selectedSourceId = sourceId,
                onSourceSelected = { sourceId = it },
                onAddSource = { newTitle, colorIndex ->
                    onAddSource(newTitle, colorIndex)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(12.dp))

            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = ShamsiDate.format(incomeDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.income_date)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                placeholder = { Text(stringResource(R.string.description_optional_hint)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(20.dp))

            Button(
                onClick = {
                    val finalTitle = title.trim()
                    if (finalTitle.isBlank()) return@Button
                    val income = IncomeEntity(
                        id = editing?.id ?: 0,
                        title = finalTitle,
                        amount = NumberFormatUtils.parseToLong(amount.text),
                        sourceId = sourceId,
                        description = description.trim(),
                        incomeDate = incomeDate,
                        createdAt = editing?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(income)
                },
                enabled = title.isNotBlank(),
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
