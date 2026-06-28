package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import com.nima.app.imanage.ui.component.CategoryPicker
import com.nima.app.imanage.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpenseSheet(
    categories: List<ExpenseCategoryEntity>,
    editing: ExpenseEntity?,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit,
    onAddCategory: (title: String, colorIndex: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = editing != null

    val sheetTitle = stringResource(if (isEdit) R.string.edit_expense_title else R.string.create_expense_title)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var categoryId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(editing) {
        editing?.let { expense ->
            title = expense.title
            description = expense.description
            amount = TextFieldValue(NumberFormatUtils.format(expense.amount))
            categoryId = expense.categoryId
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = sheetTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = com.nima.app.imanage.ui.theme.vazirFontFamily
            )
            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.expense_title_label)) },
                placeholder = { Text(stringResource(R.string.expense_title_hint)) },
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

            CategoryPicker(
                categories = categories,
                selectedCategoryId = categoryId,
                onCategorySelected = { categoryId = it },
                onAddCategory = { newTitle, colorIndex ->
                    onAddCategory(newTitle, colorIndex)
                },
                modifier = Modifier.fillMaxWidth()
            )

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
                    val expense = ExpenseEntity(
                        id = editing?.id ?: 0,
                        title = finalTitle,
                        description = description.trim(),
                        amount = NumberFormatUtils.parseToLong(amount.text),
                        categoryId = categoryId,
                        createdAt = editing?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(expense)
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
                    fontFamily = com.nima.app.imanage.ui.theme.vazirFontFamily
                )
            }

            Spacer(modifier = Modifier.size(12.dp))
        }
    }
}
