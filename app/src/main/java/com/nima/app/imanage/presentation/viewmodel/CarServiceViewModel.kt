package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import com.nima.app.imanage.data.repository.CarServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CarServiceViewModel(
    private val repository: CarServiceRepository
) : ViewModel() {

    private val _selectedService = MutableStateFlow<CarServiceEntity?>(null)
    val selectedService: StateFlow<CarServiceEntity?> = _selectedService

    val services = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun loadService(serviceId: Int) {
        viewModelScope.launch {
            repository.get(serviceId).collect {
                _selectedService.value = it
            }
        }
    }

    fun saveService(carServiceEntity: CarServiceEntity) {
        viewModelScope.launch {
            repository.insert(carServiceEntity)
        }
    }

    fun removeService(carServiceEntity: CarServiceEntity) {
        viewModelScope.launch {
            repository.delete(carServiceEntity)
        }
    }
}
