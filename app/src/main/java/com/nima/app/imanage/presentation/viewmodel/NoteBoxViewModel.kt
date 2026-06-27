package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import com.nima.app.imanage.data.repository.NoteBoxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteBoxViewModel(
    private val repository: NoteBoxRepository
) : ViewModel() {

    private val _selectedBox = MutableStateFlow<NoteBoxEntity?>(null)
    val selectedBox: StateFlow<NoteBoxEntity?> = _selectedBox

    val boxes = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun loadBox(boxId: Int) {
        viewModelScope.launch {
            repository.get(boxId).collect { _selectedBox.value = it }
        }
    }

    suspend fun saveBox(box: NoteBoxEntity) {
        if (box.id > 0) repository.update(box) else repository.insert(box)
    }

    fun saveBoxAsync(box: NoteBoxEntity) {
        viewModelScope.launch {
            if (box.id > 0) repository.update(box) else repository.insert(box)
        }
    }

    fun removeBox(box: NoteBoxEntity) {
        viewModelScope.launch {
            repository.delete(box)
        }
    }
}
