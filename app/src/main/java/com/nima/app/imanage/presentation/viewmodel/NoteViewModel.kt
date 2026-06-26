package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.NoteEntity
import com.nima.app.imanage.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _boxId = MutableStateFlow<Int?>(null)
    val boxId: StateFlow<Int?> = _boxId

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote

    val notes = _boxId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getByBox(id)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun setBoxId(id: Int) {
        _boxId.value = id
    }

    fun loadNote(noteId: Int) {
        viewModelScope.launch {
            repository.get(noteId).collect { _selectedNote.value = it }
        }
    }

    suspend fun saveNote(note: NoteEntity) {
        repository.insert(note)
    }

    fun removeNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}
