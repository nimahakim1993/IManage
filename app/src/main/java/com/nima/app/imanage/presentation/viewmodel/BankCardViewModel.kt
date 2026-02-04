package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.repository.BankCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BankCardViewModel(
    private val repository: BankCardRepository
) : ViewModel() {

    private val _selectedCard = MutableStateFlow<BankCardEntity?>(null)
    val selectedCard: StateFlow<BankCardEntity?> = _selectedCard

    val cards = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun loadCard(cardId: Int) {
        viewModelScope.launch {
            repository.get(cardId).collect {
                _selectedCard.value = it
            }
        }
    }

    fun saveCard(card: BankCardEntity) {
        viewModelScope.launch {
            repository.insert(card)
        }
    }

    fun removeCard(card: BankCardEntity) {
        viewModelScope.launch {
            repository.delete(card)
        }
    }
}