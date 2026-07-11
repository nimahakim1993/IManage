package com.nima.app.imanage.presentation.view.tripsplit

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.TripDetailViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.theme.NoteBoxPalettes
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    tripId: Int
) {
    val activity = LocalContext.current as FragmentActivity
    val viewModel: TripDetailViewModel = koinViewModel(viewModelStoreOwner = activity)
    LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

    val tripName by viewModel.tripName.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val balances by viewModel.balances.collectAsState()

    val fallbackTitle = stringResource(R.string.trip_detail_fallback_title)
    val addExpenseDesc = stringResource(R.string.expense_create_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = tripName.ifEmpty { fallbackTitle },
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.Add,
                        contentDescription = addExpenseDesc,
                        onClick = {
                            navController.navigate(
                                Screen.CreateTripExpense.createRoute(
                                    tripId
                                )
                            )
                        }
                    )
                )
            )
        )
    }

    var deleteExpense by remember {
        mutableStateOf<com.nima.app.imanage.data.db.entity.TripExpenseEntity?>(
            null
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateTripExpense.createRoute(tripId)) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = addExpenseDesc,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        if (participants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.trip_detail_loading), fontFamily = vazirFontFamily)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    SummaryCard(
                        totalExpenses = totalExpenses,
                        participantCount = participants.size
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.trip_participants_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    participants.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { participant ->
                                val balance = balances.find { it.participantId == participant.id }
                                val color =
                                    NoteBoxPalettes.getOrElse(participant.colorIndex) { NoteBoxPalettes.first() }
                                ParticipantChip(
                                    participant.name,
                                    balance?.netBalance ?: 0.0,
                                    color.secondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.trip_expenses_header),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = vazirFontFamily
                        )
                        OutlinedButton(
                            onClick = {
                                navController.navigate(
                                    Screen.TripSettlement.createRoute(
                                        tripId
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                stringResource(R.string.trip_settlement_button),
                                fontFamily = vazirFontFamily
                            )
                        }
                    }
                }

                items(expenses, key = { it.id }) { expense ->
                    val payer = participants.find { it.id == expense.payerParticipantId }
                    val payerName = payer?.name ?: "?"
                    val payerColor = NoteBoxPalettes.getOrElse(
                        payer?.colorIndex ?: 0
                    ) { NoteBoxPalettes.first() }
                    ExpenseCard(
                        expense = expense,
                        payerName = payerName,
                        payerColor = payerColor.secondary,
                        onEdit = {
                            navController.navigate(
                                Screen.CreateTripExpense.createRoute(
                                    tripId,
                                    expense.id
                                )
                            )
                        },
                        onDelete = { deleteExpense = expense }
                    )
                }

                if (expenses.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.trip_no_expenses),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = vazirFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    deleteExpense?.let { expense ->
        ActionDialog(
            onDismiss = { deleteExpense = null },
            onPositiveClicked = {
                viewModel.deleteExpense(expense)
                deleteExpense = null
            }
        )
    }
}

@Composable
private fun SummaryCard(totalExpenses: Double, participantCount: Int) {
    val dark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dark) Color(0xFF1E3A5F) else Color(0xFF1565C0)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStat(
                label = stringResource(R.string.trip_total_expenses),
                value = NumberFormatUtils.format(totalExpenses.toLong()),
                color = Color.White
            )
            SummaryStat(
                label = stringResource(R.string.trip_participant_count),
                value = NumberFormatUtils.format(participantCount),
                color = Color.White
            )
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = color.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontFamily = vazirFontFamily
        )
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            fontFamily = vazirFontFamily
        )
    }
}

@Composable
private fun RowScope.ParticipantChip(name: String, netBalance: Double, dotColor: Color) {
    val chipColor = when {
        netBalance > 0.001 -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        netBalance < -0.001 -> Color(0xFFF44336).copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        netBalance > 0.001 -> Color(0xFF2E7D32)
        netBalance < -0.001 -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val sign = when {
        netBalance > 0.001 -> "+"
        netBalance < -0.001 -> "-"
        else -> ""
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(chipColor)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$sign${NumberFormatUtils.format(kotlin.math.abs(netBalance).toLong())}",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontFamily = vazirFontFamily
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: com.nima.app.imanage.data.db.entity.TripExpenseEntity,
    payerName: String,
    payerColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(payerColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = payerColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$payerName \u2022 ${ShamsiDate.format(expense.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = vazirFontFamily
                    )
                }
            }
            Text(
                text = NumberFormatUtils.format(expense.amount.toLong()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}
