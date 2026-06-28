package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import com.nima.app.imanage.data.repository.ExpenseCategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseCategoryViewModel(
    private val repository: ExpenseCategoryRepository
) : ViewModel() {

    val categories = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun addCategory(title: String, colorIndex: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                ExpenseCategoryEntity(
                    title = title.trim(),
                    colorIndex = colorIndex,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateCategory(category: ExpenseCategoryEntity) {
        viewModelScope.launch {
            repository.update(category)
        }
    }

    fun removeCategory(category: ExpenseCategoryEntity) {
        viewModelScope.launch {
            repository.delete(category)
        }
    }
}
