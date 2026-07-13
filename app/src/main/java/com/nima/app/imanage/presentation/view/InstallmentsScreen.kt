package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.InstallmentViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.LocalAppColors
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import com.nima.app.imanage.ui.theme.NoteBoxBlue
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

private const val DAY_MS = 24L * 60 * 60 * 1000

@Composable
fun InstallmentsScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    viewModel: InstallmentViewModel = koinViewModel()
) {
    val installments by viewModel.installments.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val itemsMap = remember(allItems) { allItems.groupBy { it.installmentId } }
    val title = stringResource(R.string.installments_title)
    val addDesc = stringResource(R.string.add)
    val editDesc = stringResource(R.string.edit)

    var toggleEditMode by rememberSaveable { mutableStateOf(false) }
    var removingInstallment by remember { mutableStateOf<InstallmentEntity?>(null) }

    LaunchedEffect(installments.isEmpty()) {
        if (installments.isEmpty()) toggleEditMode = false
    }

    LaunchedEffect(toggleEditMode, installments.isEmpty()) {
        val actions = mutableListOf(
            ToolbarAction(
                icon = Icons.Default.Add,
                contentDescription = addDesc,
                onClick = { navController.navigate(Screen.CreateInstallment.createRoute()) }
            )
        )
        if (installments.isNotEmpty()) {
            actions.add(
                ToolbarAction(
                    icon = if (toggleEditMode) Icons.Default.EditOff else Icons.Default.Edit,
                    contentDescription = editDesc,
                    onClick = { toggleEditMode = !toggleEditMode }
                )
            )
        }
        setToolbar(
            ToolbarConfig(
                title = title,
                showBack = true,
                actions = actions
            )
        )
    }

    if (installments.isEmpty()) {
        EmptyState(
            icon = Icons.Default.CalendarMonth,
            title = stringResource(R.string.empty_installments),
            hint = stringResource(R.string.empty_installments_hint),
            actionLabel = stringResource(R.string.add),
            onAction = { navController.navigate(Screen.CreateInstallment.createRoute()) }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(installments, key = { it.id }) { installment ->
                val items = itemsMap[installment.id] ?: emptyList()
                InstallmentCard(
                    installment = installment,
                    items = items,
                    editMode = toggleEditMode,
                    onClick = { navController.navigate(Screen.InstallmentDetail.createRoute(installment.id)) },
                    onEdit = { navController.navigate(Screen.CreateInstallment.createRoute(installment.id)) },
                    onDelete = { removingInstallment = installment }
                )
            }
        }
    }

    removingInstallment?.let { installment ->
        ActionDialog(
            onDismiss = { removingInstallment = null },
            onPositiveClicked = {
                viewModel.removeInstallment(installment)
                removingInstallment = null
            }
        )
    }
}

@Composable
fun InstallmentCard(
    installment: InstallmentEntity,
    items: List<InstallmentItemEntity>,
    editMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val overdueColor = LocalAppColors.current.debt
    val settledColor = if (isDark) Color(0xFF1565C0) else Color(0xFF1976D2)
    val onTrackColor = if (isDark) Color(0xFF1565C0) else Color(0xFF1976D2)
    val warningColor = if (isDark) Color(0xFFE65100) else Color(0xFFFB8C00)

    val today = ShamsiDate.todayMillis()

    val startDate = installment.startDate
    val endDate = if (items.isNotEmpty()) {
        items.last().dueDate
    } else {
        installment.startDate + installment.numberOfInstallments.toLong() * installment.periodDays * DAY_MS
    }

    val totalDays = ((endDate - startDate) / DAY_MS).toInt().coerceAtLeast(1)
    val daysPassed = ((today - startDate) / DAY_MS).toInt().coerceIn(0, totalDays)
    val daysRemaining = (totalDays - daysPassed).coerceAtLeast(0)
    val progress = (daysPassed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    val isOverdue = today > endDate
    val settledCount = items.count { it.settled }
    val allSettled = items.isNotEmpty() && items.all { it.settled }
    val isCloseToDeadline = !isOverdue && !allSettled && daysRemaining < (totalDays * 0.2).toInt()

    val progressColor by animateColorAsState(
        targetValue = when {
            allSettled -> settledColor
            isOverdue -> overdueColor
            isCloseToDeadline -> warningColor
            else -> onTrackColor
        },
        animationSpec = tween(700)
    )

    val palette = NoteBoxBlue

    val cardBaseColor by animateColorAsState(
        targetValue = if (allSettled) settledColor else palette.primary,
        animationSpec = tween(700)
    )

    val gradient = Brush.linearGradient(
        colors = listOf(cardBaseColor, cardBaseColor.copy(alpha = 0.78f)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val accentColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (allSettled) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                        Text(
                            text = installment.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            fontFamily = vazirFontFamily,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Text(
                        text = NumberFormatUtils.toLocalizedDigits("$settledCount/${installment.numberOfInstallments}"),
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = vazirFontFamily
                    )
                }

                if (installment.amount > 0) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = NumberFormatUtils.format(installment.amount),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        fontFamily = vazirFontFamily
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                }

                if (installment.description.isNotBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = installment.description,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                            fontFamily = vazirFontFamily,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (allSettled) {
                                stringResource(R.string.all_settled)
                            } else if (isOverdue) {
                                stringResource(R.string.overdue_by_days, daysPassed - totalDays)
                            } else {
                                stringResource(R.string.days_passed, daysPassed)
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (allSettled) {
                                stringResource(R.string.completed)
                            } else if (isOverdue) {
                                stringResource(R.string.deadline_passed)
                            } else {
                                stringResource(R.string.days_remaining, daysRemaining)
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        LinearProgressIndicator(
                            progress = { if (allSettled) 1f else if (isOverdue) 1f else progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = ShamsiDate.formatLong(installment.startDate),
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontFamily = vazirFontFamily
                        )
                    }
                    Text(
                        text = periodTypeShortLabel(installment),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = vazirFontFamily
                    )
                }

                AnimatedVisibility(visible = editMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = Color.White)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun periodTypeShortLabel(installment: InstallmentEntity): String {
    return when (installment.periodType) {
        InstallmentEntity.PERIOD_MONTHLY -> stringResource(R.string.period_monthly)
        InstallmentEntity.PERIOD_WEEKLY -> stringResource(R.string.period_weekly)
        else -> stringResource(R.string.every_days, installment.periodDays)
    }
}
