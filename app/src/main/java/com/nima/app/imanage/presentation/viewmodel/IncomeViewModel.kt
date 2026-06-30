package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.IncomeEntity
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import com.nima.app.imanage.data.repository.IncomeRepository
import com.nima.app.imanage.data.repository.IncomeSourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IncomeViewModel(
    private val repository: IncomeRepository,
    private val sourceRepository: IncomeSourceRepository
) : ViewModel() {

    val incomes = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val sources = sourceRepository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    fun addSource(title: String, colorIndex: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            sourceRepository.insert(
                IncomeSourceEntity(
                    title = title.trim(),
                    colorIndex = colorIndex,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateSource(source: IncomeSourceEntity) {
        viewModelScope.launch {
            sourceRepository.update(source)
        }
    }

    fun saveIncome(income: IncomeEntity) {
        viewModelScope.launch {
            if (income.id > 0) repository.update(income) else repository.insert(income)
        }
    }

    fun removeIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.delete(income)
        }
    }

    fun removeSourceAndClearFromIncomes(source: IncomeSourceEntity) {
        viewModelScope.launch {
            repository.clearSourceForIncomes(source.id)
            sourceRepository.delete(source)
        }
    }
}
