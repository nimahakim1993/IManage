package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.event.ToolbarEvent
import com.nima.app.imanage.data.event.ToolbarEventBus
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun BankAccountScreen(
    navController: NavController,
    viewModel: BankCardViewModel = koinViewModel()
) {
    val showSensitive by viewModel.showSensitive.collectAsState()
    val editMode by viewModel.editMode.collectAsState()
    val cards by viewModel.cards.collectAsState()

    LaunchedEffect(Unit) {
        ToolbarEventBus.events.collect { event ->
            if (event is ToolbarEvent.ToggleSensitive) {
                viewModel.toggleSensitive()
            }
        }
    }
    LaunchedEffect(Unit) {
        ToolbarEventBus.events.collect { event ->
            if (event is ToolbarEvent.ToggleEdit) {
                viewModel.toggleEditMode()
            }
        }
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
                    editMode = editMode,
                    showSensitive = showSensitive,
                    cardNumber = card.cardNumber,
                    cvv = card.cvv,
                    month = card.month,
                    year = card.year,
                    bankName = card.bankName,
                    color = cardColor,
                    onEdit = {
                        navController.navigate(Screen.CreateBankAccount.createRoute(card.id))
                    },
                    onDelete = {
                        viewModel.removeCard(card)
                    }
                )
            }
        }
    }
}