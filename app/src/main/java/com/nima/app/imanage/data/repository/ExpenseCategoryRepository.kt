package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.ExpenseCategoryDao
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import kotlinx.coroutines.flow.Flow

class ExpenseCategoryRepository(
    private val dao: ExpenseCategoryDao
) {
    suspend fun insert(category: ExpenseCategoryEntity) = dao.insert(category)
    suspend fun update(category: ExpenseCategoryEntity) = dao.update(category)
    suspend fun delete(category: ExpenseCategoryEntity) = dao.delete(category)
    fun getAll(): Flow<List<ExpenseCategoryEntity>> = dao.getAll()
    suspend fun getById(id: Int): ExpenseCategoryEntity? = dao.getById(id)
}
