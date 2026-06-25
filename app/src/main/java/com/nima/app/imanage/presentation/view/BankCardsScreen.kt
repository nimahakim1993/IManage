package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import org.koin.androidx.compose.koinViewModel


@Composable
fun BankCardsScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    viewModel: BankCardViewModel = koinViewModel()
) {
    val cards by viewModel.cards.collectAsState()

    var toggleSensitive by rememberSaveable { mutableStateOf(false) }
    var toggleEditMode by rememberSaveable { mutableStateOf(false) }

    var removingCard by remember { mutableStateOf<BankCardEntity?>(null) }

    val bankAccountTitle = stringResource(R.string.bank_account)
    val visibilityDesc = stringResource(R.string.visibility)
    val editDesc = stringResource(R.string.edit)
    val addAccountDesc = stringResource(R.string.add_account)

    LaunchedEffect(toggleSensitive, toggleEditMode) {
        setToolbar(ToolbarConfig(title = bankAccountTitle,
            showBack = true,
            actions = listOf(
                ToolbarAction(
                    icon = if (toggleSensitive)
                        Icons.Default.VisibilityOff
                    else
                        Icons.Default.Visibility,
                    contentDescription = visibilityDesc,
                    onClick = { toggleSensitive = !toggleSensitive }
                ),
                ToolbarAction(
                    icon = if (toggleEditMode)
                        Icons.Default.EditOff
                    else
                        Icons.Default.Edit,
                    contentDescription = editDesc,
                    onClick = { toggleEditMode = !toggleEditMode }
                ),
                ToolbarAction(
                    icon = Icons.Default.Add,
                    contentDescription = addAccountDesc,
                    onClick = {
                        navController.navigate(Screen.CreateBankCard.route)
                    }
                ),
            ))
        )
    }

    val colors = listOf(
        Color(0xFF0F5C5A),
        Color(0xFF1E3A8A),
        Color(0xFF7C2D12),
        Color(0xFF4C1D95),
        Color(0xFF374151),
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cards, key = { it.id }) { card ->

                val cardColor = colors.firstOrNull { it.value.toLong() == card.color }
                    ?: colors.first()

                AtmCardPreview(
                    editMode = toggleEditMode,
                    showSensitive = toggleSensitive,
                    cardNumber = card.cardNumber,
                    cvv = card.cvv,
                    month = card.month,
                    year = card.year,
                    bankName = card.bankName,
                    color = cardColor,
                    onEdit = {
                        navController.navigate(Screen.CreateBankCard.createRoute(card.id))
                    },
                    onDelete = {
                        removingCard = card
                    }
                )
            }
        }
    }

    removingCard?.let { card ->
        ActionDialog(
            onDismiss = { removingCard = null },
            onPositiveClicked = {
                viewModel.removeCard(card)
                removingCard = null
            }
        )
    }
}