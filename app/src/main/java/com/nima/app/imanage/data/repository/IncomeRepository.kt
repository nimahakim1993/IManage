package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.IncomeDao
import com.nima.app.imanage.data.db.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

class IncomeRepository(
    private val dao: IncomeDao
) {
    suspend fun insert(income: IncomeEntity) = dao.insert(income)
    suspend fun update(income: IncomeEntity) = dao.update(income)
    suspend fun delete(income: IncomeEntity) = dao.delete(income)
    fun getAll(): Flow<List<IncomeEntity>> = dao.getAll()
    fun get(id: Int): Flow<IncomeEntity?> = dao.get(id)
    suspend fun clearSourceForIncomes(sourceId: Int) = dao.clearSourceForIncomes(sourceId)
}
