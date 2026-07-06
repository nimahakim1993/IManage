package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.TripExpenseSplitDao
import com.nima.app.imanage.data.db.entity.TripExpenseSplitEntity

class TripExpenseSplitRepository(private val dao: TripExpenseSplitDao) {
    suspend fun insert(split: TripExpenseSplitEntity) = dao.insert(split)
    suspend fun insertAll(splits: List<TripExpenseSplitEntity>) = dao.insertAll(splits)
    suspend fun deleteByExpense(expenseId: Int) = dao.deleteByExpense(expenseId)
    suspend fun getByExpenseOnce(expenseId: Int) = dao.getByExpenseOnce(expenseId)
    suspend fun getByExpenseIds(expenseIds: List<Int>) = dao.getByExpenseIds(expenseIds)
}
