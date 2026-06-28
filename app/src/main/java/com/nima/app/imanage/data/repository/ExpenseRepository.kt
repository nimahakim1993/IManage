package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.ExpenseDao
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val dao: ExpenseDao
) {
    suspend fun insert(expense: ExpenseEntity) = dao.insert(expense)
    suspend fun update(expense: ExpenseEntity) = dao.update(expense)
    suspend fun delete(expense: ExpenseEntity) = dao.delete(expense)
    fun getAll(): Flow<List<ExpenseEntity>> = dao.getAll()
    fun get(id: Int): Flow<ExpenseEntity?> = dao.get(id)
    suspend fun clearCategoryForExpenses(categoryId: Int) = dao.clearCategoryForExpenses(categoryId)
}
