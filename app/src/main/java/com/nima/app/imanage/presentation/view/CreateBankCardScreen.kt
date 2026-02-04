package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun CreateBankCardScreen(
    navController: NavController,
    cardId: Int,
    setToolbar: (ToolbarConfig) -> Unit,
    viewModel: BankCardViewModel = koinViewModel()
) {

    LaunchedEffect(cardId) {
        if (cardId != -1) {
            viewModel.loadCard(cardId)
        }
    }

    var cardNumber by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var month by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var bankName by rememberSaveable { mutableStateOf("") }

    val colorSaver = Saver<Color, Long>(
        save = { it.value.toLong() },
        restore = { Color(it) }
    )

    var cardColor by rememberSaveable(stateSaver = colorSaver) {
        mutableStateOf(Color(0xFF0F5C5A))
    }

    val colors = listOf(
        Color(0xFF0F5C5A),
        Color(0xFF1E3A8A),
        Color(0xFF7C2D12),
        Color(0xFF4C1D95),
        Color(0xFF374151),
    )

    val selectedCard by viewModel.selectedCard.collectAsState()
    LaunchedEffect(selectedCard) {
        selectedCard?.let { card ->
            cardNumber = card.cardNumber
            cvv = card.cvv
            month = card.month
            year = card.year
            bankName = card.bankName
            cardColor = colors.first { it.value.toLong() == card.color }
        }
    }

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = "", actions = listOf(
                ToolbarAction(
                    icon = Icons.Outlined.SettingsBackupRestore,
                    contentDescription = "Reset",
                    onClick = {
                        cardNumber = ""
                        cvv = ""
                        month = ""
                        year = ""
                        bankName = ""
                    }
                )
            ))
        )
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AtmCardPreview(
            editMode = false,
            showSensitive = true,
            cardNumber = cardNumber,
            cvv = cvv,
            month = month,
            year = year,
            bankName = bankName,
            color = cardColor,
            onEdit = {},
            onDelete = {}
        )

        OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text("Bank Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cardNumber,
            onValueChange = {
                if (it.length <= 16 && it.all(Char::isDigit)) cardNumber = it
            },
            label = { Text("Card Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cvv,
            onValueChange = {
                if (it.length <= 4 && it.all(Char::isDigit)) cvv = it
            },
            label = { Text("CVV2") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = month,
                onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) month = it
                },
                label = { Text("Month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = year,
                onValueChange = {
                    if (it.length <= 2 && it.all(Char::isDigit)) year = it
                },
                label = { Text("Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { cardColor = color }
                )
            }
        }

        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            val newCard = BankCardEntity(
                id = if (cardId != -1) cardId else 0,
                cardNumber = cardNumber,
                cvv = cvv,
                month = month,
                year = year,
                bankName = bankName,
                color = cardColor.value.toLong()
            )
            viewModel.saveCard(newCard)
            navController.popBackStack()
        }) {
            Text(text = "تایید")
        }
    }
}

@Composable
fun AtmCardPreview(
    editMode: Boolean,
    showSensitive: Boolean,
    cardNumber: String,
    cvv: String,
    month: String,
    year: String,
    bankName: String,
    color: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val expiry = buildString {
        append(year.ifEmpty { "YY" })
        append("/")
        append(month.ifEmpty { "MM" })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = bankName.ifEmpty { "Bank Name" },
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = cardNumber.chunked(4).joinToString(" "),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                letterSpacing = 2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (!showSensitive) "CVV: ***" else "CVV: ${cvv.ifEmpty { "***" }}",
                    color = Color.White
                )
                Text(
                    text = expiry.ifEmpty { "YY/MM" },
                    color = Color.White
                )
            }

            AnimatedVisibility(visible = editMode) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
