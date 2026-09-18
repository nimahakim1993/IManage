package com.nima.app.imanage.presentation.view.office

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.db.entity.OfficeNoteEntity
import com.nima.app.imanage.data.db.entity.OfficeReminderEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.domain.model.EventType
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.domain.model.OfficeEvent
import com.nima.app.imanage.presentation.view.YearPickerDialog
import com.nima.app.imanage.presentation.viewmodel.OfficeExtrasViewModel
import com.nima.app.imanage.presentation.viewmodel.OfficeViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.ShamsiMonthYearPicker
import com.nima.app.imanage.ui.theme.scaledSp
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.NumberFormatUtils
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

private val ReminderColor = Color(0xFFB3261E)
private val ReminderContainerColor = Color(0xFFF9DEDC)
private val OnReminderContainerColor = Color(0xFF410E0B)
private val EventCardColor = Color(0xFF2196F3).copy(alpha = 0.14f)
private const val DAY_MS = 24L * 60 * 60 * 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: OfficeViewModel = koinViewModel(),
    extrasViewModel: OfficeExtrasViewModel = koinViewModel()
) {
    val officeTitle = stringResource(R.string.office_title)
    val remindersDescription = stringResource(R.string.office_reminders)
    val notesDescription = stringResource(R.string.office_notes)
    var showReminderManagement by remember { mutableStateOf(false) }
    var showNoteManagement by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = officeTitle,
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.NotificationsActive,
                        contentDescription = remindersDescription,
                        onClick = { showReminderManagement = true }
                    ),
                    ToolbarAction(
                        icon = Icons.Default.NoteAdd,
                        contentDescription = notesDescription,
                        onClick = { showNoteManagement = true }
                    )
                )
            )
        )
    }

    val allEvents by viewModel.allEvents.collectAsState()
    val officeNotes by extrasViewModel.notes.collectAsState()
    val officeReminders by extrasViewModel.reminders.collectAsState()
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
    val notesForSelectedDay = remember(officeNotes, selectedDateTimestamp) {
        officeNotes.filter { it.date == selectedDateTimestamp }
    }
    val remindersForSelectedDay = remember(officeReminders, selectedDateTimestamp) {
        officeReminders.filter { it.date == selectedDateTimestamp }
    }
    val daysWithNotes = remember(officeNotes, currentYear, currentMonth) {
        officeNotes.mapNotNull { note ->
            val (year, month, day) = ShamsiDate.fromMillis(note.date)
            if (year == currentYear && month == currentMonth) day else null
        }.toSet()
    }
    val daysWithReminders = remember(officeReminders, currentYear, currentMonth) {
        officeReminders.mapNotNull { reminder ->
            val (year, month, day) = ShamsiDate.fromMillis(reminder.date)
            if (year == currentYear && month == currentMonth) day else null
        }.toSet()
    }

    var showReminderSheet by remember { mutableStateOf(false) }
    var showNoteSheet by remember { mutableStateOf(false) }
    var eventsExpanded by remember { mutableStateOf(false) }
    var notesExpanded by remember { mutableStateOf(false) }
    var remindersExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
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
                    daysWithNotes = daysWithNotes,
                    daysWithReminders = daysWithReminders,
                    onDaySelected = { day -> selectedDay = day }
                )
            }
        }

        item {
            OfficeActionButtons(
                onAddReminder = { showReminderSheet = true },
                onAddNote = { showNoteSheet = true }
            )
        }

        item {
            ExpandableSectionCard(
                title = stringResource(R.string.office_events),
                count = eventsForSelectedDay.size,
                expanded = eventsExpanded,
                onClick = { eventsExpanded = !eventsExpanded },
                emptyText = stringResource(R.string.office_no_events)
            ) {
                eventsForSelectedDay.forEach { event ->
                    EventCard(event = event)
                }
            }
        }

        item {
            ExpandableSectionCard(
                title = stringResource(R.string.office_notes),
                count = notesForSelectedDay.size,
                expanded = notesExpanded,
                onClick = { notesExpanded = !notesExpanded },
                emptyText = stringResource(R.string.office_no_notes)
            ) {
                notesForSelectedDay.forEach { note ->
                    OfficeNoteCard(note)
                }
            }
        }

        item {
            ExpandableSectionCard(
                title = stringResource(R.string.office_reminders),
                count = remindersForSelectedDay.size,
                expanded = remindersExpanded,
                onClick = { remindersExpanded = !remindersExpanded },
                emptyText = stringResource(R.string.office_no_reminders)
            ) {
                remindersForSelectedDay.forEach { reminder ->
                    OfficeReminderCard(reminder)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showReminderSheet) {
        OfficeReminderSheet(
            date = selectedDateTimestamp,
            allowDateSelection = true,
            onDismiss = { showReminderSheet = false },
            onSave = { _, date, reminderAt, text ->
                extrasViewModel.addReminder(date, reminderAt, text)
                showReminderSheet = false
            }
        )
    }

    if (showNoteSheet) {
        OfficeNoteSheet(
            initialDate = selectedDateTimestamp,
            allowDateSelection = true,
            onDismiss = { showNoteSheet = false },
            onSave = { _, date, text ->
                extrasViewModel.addNote(date, text)
                showNoteSheet = false
            }
        )
    }

    if (showReminderManagement) {
        OfficeReminderManagementSheet(
            reminders = officeReminders,
            viewModel = extrasViewModel,
            onDismiss = { showReminderManagement = false }
        )
    }

    if (showNoteManagement) {
        OfficeNoteManagementSheet(
            notes = officeNotes,
            viewModel = extrasViewModel,
            onDismiss = { showNoteManagement = false }
        )
    }
}

@Composable
private fun OfficeActionButtons(
    onAddReminder: () -> Unit,
    onAddNote: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onAddReminder,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.office_add_reminder), fontFamily = vazirFontFamily)
        }
        Button(
            onClick = onAddNote,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.office_add_note), fontFamily = vazirFontFamily)
        }
    }
}

