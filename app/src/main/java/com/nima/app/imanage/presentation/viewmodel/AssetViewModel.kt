package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.AssetEntity
import com.nima.app.imanage.data.repository.AssetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssetViewModel(private val repository: AssetRepository) : ViewModel() {

    val assets = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveAsset(asset: AssetEntity) {
        viewModelScope.launch {
            if (asset.id > 0) repository.update(asset) else repository.insert(asset)
        }
    }

    fun removeAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.delete(asset)
        }
    }
}
