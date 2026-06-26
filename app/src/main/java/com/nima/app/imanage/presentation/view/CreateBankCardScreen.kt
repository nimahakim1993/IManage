package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.util.NumberFormatUtils
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
    var shebaNumber by rememberSaveable { mutableStateOf("") }
    var accountNumber by rememberSaveable { mutableStateOf("") }

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
            shebaNumber = card.shebaNumber.orEmpty()
            accountNumber = card.accountNumber.orEmpty()
            cardColor = colors.first { it.value.toLong() == card.color }
        }
    }

    val resetDesc = stringResource(R.string.reset)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = "", actions = listOf(
                ToolbarAction(
                    icon = Icons.Outlined.SettingsBackupRestore,
                    contentDescription = resetDesc,
                    onClick = {
                        cardNumber = ""
                        cvv = ""
                        month = ""
                        year = ""
                        bankName = ""
                        shebaNumber = ""
                        accountNumber = ""
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
            label = { Text(stringResource(R.string.bank_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cardNumber,
            onValueChange = {
                if (it.length <= 16 && it.all(Char::isDigit)) cardNumber = it
            },
            label = { Text(stringResource(R.string.card_number)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cvv,
            onValueChange = {
                if (it.length <= 4 && it.all(Char::isDigit)) cvv = it
            },
            label = { Text(stringResource(R.string.cvv2)) },
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
                label = { Text(stringResource(R.string.month)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

        OutlinedTextField(
            value = year,
            onValueChange = {
                if (it.length <= 2 && it.all(Char::isDigit)) year = it
            },
            label = { Text(stringResource(R.string.year)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }

    OutlinedTextField(
        value = shebaNumber,
        onValueChange = { shebaNumber = it.uppercase() },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text(stringResource(R.string.sheba_number)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = accountNumber,
        onValueChange = {
            if (it.length <= 20 && it.all(Char::isDigit)) accountNumber = it
        },
        label = { Text(stringResource(R.string.account_number)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

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
                color = cardColor.value.toLong(),
                shebaNumber = shebaNumber.takeIf { it.isNotBlank() },
                accountNumber = accountNumber.takeIf { it.isNotBlank() }
            )
            viewModel.saveCard(newCard)
            navController.popBackStack()
        }) {
            Text(text = stringResource(R.string.confirm))
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
    onDelete: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    onCopyCardNumber: (() -> Unit)? = null
) {

    val yyPlaceholder = stringResource(R.string.yy_placeholder)
    val mmPlaceholder = stringResource(R.string.mm_placeholder)
    val moreOptionsDesc = stringResource(R.string.more_options)
    val expiry = buildString {
        append(year.ifEmpty { yyPlaceholder })
        append("/")
        append(month.ifEmpty { mmPlaceholder })
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
                .padding(20.dp)
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bankName.ifEmpty { stringResource(R.string.bank_name_placeholder) },
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = if (onMenuClick != null) 40.dp else 0.dp)
                )
                if (onMenuClick != null) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = moreOptionsDesc,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = NumberFormatUtils.toLocalizedDigits(cardNumber).chunked(4).joinToString(" "),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    letterSpacing = 2.sp
                )
                if (onCopyCardNumber != null) {
                    IconButton(onClick = onCopyCardNumber) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.copy),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (!showSensitive) stringResource(R.string.cvv_masked) else NumberFormatUtils.toLocalizedDigits(stringResource(R.string.cvv_format, cvv.ifEmpty { "***" })),
                    color = Color.White
                )
                Text(
                    text = NumberFormatUtils.toLocalizedDigits(expiry).ifEmpty { stringResource(R.string.yy_mm_placeholder) },
                    color = Color.White
                )
            }

            AnimatedVisibility(visible = editMode) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
