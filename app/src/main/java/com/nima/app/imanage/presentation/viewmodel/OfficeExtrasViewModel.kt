package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.OfficeNoteEntity
import com.nima.app.imanage.data.db.entity.OfficeReminderEntity
import com.nima.app.imanage.data.repository.OfficeNoteRepository
import com.nima.app.imanage.data.repository.OfficeReminderRepository
import com.nima.app.imanage.util.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OfficeExtrasViewModel(
    private val noteRepository: OfficeNoteRepository,
    private val reminderRepository: OfficeReminderRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    val notes: StateFlow<List<OfficeNoteEntity>> = noteRepository.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val reminders: StateFlow<List<OfficeReminderEntity>> = reminderRepository.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun addNote(date: Long, text: String) {
        viewModelScope.launch {
            noteRepository.insert(OfficeNoteEntity(date = date, text = text))
        }
    }

    fun updateNote(note: OfficeNoteEntity, date: Long, text: String) {
        viewModelScope.launch {
            noteRepository.update(note.copy(date = date, text = text))
        }
    }

    fun removeNote(note: OfficeNoteEntity) {
        viewModelScope.launch { noteRepository.delete(note) }
    }

    fun addReminder(date: Long, reminderAt: Long, text: String) {
        viewModelScope.launch {
            val reminder = OfficeReminderEntity(
                date = date,
                reminderAt = reminderAt,
                text = text
            )
            val id = reminderRepository.insert(reminder).toInt()
            reminderScheduler.schedule(reminder.copy(id = id))
        }
    }

    fun updateReminder(reminder: OfficeReminderEntity, date: Long, reminderAt: Long, text: String) {
        viewModelScope.launch {
            reminderScheduler.cancel(reminder.id)
            val updated = reminder.copy(date = date, reminderAt = reminderAt, text = text)
            reminderRepository.update(updated)
            reminderScheduler.schedule(updated)
        }
    }

    fun removeReminder(reminder: OfficeReminderEntity) {
        viewModelScope.launch {
            reminderScheduler.cancel(reminder.id)
            reminderRepository.delete(reminder)
        }
    }
}
