package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import com.nima.app.imanage.data.repository.ExpenseCategoryRepository
import com.nima.app.imanage.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val categoryRepository: ExpenseCategoryRepository
) : ViewModel() {

    val expenses = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val categories = categoryRepository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    fun addCategory(title: String, colorIndex: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            categoryRepository.insert(
                ExpenseCategoryEntity(
                    title = title.trim(),
                    colorIndex = colorIndex,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            if (expense.id > 0) repository.update(expense) else repository.insert(expense)
        }
    }

    fun removeExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun removeCategoryAndClearFromExpenses(category: ExpenseCategoryEntity) {
        viewModelScope.launch {
            repository.clearCategoryForExpenses(category.id)
            categoryRepository.delete(category)
        }
    }
}
