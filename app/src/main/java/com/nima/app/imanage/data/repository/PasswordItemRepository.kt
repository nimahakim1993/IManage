package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.PasswordItemDao
import com.nima.app.imanage.data.db.entity.PasswordItemEntity
import kotlinx.coroutines.flow.Flow

class PasswordItemRepository(private val dao: PasswordItemDao) {
    suspend fun insert(item: PasswordItemEntity) = dao.insert(item)
    suspend fun update(item: PasswordItemEntity) = dao.update(item)
    suspend fun delete(item: PasswordItemEntity) = dao.delete(item)
    fun getAll(): Flow<List<PasswordItemEntity>> = dao.getAll()
    fun get(id: Int): Flow<PasswordItemEntity?> = dao.get(id)
}