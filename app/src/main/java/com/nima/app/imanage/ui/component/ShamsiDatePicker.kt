package com.nima.app.imanage.ui.component

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.nima.app.imanage.R
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.ShamsiDate

@Composable
fun ShamsiDatePicker(
    initialDate: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.select_date)
) {
    val initialShamsi = remember(initialDate) { ShamsiDate.fromMillis(initialDate) }
    var selectedYear by remember { mutableIntStateOf(initialShamsi.first) }
    var selectedMonth by remember { mutableIntStateOf(initialShamsi.second) }
    var selectedDay by remember { mutableIntStateOf(initialShamsi.third) }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        ShamsiDate.daysInMonth(selectedYear, selectedMonth)
    }

    LaunchedEffect(daysInMonth) {
        if (selectedDay > daysInMonth) selectedDay = daysInMonth
    }

    val currentShamsi = ShamsiDate.today()
    val yearRange = (currentShamsi.first - 50)..(currentShamsi.first + 10)

    val monthName = ShamsiDate.getMonthName(selectedMonth)
    val dayText = ShamsiDate.toPersianDigits(selectedDay.toString())
    val yearText = ShamsiDate.toPersianDigits(selectedYear.toString())
    val formattedDate = "$dayText $monthName $yearText"
    val dayName = remember(selectedYear, selectedMonth, selectedDay) {
        val millis = ShamsiDate.toMillis(selectedYear, selectedMonth, selectedDay)
        ShamsiDate.getDayName(millis)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = vazirFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = vazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.size(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    NumberWheel(
                        items = (1..daysInMonth).toList(),
                        selected = selectedDay,
                        onSelectedChange = { selectedDay = it },
                        label = stringResource(R.string.shamsi_day),
                        format = { ShamsiDate.toPersianDigits(it.toString()) },
                        modifier = Modifier.weight(1f)
                    )
                    NumberWheel(
                        items = (1..12).toList(),
                        selected = selectedMonth,
                        onSelectedChange = { selectedMonth = it },
                        label = stringResource(R.string.shamsi_month),
                        format = { ShamsiDate.getMonthName(it) },
                        modifier = Modifier.weight(1.4f)
                    )
                    NumberWheel(
                        items = yearRange.toList(),
                        selected = selectedYear,
                        onSelectedChange = { selectedYear = it },
                        label = stringResource(R.string.shamsi_year),
                        format = { ShamsiDate.toPersianDigits(it.toString()) },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                TextButton(onClick = {
                    selectedYear = currentShamsi.first
                    selectedMonth = currentShamsi.second
                    selectedDay = currentShamsi.third
                }) {
                    Text(
                        text = stringResource(R.string.today),
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val millis = ShamsiDate.toMillis(selectedYear, selectedMonth, selectedDay)
                onConfirm(millis)
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.confirm),
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontFamily = vazirFontFamily
                )
            }
        }
    )
}

@Composable
private fun NumberWheel(
    items: List<Int>,
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    label: String,
    format: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val itemHeight = 44.dp
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = items.indexOf(selected).coerceAtLeast(0)
    )

    LaunchedEffect(selected) {
        val index = items.indexOf(selected)
        if (index >= 0 && !listState.isScrollInProgress) {
            val current = listState.firstVisibleItemIndex
            if (kotlin.math.abs(current - index) > 1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .height(itemHeight * 3)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = itemHeight),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    val isSelected = item == selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .clickable {
                                onSelectedChange(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = format(item),
                            style = if (isSelected) MaterialTheme.typography.titleLarge
                            else MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            fontFamily = vazirFontFamily,
                            fontSize = if (isSelected) 20.sp else 16.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }
}