@Composable
private fun ExpandableSectionCard(
    title: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    emptyText: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${NumberFormatUtils.toLocalizedDigits(count.toString())}))",
                        maxLines = 1,
                        softWrap = false,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
                if (count == 0) EmptySectionText(emptyText) else content()
            }
        }
    }
}

@Composable
private fun EmptySectionText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = vazirFontFamily
    )
}

@Composable
private fun OfficeNoteCard(note: OfficeNoteEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFC107))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = note.text,
                color = Color(0xFF5D4500),
                fontFamily = vazirFontFamily,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun OfficeReminderCard(reminder: OfficeReminderEntity) {
    val calendar = remember(reminder.reminderAt) {
        Calendar.getInstance().apply { timeInMillis = reminder.reminderAt }
    }
    val time = NumberFormatUtils.toLocalizedDigits(
        String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ReminderContainerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ReminderColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = reminder.text,
                    color = OnReminderContainerColor,
                    fontFamily = vazirFontFamily,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = time,
                    color = OnReminderContainerColor.copy(alpha = 0.7f),
                    fontFamily = vazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(12f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteSheet(
    initialDate: Long,
    initialNote: OfficeNoteEntity? = null,
    allowDateSelection: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (OfficeNoteEntity?, Long, String) -> Unit
) {
    var text by remember(initialNote?.id) { mutableStateOf(initialNote?.text.orEmpty()) }
    var selectedDate by remember(initialNote?.id, initialDate) {
        mutableStateOf(
            initialNote?.date ?: initialDate
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(
                    if (initialNote == null) R.string.office_add_note else R.string.edit_note_title
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
            if (allowDateSelection) {
                TextButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(ShamsiDate.formatLong(selectedDate), fontFamily = vazirFontFamily)
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.office_note_text)) },
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            Button(
                onClick = { onSave(initialNote, selectedDate, text.trim()) },
                enabled = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.save), fontFamily = vazirFontFamily)
            }
        }
    }

    if (showDatePicker) {
        ShamsiDatePicker(
            initialDate = selectedDate,
            title = stringResource(R.string.select_date),
            onDismiss = { showDatePicker = false },
            onConfirm = {
                selectedDate = it
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderSheet(
    date: Long,
    initialReminder: OfficeReminderEntity? = null,
    allowDateSelection: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (OfficeReminderEntity?, Long, Long, String) -> Unit
) {
    val now = remember(initialReminder?.id, date) {
        Calendar.getInstance().apply {
            timeInMillis = initialReminder?.reminderAt ?: System.currentTimeMillis()
        }
    }
    var selectedDate by remember(initialReminder?.id, date) {
        mutableStateOf(initialReminder?.date ?: date)
    }
    val timeState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )
    var text by remember(initialReminder?.id) { mutableStateOf(initialReminder?.text.orEmpty()) }
    var showPastError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val reminderAt = remember(selectedDate, timeState.hour, timeState.minute) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, timeState.hour)
            set(Calendar.MINUTE, timeState.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    if (initialReminder == null) R.string.office_add_reminder else R.string.edit_reminder
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
            if (allowDateSelection) {
                TextButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(ShamsiDate.formatLong(selectedDate), fontFamily = vazirFontFamily)
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TimePicker(state = timeState)
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.office_reminder_text)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            if (showPastError) {
                Text(
                    text = stringResource(R.string.office_reminder_past_error),
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = vazirFontFamily,
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = {
                    if (reminderAt <= System.currentTimeMillis()) {
                        showPastError = true
                    } else {
                        onSave(initialReminder, selectedDate, reminderAt, text.trim())
                    }
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.save), fontFamily = vazirFontFamily)
            }
        }
    }

    if (showDatePicker) {
        ShamsiDatePicker(
            initialDate = selectedDate,
            title = stringResource(R.string.select_date),
            onDismiss = { showDatePicker = false },
            onConfirm = {
                selectedDate = it
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderManagementSheet(
    reminders: List<OfficeReminderEntity>,
    viewModel: OfficeExtrasViewModel,
    onDismiss: () -> Unit
) {
    var filterMode by remember { mutableStateOf(FilterMode.CURRENT_YEAR) }
    var selectedMonthYear by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var customFrom by remember { mutableLongStateOf(ShamsiDate.todayMillis()) }
    var customTo by remember { mutableLongStateOf(ShamsiDate.todayMillis()) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showCustomFromPicker by remember { mutableStateOf(false) }
    var showCustomToPicker by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<OfficeReminderEntity?>(null) }
    var reminderToDelete by remember { mutableStateOf<OfficeReminderEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filteredReminders = remember(
        reminders, filterMode, selectedMonthYear, selectedYear, customFrom, customTo
    ) {
        reminders
            .asSequence()
            .filter {
                matchesOfficeFilter(
                    it.date, filterMode, selectedMonthYear, selectedYear, customFrom, customTo
                )
            }
            .sortedByDescending { it.createdAt }
            .toList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ManagementHeader(
                title = stringResource(R.string.office_reminders),
                icon = Icons.Default.AddAlert,
                onAdd = {
                    editingReminder = null
                    showEditor = true
                }
            )
            OfficeFilterChipRow(
                currentMode = filterMode,
                selectedLabel = when (filterMode) {
                    FilterMode.MONTHLY -> selectedMonthYear?.let { (month, year) ->
                        "${ShamsiDate.getMonthName(month)} ${ShamsiDate.toPersianDigits(year.toString())}"
                    }

                    FilterMode.YEARLY -> selectedYear?.let { ShamsiDate.toPersianDigits(it.toString()) }
                    FilterMode.CUSTOM -> stringResource(R.string.report_custom_range)
                    else -> null
                },
                onModeSelected = { mode ->
                    filterMode = mode
                    when (mode) {
                        FilterMode.MONTHLY -> showMonthYearPicker = true
                        FilterMode.YEARLY -> showYearPicker = true
                        else -> Unit
                    }
                }
            )
            if (filterMode == FilterMode.CUSTOM) {
                OfficeCustomRangeRow(
                    fromDate = customFrom,
                    toDate = customTo,
                    onFromClick = { showCustomFromPicker = true },
                    onToClick = { showCustomToPicker = true }
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredReminders.isEmpty()) {
                    item { EmptySectionText(stringResource(R.string.office_no_reminders_period)) }
                } else {
                    items(filteredReminders, key = { "management_reminder_${it.id}" }) { reminder ->
                        ManagementReminderRow(
                            reminder = reminder,
                            onEdit = {
                                editingReminder = reminder
                                showEditor = true
                            },
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                }
            }
        }
    }

    if (showMonthYearPicker) {
        ShamsiMonthYearPicker(
            initialMonth = selectedMonthYear?.first,
            initialYear = selectedMonthYear?.second,
            onConfirm = { month, year ->
                if (month != null) selectedMonthYear = month to year
                showMonthYearPicker = false
            },
            onDismiss = { showMonthYearPicker = false }
        )
    }

    if (showYearPicker) {
        YearPickerDialog(
            currentYear = selectedYear ?: ShamsiDate.today().first,
            onDismiss = { showYearPicker = false },
            onConfirm = {
                selectedYear = it
                showYearPicker = false
            }
        )
    }

    if (showCustomFromPicker) {
        ShamsiDatePicker(
            initialDate = customFrom,
            title = stringResource(R.string.report_custom_range),
            onDismiss = { showCustomFromPicker = false },
            onConfirm = {
                customFrom = it
                showCustomFromPicker = false
            }
        )
    }

    if (showCustomToPicker) {
        ShamsiDatePicker(
            initialDate = customTo,
            title = stringResource(R.string.report_custom_range),
            onDismiss = { showCustomToPicker = false },
            onConfirm = {
                customTo = it
                showCustomToPicker = false
            }
        )
    }

    if (showEditor) {
        OfficeReminderSheet(
            date = editingReminder?.date ?: ShamsiDate.todayMillis(),
            initialReminder = editingReminder,
            allowDateSelection = true,
            onDismiss = { showEditor = false },
            onSave = { existing, date, reminderAt, text ->
                if (existing == null) {
                    viewModel.addReminder(date, reminderAt, text)
                } else {
                    viewModel.updateReminder(existing, date, reminderAt, text)
                }
                showEditor = false
            }
        )
    }

    reminderToDelete?.let { reminder ->
        ActionDialog(
            onDismiss = { reminderToDelete = null },
            onPositiveClicked = {
                viewModel.removeReminder(reminder)
                reminderToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteManagementSheet(
    notes: List<OfficeNoteEntity>,
    viewModel: OfficeExtrasViewModel,
    onDismiss: () -> Unit
) {
    var filterMode by remember { mutableStateOf(FilterMode.CURRENT_YEAR) }
    var selectedMonthYear by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var customFrom by remember { mutableLongStateOf(ShamsiDate.todayMillis()) }
    var customTo by remember { mutableLongStateOf(ShamsiDate.todayMillis()) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showCustomFromPicker by remember { mutableStateOf(false) }
    var showCustomToPicker by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<OfficeNoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<OfficeNoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filteredNotes = remember(
        notes, filterMode, selectedMonthYear, selectedYear, customFrom, customTo
    ) {
        notes
            .asSequence()
            .filter {
                matchesOfficeFilter(
                    it.date, filterMode, selectedMonthYear, selectedYear, customFrom, customTo
                )
            }
            .sortedByDescending { it.createdAt }
            .toList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ManagementHeader(
                title = stringResource(R.string.office_notes),
                icon = Icons.Default.NoteAdd,
                onAdd = {
                    editingNote = null
                    showEditor = true
                }
            )
            OfficeFilterChipRow(
                currentMode = filterMode,
                selectedLabel = when (filterMode) {
                    FilterMode.MONTHLY -> selectedMonthYear?.let { (month, year) ->
                        "${ShamsiDate.getMonthName(month)} ${ShamsiDate.toPersianDigits(year.toString())}"
                    }

                    FilterMode.YEARLY -> selectedYear?.let { ShamsiDate.toPersianDigits(it.toString()) }
                    FilterMode.CUSTOM -> stringResource(R.string.report_custom_range)
                    else -> null
                },
                onModeSelected = { mode ->
                    filterMode = mode
                    when (mode) {
                        FilterMode.MONTHLY -> showMonthYearPicker = true
                        FilterMode.YEARLY -> showYearPicker = true
                        else -> Unit
                    }
                }
            )
            if (filterMode == FilterMode.CUSTOM) {
                OfficeCustomRangeRow(
                    fromDate = customFrom,
                    toDate = customTo,
                    onFromClick = { showCustomFromPicker = true },
                    onToClick = { showCustomToPicker = true }
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredNotes.isEmpty()) {
                    item { EmptySectionText(stringResource(R.string.office_no_notes_period)) }
                } else {
                    items(filteredNotes, key = { "management_note_${it.id}" }) { note ->
                        ManagementNoteRow(
                            note = note,
                            onEdit = {
                                editingNote = note
                                showEditor = true
                            },
                            onDelete = { noteToDelete = note }
                        )
                    }
                }
            }
        }
    }

    if (showMonthYearPicker) {
        ShamsiMonthYearPicker(
            initialMonth = selectedMonthYear?.first,
            initialYear = selectedMonthYear?.second,
            onConfirm = { month, year ->
                if (month != null) selectedMonthYear = month to year
                showMonthYearPicker = false
            },
            onDismiss = { showMonthYearPicker = false }
        )
    }

    if (showYearPicker) {
        YearPickerDialog(
            currentYear = selectedYear ?: ShamsiDate.today().first,
            onDismiss = { showYearPicker = false },
            onConfirm = {
                selectedYear = it
                showYearPicker = false
            }
        )
    }

    if (showCustomFromPicker) {
        ShamsiDatePicker(
            initialDate = customFrom,
            title = stringResource(R.string.report_custom_range),
            onDismiss = { showCustomFromPicker = false },
            onConfirm = {
                customFrom = it
                showCustomFromPicker = false
            }
        )
    }

    if (showCustomToPicker) {
        ShamsiDatePicker(
            initialDate = customTo,
            title = stringResource(R.string.report_custom_range),
            onDismiss = { showCustomToPicker = false },
            onConfirm = {
                customTo = it
                showCustomToPicker = false
            }
        )
    }

    if (showEditor) {
        OfficeNoteSheet(
            initialDate = editingNote?.date ?: ShamsiDate.todayMillis(),
            initialNote = editingNote,
            allowDateSelection = true,
            onDismiss = { showEditor = false },
            onSave = { existing, date, text ->
                if (existing == null) {
                    viewModel.addNote(date, text)
                } else {
                    viewModel.updateNote(existing, date, text)
                }
                showEditor = false
            }
        )
    }

    noteToDelete?.let { note ->
        ActionDialog(
            onDismiss = { noteToDelete = null },
            onPositiveClicked = {
                viewModel.removeNote(note)
                noteToDelete = null
            }
        )
    }
}

@Composable
private fun ManagementHeader(title: String, icon: ImageVector, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = vazirFontFamily
        )
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                icon,
                contentDescription = stringResource(R.string.add),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.add), fontFamily = vazirFontFamily)
        }
    }
}

@Composable
private fun OfficeFilterChipRow(
    currentMode: FilterMode,
    selectedLabel: String?,
    onModeSelected: (FilterMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            FilterMode.CURRENT_YEAR to stringResource(R.string.report_filter_current_year),
            FilterMode.MONTHLY to (selectedLabel.takeIf { currentMode == FilterMode.MONTHLY }
                ?: stringResource(R.string.report_filter_monthly)),
            FilterMode.YEARLY to (selectedLabel.takeIf { currentMode == FilterMode.YEARLY }
                ?: stringResource(R.string.report_filter_yearly)),
            FilterMode.CUSTOM to (selectedLabel.takeIf { currentMode == FilterMode.CUSTOM }
                ?: stringResource(R.string.report_filter_custom))
        ).forEach { (mode, label) ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        label,
                        fontFamily = vazirFontFamily,
                        fontSize = scaledSp(11f),
                        maxLines = 1
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun OfficeCustomRangeRow(
    fromDate: Long,
    toDate: Long,
    onFromClick: () -> Unit,
    onToClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(onClick = onFromClick, modifier = Modifier.weight(1f)) {
            Text(ShamsiDate.format(fromDate), fontFamily = vazirFontFamily, maxLines = 1)
        }
        TextButton(onClick = onToClick, modifier = Modifier.weight(1f)) {
            Text(ShamsiDate.format(toDate), fontFamily = vazirFontFamily, maxLines = 1)
        }
    }
}

private fun matchesOfficeFilter(
    date: Long,
    mode: FilterMode,
    monthYear: Pair<Int, Int>?,
    year: Int?,
    customFrom: Long,
    customTo: Long
): Boolean {
    val (dateYear, dateMonth) = ShamsiDate.fromMillis(date)
    return when (mode) {
        FilterMode.CURRENT_YEAR -> dateYear == ShamsiDate.today().first
        FilterMode.MONTHLY -> monthYear?.let { dateMonth == it.first && dateYear == it.second } == true
        FilterMode.YEARLY -> year?.let { dateYear == it } == true
        FilterMode.CUSTOM -> {
            val from = ShamsiDate.startOfDayMillis(customFrom)
            val to = ShamsiDate.startOfDayMillis(customTo) + DAY_MS - 1
            val rangeStart = minOf(from, to)
            val rangeEnd = maxOf(from, to)
            date in rangeStart..rangeEnd
        }
    }
}

@Composable
private fun ManagementNoteRow(
    note: OfficeNoteEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.text, color = Color(0xFF5D4500), fontFamily = vazirFontFamily)
                Text(
                    ShamsiDate.formatLong(note.date),
                    color = Color(0xFF8A6B00),
                    fontSize = scaledSp(12f),
                    fontFamily = vazirFontFamily
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ManagementReminderRow(
    reminder: OfficeReminderEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val calendar = remember(reminder.reminderAt) {
        Calendar.getInstance().apply { timeInMillis = reminder.reminderAt }
    }
    val time = NumberFormatUtils.toLocalizedDigits(
        String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ReminderContainerColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.text, color = OnReminderContainerColor, fontFamily = vazirFontFamily)
                Text(
                    "${ShamsiDate.formatLong(reminder.date)}  $time",
                    color = OnReminderContainerColor.copy(alpha = 0.7f),
                    fontSize = scaledSp(12f),
                    fontFamily = vazirFontFamily
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
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
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = ShamsiDate.getMonthName(month),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily,
                    color = Color.White
                )
                Text(
                    text = ShamsiDate.toPersianDigits(year.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = vazirFontFamily,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.office_next_month),
                    tint = Color.White
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
    daysWithNotes: Set<Int>,
    daysWithReminders: Set<Int>,
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
                                hasNote = day in daysWithNotes,
                                hasReminder = day in daysWithReminders,
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
    hasNote: Boolean,
    hasReminder: Boolean,
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
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (hasEvent) {
                    CalendarMarker(Color(0xFF2196F3))
                }
                if (hasNote) {
                    CalendarMarker(Color(0xFFFFC107))
                }
                if (hasReminder) {
                    CalendarMarker(ReminderColor)
                }
            }
        }
    }
}

@Composable
private fun CalendarMarker(color: Color) {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun EventCard(event: OfficeEvent) {
    val displayTitle = when {
        event.type == EventType.CAR_SERVICE && event.serviceTypeName != null -> {
            event.serviceTypeName
        }

        else -> event.title
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventCardColor)
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
                        EventType.CHECK -> stringResource(R.string.office_type_check)
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

private fun getFirstDayOfWeek(year: Int, month: Int): Int {
    val firstDayMillis = ShamsiDate.toMillis(year, month, 1)
    val cal = Calendar.getInstance()
    cal.timeInMillis = firstDayMillis
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    return when (dayOfWeek) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }
}
