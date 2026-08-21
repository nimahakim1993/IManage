package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.CheckCounterpartyEntity
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.data.repository.CheckCounterpartyRepository
import com.nima.app.imanage.data.repository.CheckRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckViewModel(
    private val repository: CheckRepository,
    private val counterpartyRepository: CheckCounterpartyRepository
) : ViewModel() {
    val checks = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val counterparties = counterpartyRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(check: CheckEntity) {
        viewModelScope.launch {
            if (check.id > 0) repository.update(check) else repository.insert(check)
        }
    }

    fun delete(check: CheckEntity) {
        viewModelScope.launch { repository.delete(check) }
    }

    fun updateState(check: CheckEntity, state: String) {
        viewModelScope.launch { repository.update(check.copy(state = state)) }
    }

    fun addCounterparty(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            counterpartyRepository.insert(
                CheckCounterpartyEntity(
                    title = title.trim(),
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateCounterparty(counterparty: CheckCounterpartyEntity) {
        viewModelScope.launch { counterpartyRepository.update(counterparty) }
    }

    fun deleteCounterparty(counterparty: CheckCounterpartyEntity) {
        viewModelScope.launch { counterpartyRepository.delete(counterparty) }
    }
}
