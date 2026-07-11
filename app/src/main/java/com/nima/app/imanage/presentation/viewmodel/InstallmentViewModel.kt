package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.repository.InstallmentItemRepository
import com.nima.app.imanage.data.repository.InstallmentRepository
import com.nima.app.imanage.util.ShamsiDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InstallmentViewModel(
    private val repository: InstallmentRepository,
    private val itemRepository: InstallmentItemRepository
) : ViewModel() {

    private val _selectedInstallment = MutableStateFlow<InstallmentEntity?>(null)
    val selectedInstallment: StateFlow<InstallmentEntity?> = _selectedInstallment

    val installments = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allItems = itemRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedItems = MutableStateFlow<List<InstallmentItemEntity>>(emptyList())
    val selectedItems: StateFlow<List<InstallmentItemEntity>> = _selectedItems

    fun loadInstallment(installmentId: Int) {
        viewModelScope.launch {
            repository.get(installmentId).first()?.let { installment ->
                _selectedInstallment.value = installment
                loadItems(installmentId)
            }
        }
    }

    fun loadItems(installmentId: Int) {
        viewModelScope.launch {
            itemRepository.getByInstallmentId(installmentId).collect { items ->
                _selectedItems.value = items
            }
        }
    }

    fun saveInstallment(installment: InstallmentEntity) {
        viewModelScope.launch {
            if (installment.id > 0) {
                repository.update(installment)
                itemRepository.deleteByInstallmentId(installment.id)
                generateInstallmentItems(installment)
            } else {
                val insertedId = repository.insert(installment.copy(createdAt = System.currentTimeMillis()))
                val inserted = installment.copy(id = insertedId.toInt())
                generateInstallmentItems(inserted)
            }
        }
    }

    private suspend fun generateInstallmentItems(installment: InstallmentEntity) {
        val perItemAmount = if (installment.numberOfInstallments > 0) {
            installment.amount / installment.numberOfInstallments
        } else 0L

        var currentMillis = installment.startDate
        for (i in 0 until installment.numberOfInstallments) {
            itemRepository.insert(
                InstallmentItemEntity(
                    installmentId = installment.id,
                    dueDate = currentMillis,
                    amount = perItemAmount
                )
            )
            val periodMs = when (installment.periodType) {
                InstallmentEntity.PERIOD_MONTHLY -> {
                    val (jy, jm, _) = ShamsiDate.fromMillis(currentMillis)
                    val daysInMonth = ShamsiDate.daysInMonth(jy, jm)
                    daysInMonth * 24L * 60 * 60 * 1000
                }
                InstallmentEntity.PERIOD_WEEKLY -> 7L * 24 * 60 * 60 * 1000
                else -> installment.periodDays.toLong() * 24L * 60 * 60 * 1000
            }
            currentMillis += periodMs
        }
    }

    fun removeInstallment(installment: InstallmentEntity) {
        viewModelScope.launch {
            repository.delete(installment)
        }
    }

    fun toggleItemSettled(item: InstallmentItemEntity) {
        viewModelScope.launch {
            itemRepository.update(
                item.copy(
                    settled = !item.settled,
                    settledAt = if (!item.settled) System.currentTimeMillis() else 0
                )
            )
        }
    }
}
