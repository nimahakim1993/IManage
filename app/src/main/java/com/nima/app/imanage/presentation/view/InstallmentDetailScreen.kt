package com.nima.app.imanage.presentation.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.InstallmentViewModel
import com.nima.app.imanage.ui.theme.DebtDark
import com.nima.app.imanage.ui.theme.DebtLight
import com.nima.app.imanage.ui.theme.NoteBoxBlue
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

private const val DAY_MS = 24L * 60 * 60 * 1000

@Composable
fun InstallmentDetailScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    installmentId: Int,
    viewModel: InstallmentViewModel = koinViewModel()
) {
    LaunchedEffect(installmentId) {
        viewModel.loadInstallment(installmentId)
    }

    val installment by viewModel.selectedInstallment.collectAsState()
    val items by viewModel.selectedItems.collectAsState()
    val defaultTitle = stringResource(R.string.installment_detail)

    LaunchedEffect(installment) {
        setToolbar(
            ToolbarConfig(
                title = installment?.title ?: defaultTitle,
                showBack = true
            )
        )
    }

    installment?.let { inst ->
        val settledCount = items.count { it.settled }
        val allSettled = items.isNotEmpty() && items.all { it.settled }

        val isDark = isSystemInDarkTheme()
        val overdueColor = if (isDark) DebtDark else DebtLight
        val settledColor = if (isDark) Color(0xFF1565C0) else Color(0xFF1976D2)

        val today = ShamsiDate.todayMillis()

        val startDate = inst.startDate
        val endDate = if (items.isNotEmpty()) items.last().dueDate else (inst.startDate + inst.numberOfInstallments.toLong() * inst.periodDays * DAY_MS)
        val totalDays = ((endDate - startDate) / DAY_MS).toInt().coerceAtLeast(1)
        val daysPassed = ((today - startDate) / DAY_MS).toInt().coerceIn(0, totalDays)
        val progress = (daysPassed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
        val isOverdue = today > endDate

        val progressColorByState = when {
            allSettled -> settledColor
            isOverdue -> overdueColor
            else -> NoteBoxBlue.primary
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SummaryCard(
                    installment = inst,
                    settledCount = settledCount,
                    progress = if (allSettled || isOverdue) 1f else progress,
                    progressColor = progressColorByState,
                    allSettled = allSettled,
                    isOverdue = isOverdue,
                    daysPassed = daysPassed,
                    totalDays = totalDays
                )
            }

            itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                val previousDueDate = if (index > 0) items[index - 1].dueDate else inst.startDate
                val periodMs = item.dueDate - previousDueDate
                val elapsedMs = (today - previousDueDate).coerceIn(0L, periodMs.coerceAtLeast(1L))
                val itemProgress = (elapsedMs.toFloat() / periodMs.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                val itemIsPastDue = today > item.dueDate && !item.settled

                ItemCard(
                    item = item,
                    progress = if (item.settled) 1f else if (itemIsPastDue) 1f else itemProgress,
                    isPastDue = itemIsPastDue,
                    isSettled = item.settled,
                    onToggleSettled = { viewModel.toggleItemSettled(item) }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    installment: InstallmentEntity,
    settledCount: Int,
    progress: Float,
    progressColor: Color,
    allSettled: Boolean,
    isOverdue: Boolean,
    daysPassed: Int,
    totalDays: Int
) {
    val animatedProgressColor by animateColorAsState(
        targetValue = progressColor,
        animationSpec = tween(700)
    )

    val gradient = Brush.linearGradient(
        colors = listOf(NoteBoxBlue.primary, NoteBoxBlue.secondary.copy(alpha = 0.78f)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = installment.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        fontFamily = vazirFontFamily
                    )
                    Text(
                        text = "$settledCount/${installment.numberOfInstallments}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = vazirFontFamily
                    )
                }

                if (installment.amount > 0) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = NumberFormatUtils.format(installment.amount),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        fontFamily = vazirFontFamily
                    )
                }

                if (installment.description.isNotBlank()) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = installment.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontFamily = vazirFontFamily
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Text(
                    text = periodTypeLabel(installment),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = vazirFontFamily
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = ShamsiDate.formatLong(installment.startDate),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = vazirFontFamily
                )

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when {
                                allSettled -> stringResource(R.string.all_settled)
                                isOverdue -> stringResource(R.string.deadline_passed)
                                else -> stringResource(R.string.days_passed, daysPassed)
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when {
                                allSettled -> stringResource(R.string.completed)
                                isOverdue -> stringResource(R.string.overdue)
                                else -> stringResource(R.string.of_total_days, totalDays)
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = animatedProgressColor,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun periodTypeLabel(installment: InstallmentEntity): String {
    val typeStr = when (installment.periodType) {
        InstallmentEntity.PERIOD_MONTHLY -> stringResource(R.string.period_monthly)
        InstallmentEntity.PERIOD_WEEKLY -> stringResource(R.string.period_weekly)
        else -> stringResource(R.string.period_custom)
    }
    val periodLabel = stringResource(R.string.period_label)
    return if (installment.periodType == InstallmentEntity.PERIOD_CUSTOM) {
        "$periodLabel: ${installment.periodDays} ${stringResource(R.string.days)}"
    } else {
        "$periodLabel: $typeStr"
    }
}

@Composable
private fun ItemCard(
    item: InstallmentItemEntity,
    progress: Float,
    isPastDue: Boolean,
    isSettled: Boolean,
    onToggleSettled: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val today = ShamsiDate.todayMillis()

    val baseColor by animateColorAsState(
        targetValue = when {
            isSettled -> if (isDark) Color(0xFF1565C0) else Color(0xFF1976D2)
            isPastDue -> if (isDark) DebtDark else DebtLight
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(500)
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isSettled) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val circleProgressColor = when {
        isSettled -> Color.White.copy(alpha = 0.6f)
        isPastDue -> Color(0x66FFCDD2)
        else -> Color(0x66FFFFFF)
    }

    val gradient = Brush.linearGradient(
        colors = listOf(baseColor, baseColor.copy(alpha = 0.78f)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CircleProgress(
                        progress = progress,
                        color = circleProgressColor,
                        trackColor = Color.White.copy(alpha = 0.12f),
                        size = 48.dp,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = ShamsiDate.formatLong(item.dueDate),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                fontFamily = vazirFontFamily
                            )
                        }
                        if (item.amount > 0) {
                            Spacer(modifier = Modifier.size(3.dp))
                            Text(
                                text = NumberFormatUtils.format(item.amount),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontFamily = vazirFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onToggleSettled)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .scale(checkScale)
                    )
                    Text(
                        text = stringResource(
                            if (isSettled) R.string.settled_on
                            else if (isPastDue) R.string.overdue_settle
                            else R.string.mark_settled
                        ),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleProgress(
    progress: Float,
    color: Color,
    trackColor: Color,
    size: Dp,
    strokeWidth: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val radius = (size.toPx() - stroke) / 2f
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${(progress * 100).toInt()}%",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}
