package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.PasswordItemEntity
import com.nima.app.imanage.data.repository.PasswordItemRepository
import com.nima.app.imanage.util.CryptoUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PasswordItemInput(
    val id: Int,
    val title: String,
    val username: String,
    val password: String,
    val iconType: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val alreadyEncrypted: Boolean = false
)

class PasswordItemViewModel(private val repository: PasswordItemRepository) : ViewModel() {

    val items = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(input: PasswordItemInput) {
        viewModelScope.launch {
            val encryptedPassword = if (input.alreadyEncrypted)
                input.password
            else
                CryptoUtils.encrypt(input.password)
            val entity = PasswordItemEntity(
                id = input.id,
                title = input.title,
                username = input.username,
                encryptedPassword = encryptedPassword,
                iconType = input.iconType,
                createdAt = input.createdAt,
                updatedAt = input.updatedAt
            )
            if (entity.id > 0) repository.update(entity) else repository.insert(entity)
        }
    }

    fun remove(item: PasswordItemEntity) {
        viewModelScope.launch { repository.delete(item) }
    }

    fun decryptPassword(stored: String): String =
        CryptoUtils.decrypt(stored)
}