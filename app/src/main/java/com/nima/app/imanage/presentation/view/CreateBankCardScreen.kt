package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nima.app.imanage.BuildConfig
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.ui.component.ColorPaletteGrid
import com.nima.app.imanage.ui.component.RequiredFieldError
import com.nima.app.imanage.ui.component.showRequiredFieldsToast
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.util.ColorUtils
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.normalizeDigits
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
    var showValidationErrors by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val bankNameError = showValidationErrors && bankName.isBlank()
    val cardNumberError = showValidationErrors && cardNumber.isBlank()
    val cvvError = showValidationErrors && cvv.isBlank()
    val monthError = showValidationErrors && month.isBlank()
    val yearError = showValidationErrors && year.isBlank()

    val colorSaver = Saver<Color, Long>(
        save = { it.value.toLong() },
        restore = { Color(it) }
    )

    var cardColor by rememberSaveable(stateSaver = colorSaver) {
        mutableStateOf(ColorUtils.colors.first())
    }

    val yearFocusRequester = remember { FocusRequester() }
    val colors = ColorUtils.colors

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
            ToolbarConfig(
                title = "",
                actions = listOf(
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
                )
            )
        )
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
            shape = RoundedCornerShape(12.dp),
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text(stringResource(R.string.bank_name)) },
            isError = bankNameError,
            supportingText = if (bankNameError) {
                { RequiredFieldError(visible = true) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            shape = RoundedCornerShape(12.dp),
            value = NumberFormatUtils.toLocalizedDigits(cardNumber),
            onValueChange = {
                val normalized = it.normalizeDigits()
                if (normalized.length <= 16 && normalized.all(Char::isDigit)) cardNumber =
                    normalized
            },
            label = { Text(stringResource(R.string.card_number)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = cardNumberError,
            supportingText = if (cardNumberError) {
                { RequiredFieldError(visible = true) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        if (!BuildConfig.HIDE_CARD_SENSITIVE) {
            OutlinedTextField(
                shape = RoundedCornerShape(12.dp),
                value = NumberFormatUtils.toLocalizedDigits(cvv),
                onValueChange = {
                    val normalized = it.normalizeDigits()
                    if (normalized.length <= 4 && normalized.all(Char::isDigit)) cvv = normalized
                },
                label = { Text(stringResource(R.string.cvv2)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = cvvError,
                supportingText = if (cvvError) {
                    { RequiredFieldError(visible = true) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    shape = RoundedCornerShape(12.dp),
                    value = NumberFormatUtils.toLocalizedDigits(month),
                    onValueChange = {
                        val normalized = it.normalizeDigits()
                        if (normalized.length <= 2 && normalized.all(Char::isDigit)) {
                            month = normalized
                            if (normalized.length == 2) yearFocusRequester.requestFocus()
                        }
                    },
                    label = { Text(stringResource(R.string.month)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = monthError,
                    supportingText = if (monthError) {
                        { RequiredFieldError(visible = true) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    shape = RoundedCornerShape(12.dp),
                    value = NumberFormatUtils.toLocalizedDigits(year),
                    onValueChange = {
                        val normalized = it.normalizeDigits()
                        if (normalized.length <= 2 && normalized.all(Char::isDigit)) year =
                            normalized
                    },
                    label = { Text(stringResource(R.string.year)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = yearError,
                    supportingText = if (yearError) {
                        { RequiredFieldError(visible = true) }
                    } else null,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(yearFocusRequester)
                )
            }
        }

    OutlinedTextField(
        shape = RoundedCornerShape(12.dp),
        value = NumberFormatUtils.toLocalizedDigits(shebaNumber),
        onValueChange = { shebaNumber = it.normalizeDigits().uppercase() },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text(stringResource(R.string.sheba_number)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        shape = RoundedCornerShape(12.dp),
        value = NumberFormatUtils.toLocalizedDigits(accountNumber),
        onValueChange = {
            val normalized = it.normalizeDigits()
            if (normalized.length <= 20 && normalized.all(Char::isDigit)) accountNumber = normalized
        },
        label = { Text(stringResource(R.string.account_number)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

        ColorPaletteGrid(
            selectedIndex = colors.indexOf(cardColor),
            onSelect = { cardColor = colors[it] }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            onClick = {
                val sensitiveValid = BuildConfig.HIDE_CARD_SENSITIVE ||
                    (!cvv.isBlank() && !month.isBlank() && !year.isBlank())
                if (bankName.isBlank() || cardNumber.isBlank() || !sensitiveValid) {
                    showValidationErrors = true
                    showRequiredFieldsToast(context)
                    return@Button
                }
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
            }
        ) {
            Text(text = stringResource(R.string.confirm))
        }
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
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bankName.ifEmpty { stringResource(R.string.bank_name_placeholder) },
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontSize = scaledSp(18f),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = NumberFormatUtils.toLocalizedDigits(cardNumber).chunked(4)
                            .joinToString("  "),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = scaledSp(20f),
                        letterSpacing = 2.sp
                    )
                }
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

            Column {
                if (!BuildConfig.HIDE_CARD_SENSITIVE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = if (!showSensitive) {
                                    stringResource(R.string.cvv_masked)
                                } else {
                                    if (cvv.isBlank()) stringResource(R.string.cvv_format, stringResource(R.string.not_set)) else NumberFormatUtils.toLocalizedDigits(stringResource(R.string.cvv_format, cvv))
                                },
                                fontSize = scaledSp(14f),
                                color = Color.White
                            )
                            Text(
                                text = NumberFormatUtils.toLocalizedDigits(expiry).ifEmpty { stringResource(R.string.yy_mm_placeholder) },
                                fontSize = scaledSp(14f),
                                color = Color.White
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = editMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

