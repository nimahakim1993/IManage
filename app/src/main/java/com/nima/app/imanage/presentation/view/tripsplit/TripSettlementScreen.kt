package com.nima.app.imanage.presentation.view.tripsplit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.calculator.ParticipantBalance
import com.nima.app.imanage.presentation.viewmodel.TripDetailViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.TextInputDropDown
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSettlementScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    tripId: Int,
    viewModel: TripDetailViewModel = koinViewModel()
) {
    LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

    val participants by viewModel.participants.collectAsState()
    val balances by viewModel.balances.collectAsState()
    val transactions by viewModel.settlementTransactions.collectAsState()
    val settlements by viewModel.settlements.collectAsState()

    var showSettlementDialog by remember { mutableStateOf(false) }
    var deleteSettlement by remember {
        mutableStateOf<com.nima.app.imanage.data.db.entity.SettlementEntity?>(
            null
        )
    }

    val settlementTitle = stringResource(R.string.settlement_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = settlementTitle,
                showBack = true
            )
        )
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Text(
                    text = stringResource(R.string.settlement_balance_header),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }

            items(balances) { balance ->
                BalanceRow(balance)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settlement_suggestions_header),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                    Button(
                        onClick = { showSettlementDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.settlement_record_button),
                            fontSize = scaledSp(12f)
                        )
                    }
                }
            }

            if (transactions.isEmpty() && settlements.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.settlement_all_cleared),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = vazirFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            }

            items(transactions) { tx ->
                SettlementSuggestionCard(
                    fromName = tx.fromParticipantName,
                    toName = tx.toParticipantName,
                    amount = tx.amount
                )
            }

            items(settlements) { settlement ->
                val fromName =
                    participants.find { it.id == settlement.fromParticipantId }?.name ?: "?"
                val toName = participants.find { it.id == settlement.toParticipantId }?.name ?: "?"

                RecordedSettlementCard(
                    fromName = fromName,
                    toName = toName,
                    amount = settlement.amount,
                    onDelete = { deleteSettlement = settlement }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showSettlementDialog) {
        SettlementRecordDialog(
            participants = participants,
            onDismiss = { showSettlementDialog = false },
            onConfirm = { fromId, toId, amount, date, note ->
                viewModel.recordSettlement(fromId, toId, amount, date, note)
                showSettlementDialog = false
            }
        )
    }

    deleteSettlement?.let { settlement ->
        ActionDialog(
            onDismiss = { deleteSettlement = null },
            onPositiveClicked = {
                viewModel.deleteSettlement(settlement)
                deleteSettlement = null
            }
        )
    }
}

@Composable
private fun BalanceRow(balance: ParticipantBalance) {
    val color = when {
        balance.netBalance > 0.001 -> Color(0xFF2E7D32)
        balance.netBalance < -0.001 -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when {
        balance.netBalance > 0.001 -> stringResource(R.string.settlement_label_creditor)
        balance.netBalance < -0.001 -> stringResource(R.string.settlement_label_debtor)
        else -> stringResource(R.string.settlement_label_settled)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = balance.participantName,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily
                )
                Text(
                    text = stringResource(
                        R.string.settlement_paid_share,
                        NumberFormatUtils.format(balance.totalPaid.toLong()),
                        NumberFormatUtils.format(balance.totalShare.toLong())
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = vazirFontFamily
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$label ${
                        NumberFormatUtils.format(
                            kotlin.math.abs(balance.netBalance).toLong()
                        )
                    }",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(14f),
                    fontFamily = vazirFontFamily
                )
            }
        }
    }
}

@Composable
private fun SettlementSuggestionCard(
    fromName: String,
    toName: String,
    amount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$fromName \u2190 $toName",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily
                )
                Text(
                    text = stringResource(R.string.settlement_suggestion_tag),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = vazirFontFamily
                )
            }
            Text(
                text = NumberFormatUtils.format(amount.toLong()),
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RecordedSettlementCard(
    fromName: String,
    toName: String,
    amount: Double,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$fromName \u2190 $toName",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily
                )
                Text(
                    text = stringResource(R.string.settlement_recorded_tag),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = vazirFontFamily
                )
            }
            Text(
                text = NumberFormatUtils.format(amount.toLong()),
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettlementRecordDialog(
    participants: List<com.nima.app.imanage.data.db.entity.ParticipantEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Double, Long, String) -> Unit
) {
    var fromId by remember { mutableStateOf(0) }
    var toId by remember { mutableStateOf(0) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.settlement_dialog_title),
                fontFamily = vazirFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextInputDropDown(
                    label = stringResource(R.string.settlement_from_label),
                    items = participants.map { it.name },
                    selectedItem = participants.find { it.id == fromId }?.name ?: "",
                    onItemSelected = { index, _ -> fromId = participants.getOrNull(index)?.id ?: 0 }
                )
                TextInputDropDown(
                    label = stringResource(R.string.settlement_to_label),
                    items = participants.map { it.name },
                    selectedItem = participants.find { it.id == toId }?.name ?: "",
                    onItemSelected = { index, _ -> toId = participants.getOrNull(index)?.id ?: 0 }
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.settlement_note_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    if (fromId != 0 && toId != 0 && amount > 0) {
                        onConfirm(fromId, toId, amount, System.currentTimeMillis(), note)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.settlement_save), fontFamily = vazirFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontFamily = vazirFontFamily)
            }
        }
    )
}
