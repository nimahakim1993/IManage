package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CarServiceIconType
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.EventType
import com.nima.app.imanage.domain.model.OfficeEvent
import com.nima.app.imanage.presentation.viewmodel.OfficeViewModel
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: OfficeViewModel = koinViewModel()
) {
    val officeTitle = stringResource(R.string.office_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = officeTitle,
                showBack = true
            )
        )
    }

    val allEvents by viewModel.allEvents.collectAsState()
    val today = ShamsiDate.today()

    var currentYear by remember { mutableStateOf(today.first) }
    var currentMonth by remember { mutableStateOf(today.second) }
    var selectedDay by remember { mutableStateOf(today.third) }

    val isTodaySelected =
        currentYear == today.first && currentMonth == today.second && selectedDay == today.third

    val daysInMonth = ShamsiDate.daysInMonth(currentYear, currentMonth)
    val daysWithEvents = remember(allEvents, currentYear, currentMonth) {
        viewModel.getDaysWithEvents(currentYear, currentMonth)
    }

    val selectedDateTimestamp = remember(currentYear, currentMonth, selectedDay) {
        ShamsiDate.toMillis(currentYear, currentMonth, selectedDay)
    }
    val eventsForSelectedDay = remember(allEvents, selectedDateTimestamp) {
        viewModel.getEventsForDate(selectedDateTimestamp)
    }

    val listState = rememberLazyListState()
    var onceCollapsed by remember { mutableStateOf(false) }
    val isCalendarCollapsed by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex == 0 -> {
                    onceCollapsed = false
                    false
                }

                listState.firstVisibleItemIndex > 1 -> {
                    onceCollapsed = true
                    true
                }

                listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset > 400 -> {
                    onceCollapsed = true
                    true
                }

                else -> onceCollapsed
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            AnimatedVisibility(
                visible = !isCalendarCollapsed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CalendarHeader(
                        year = currentYear,
                        month = currentMonth,
                        isTodaySelected = isTodaySelected,
                        onPreviousMonth = {
                            if (currentMonth > 1) currentMonth--
                            else {
                                currentMonth = 12; currentYear--
                            }
                        },
                        onNextMonth = {
                            if (currentMonth < 12) currentMonth++
                            else {
                                currentMonth = 1; currentYear++
                            }
                        },
                        onTodayClick = {
                            val t = ShamsiDate.today()
                            currentYear = t.first
                            currentMonth = t.second
                            selectedDay = t.third
                        }
                    )
                    CalendarGrid(
                        year = currentYear,
                        month = currentMonth,
                        daysInMonth = daysInMonth,
                        selectedDay = selectedDay,
                        daysWithEvents = daysWithEvents,
                        onDaySelected = { day -> selectedDay = day }
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isCalendarCollapsed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                CompactDateCard(
                    year = currentYear,
                    month = currentMonth,
                    day = selectedDay,
                    isTodaySelected = isTodaySelected,
                    onTodayClick = {
                        val t = ShamsiDate.today()
                        currentYear = t.first
                        currentMonth = t.second
                        selectedDay = t.third
                    }
                )
            }
        }

        item {
            Text(
                text = stringResource(
                    R.string.office_events_for_date,
                    ShamsiDate.format(selectedDateTimestamp)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (eventsForSelectedDay.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.office_no_events),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = vazirFontFamily
                    )
                }
            }
        } else {
            items(eventsForSelectedDay) { event ->
                EventCard(event = event)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CompactDateCard(
    year: Int,
    month: Int,
    day: Int,
    isTodaySelected: Boolean,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ShamsiDate.toPersianDigits(day.toString()),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(18f),
                        fontFamily = vazirFontFamily
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = ShamsiDate.getMonthName(month),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = ShamsiDate.toPersianDigits(year.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            if (!isTodaySelected) {
                TextButton(onClick = onTodayClick) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.office_today),
                        fontFamily = vazirFontFamily,
                        fontSize = scaledSp(12f),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    year: Int,
    month: Int,
    isTodaySelected: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.office_previous_month),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = ShamsiDate.getMonthName(month),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = ShamsiDate.toPersianDigits(year.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.office_next_month),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (!isTodaySelected) {
                TextButton(onClick = onTodayClick) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.office_today),
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    daysInMonth: Int,
    selectedDay: Int,
    daysWithEvents: Set<Int>,
    onDaySelected: (Int) -> Unit
) {
    val dayNames = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayNames.forEachIndexed { index, dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily,
                        color = if (index == 6) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val firstDayOfWeek = getFirstDayOfWeek(year, month)
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1

                        if (day in 1..daysInMonth) {
                            DayCell(
                                day = day,
                                isSelected = day == selectedDay,
                                hasEvent = day in daysWithEvents,
                                isFriday = col == 6,
                                onClick = { onDaySelected(day) }
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    day: Int,
    isSelected: Boolean,
    hasEvent: Boolean,
    isFriday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isFriday -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ShamsiDate.toPersianDigits(day.toString()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = vazirFontFamily,
                color = textColor
            )
            if (hasEvent && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun EventCard(event: OfficeEvent) {
    val displayTitle = when {
        event.type == EventType.CAR_SERVICE && event.serviceType != null -> {
            val iconType = CarServiceIconType.fromValue(event.serviceType)
            stringResource(iconType.labelRes())
        }

        else -> event.title
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(event.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    event.icon,
                    contentDescription = null,
                    tint = event.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (event.type) {
                        EventType.EXPENSE -> stringResource(R.string.office_type_expense)
                        EventType.INCOME -> stringResource(R.string.office_type_income)
                        EventType.LOAN -> {
                            if (event.isSettlementDue) {
                                if (event.loanType == LoanEntity.TYPE_DEBT) {
                                    stringResource(R.string.office_type_loan_due_debt)
                                } else {
                                    stringResource(R.string.office_type_loan_due_receivable)
                                }
                            } else if (event.loanType == LoanEntity.TYPE_DEBT) {
                                stringResource(R.string.office_type_debt)
                            } else {
                                stringResource(R.string.office_type_receivable)
                            }
                        }

                        EventType.TRIP -> stringResource(R.string.office_type_trip)
                        EventType.CAR_SERVICE -> stringResource(R.string.office_type_car_service)
                        EventType.INSTALLMENT -> stringResource(R.string.office_type_installment)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = vazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            event.amount?.let { amount ->
                Text(
                    text = NumberFormatUtils.format(amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily,
                    color = event.color
                )
            }
        }
    }
}

private fun CarServiceIconType.labelRes(): Int {
    return when (this) {
        CarServiceIconType.OIL_CHANGE -> R.string.car_type_oil_change
        CarServiceIconType.TIRE_CHANGE -> R.string.car_type_tire_change
        CarServiceIconType.BRAKE_PAD -> R.string.car_type_brake_pad
        CarServiceIconType.FILTER -> R.string.car_type_filter
        CarServiceIconType.BELT -> R.string.car_type_belt
        CarServiceIconType.LAMP -> R.string.car_type_lamp
        CarServiceIconType.BATTERY -> R.string.car_type_battery
        CarServiceIconType.ENGINE -> R.string.car_type_engine
        CarServiceIconType.GENERAL -> R.string.car_type_general
        CarServiceIconType.INSURANCE -> R.string.car_type_insurance
        CarServiceIconType.DEFAULT -> R.string.car_type_other
    }
}

private fun getFirstDayOfWeek(year: Int, month: Int): Int {
    val firstDayMillis = ShamsiDate.toMillis(year, month, 1)
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = firstDayMillis
    val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
    return when (dayOfWeek) {
        java.util.Calendar.SATURDAY -> 0
        java.util.Calendar.SUNDAY -> 1
        java.util.Calendar.MONDAY -> 2
        java.util.Calendar.TUESDAY -> 3
        java.util.Calendar.WEDNESDAY -> 4
        java.util.Calendar.THURSDAY -> 5
        java.util.Calendar.FRIDAY -> 6
        else -> 0
    }
}
