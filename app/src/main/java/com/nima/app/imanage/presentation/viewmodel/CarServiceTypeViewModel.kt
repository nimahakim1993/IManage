package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.CarServiceTypeEntity
import com.nima.app.imanage.data.repository.CarServiceTypeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CarServiceTypeViewModel(
    private val repository: CarServiceTypeRepository
) : ViewModel() {

    val types = repository.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun addType(title: String, colorIndex: Int, iconIndex: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                CarServiceTypeEntity(
                    title = title.trim(),
                    colorIndex = colorIndex,
                    iconIndex = iconIndex,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateType(type: CarServiceTypeEntity) {
        viewModelScope.launch {
            repository.update(type)
        }
    }

    fun removeType(type: CarServiceTypeEntity) {
        viewModelScope.launch {
            repository.delete(type)
        }
    }
}
