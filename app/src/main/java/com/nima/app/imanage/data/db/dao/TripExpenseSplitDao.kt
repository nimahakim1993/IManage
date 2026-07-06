package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nima.app.imanage.data.db.entity.TripExpenseSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripExpenseSplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(split: TripExpenseSplitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(splits: List<TripExpenseSplitEntity>)

    @Delete
    suspend fun delete(split: TripExpenseSplitEntity)

    @Query("DELETE FROM trip_expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteByExpense(expenseId: Int)

    @Query("SELECT * FROM trip_expense_splits WHERE expenseId = :expenseId")
    fun getByExpense(expenseId: Int): Flow<List<TripExpenseSplitEntity>>

    @Query("SELECT * FROM trip_expense_splits WHERE expenseId = :expenseId")
    suspend fun getByExpenseOnce(expenseId: Int): List<TripExpenseSplitEntity>

    @Query("SELECT * FROM trip_expense_splits WHERE expenseId IN (:expenseIds)")
    suspend fun getByExpenseIds(expenseIds: List<Int>): List<TripExpenseSplitEntity>
}
