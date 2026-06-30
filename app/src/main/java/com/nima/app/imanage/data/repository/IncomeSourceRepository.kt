package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.IncomeSourceDao
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import kotlinx.coroutines.flow.Flow

class IncomeSourceRepository(
    private val dao: IncomeSourceDao
) {
    suspend fun insert(source: IncomeSourceEntity) = dao.insert(source)
    suspend fun update(source: IncomeSourceEntity) = dao.update(source)
    suspend fun delete(source: IncomeSourceEntity) = dao.delete(source)
    fun getAll(): Flow<List<IncomeSourceEntity>> = dao.getAll()
    suspend fun getById(id: Int): IncomeSourceEntity? = dao.getById(id)
}
