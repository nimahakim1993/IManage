package com.nima.app.imanage.presentation.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.BankCardViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
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
    var sheetCard by remember { mutableStateOf<BankCardEntity?>(null) }

    val bankAccountTitle = stringResource(R.string.bank_account)
    val visibilityDesc = stringResource(R.string.visibility)
    val editDesc = stringResource(R.string.edit)
    val addAccountDesc = stringResource(R.string.add_account)
    val dragDesc = stringResource(R.string.drag)
    val context = LocalContext.current
    val copiedMsg = stringResource(R.string.copied_to_clipboard)

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

    val listState = rememberLazyListState()
    var draggingId by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var draggedItems by remember { mutableStateOf(cards) }

    LaunchedEffect(cards) {
        if (draggingId == null) {
            draggedItems = cards
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                EmptyState(
                    icon = Icons.Default.CreditCard,
                    title = stringResource(R.string.empty_bank_cards),
                    hint = stringResource(R.string.empty_bank_cards_hint),
                    actionLabel = stringResource(R.string.add_account),
                    onAction = { navController.navigate(Screen.CreateBankCard.route) }
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(draggedItems, key = { it.id }) { card ->

                    val cardColor = colors.firstOrNull { it.value.toLong() == card.color }
                        ?: colors.first()

                    val isDragging = draggingId == card.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                if (isDragging) translationY = dragOffsetY
                            }
                            .then(
                                if (isDragging) Modifier.shadow(16.dp, RoundedCornerShape(24.dp))
                                else Modifier
                            )
                    ) {
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
                            },
                            onMenuClick = { sheetCard = card },
                            onCopyCardNumber = {
                                copyToClipboard(context, "Card Number", card.cardNumber, copiedMsg)
                            }
                        )

                        if (toggleEditMode) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .pointerInput(card.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                draggingId = card.id
                                                dragOffsetY = 0f
                                            },
                                            onDragEnd = {
                                                viewModel.reorderCards(draggedItems.map { it.id })
                                                draggingId = null
                                                dragOffsetY = 0f
                                            },
                                            onDragCancel = {
                                                draggedItems = cards
                                                draggingId = null
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y

                                                val draggedKey = draggingId ?: return@detectDragGestures
                                                val layoutInfo = listState.layoutInfo
                                                val draggedInfo = layoutInfo.visibleItemsInfo
                                                    .firstOrNull { it.key == draggedKey } ?: return@detectDragGestures
                                                val draggedCenter =
                                                    draggedInfo.offset + draggedInfo.size / 2 + dragOffsetY

                                                val idx = draggedItems.indexOfFirst { it.id == draggedKey }
                                                if (idx == -1) return@detectDragGestures

                                                if (idx < draggedItems.size - 1) {
                                                    val nextInfo = layoutInfo.visibleItemsInfo
                                                        .firstOrNull { it.key == draggedItems[idx + 1].id }
                                                    if (nextInfo != null && draggedCenter > nextInfo.offset + nextInfo.size / 2) {
                                                        draggedItems = draggedItems.toMutableList().apply {
                                                            add(idx, removeAt(idx + 1))
                                                        }
                                                        dragOffsetY -= nextInfo.size
                                                        return@detectDragGestures
                                                    }
                                                }
                                                if (idx > 0) {
                                                    val prevInfo = layoutInfo.visibleItemsInfo
                                                        .firstOrNull { it.key == draggedItems[idx - 1].id }
                                                    if (prevInfo != null && draggedCenter < prevInfo.offset + prevInfo.size / 2) {
                                                        draggedItems = draggedItems.toMutableList().apply {
                                                            add(idx - 1, removeAt(idx))
                                                        }
                                                        dragOffsetY += prevInfo.size
                                                    }
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = dragDesc,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
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

    sheetCard?.let { card ->
        BankCardDetailsSheet(
            card = card,
            onDismiss = { sheetCard = null }
        )
    }
}

private fun copyToClipboard(
    context: Context,
    label: String,
    value: String,
    toastMessage: String
) {
    if (value.isBlank()) return
    val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}