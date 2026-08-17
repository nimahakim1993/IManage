package com.nima.app.imanage.presentation.view.office

import androidx.compose.runtime.Composable
import com.nima.app.imanage.data.db.entity.OfficeNoteEntity
import com.nima.app.imanage.data.db.entity.OfficeReminderEntity
import com.nima.app.imanage.presentation.viewmodel.OfficeExtrasViewModel

@Composable
internal fun OfficeNoteSheet(
    initialDate: Long,
    initialNote: OfficeNoteEntity? = null,
    allowDateSelection: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (OfficeNoteEntity?, Long, String) -> Unit
) {
    NoteSheet(
        initialDate = initialDate,
        initialNote = initialNote,
        allowDateSelection = allowDateSelection,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
internal fun OfficeReminderSheet(
    date: Long,
    initialReminder: OfficeReminderEntity? = null,
    allowDateSelection: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (OfficeReminderEntity?, Long, Long, String) -> Unit
) {
    ReminderSheet(
        date = date,
        initialReminder = initialReminder,
        allowDateSelection = allowDateSelection,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
internal fun OfficeReminderManagementSheet(
    reminders: List<OfficeReminderEntity>,
    viewModel: OfficeExtrasViewModel,
    onDismiss: () -> Unit
) {
    ReminderManagementSheet(reminders, viewModel, onDismiss)
}

@Composable
internal fun OfficeNoteManagementSheet(
    notes: List<OfficeNoteEntity>,
    viewModel: OfficeExtrasViewModel,
    onDismiss: () -> Unit
) {
    NoteManagementSheet(notes, viewModel, onDismiss)
}
