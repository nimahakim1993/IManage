package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.repository.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoanViewModel(
    private val repository: LoanRepository
) : ViewModel() {

    private val _selectedLoan = MutableStateFlow<LoanEntity?>(null)
    val selectedLoan: StateFlow<LoanEntity?> = _selectedLoan

    val loans = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun loadLoan(loanId: Int) {
        viewModelScope.launch {
            repository.get(loanId).collect {
                _selectedLoan.value = it
            }
        }
    }

    fun saveLoan(loanEntity: LoanEntity) {
        viewModelScope.launch {
            repository.insert(loanEntity)
        }
    }

    fun removeLoan(loanEntity: LoanEntity) {
        viewModelScope.launch {
            repository.delete(loanEntity)
        }
    }
}
