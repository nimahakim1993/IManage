package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ExpenseCategoryEntity)

    @Update
    suspend fun update(category: ExpenseCategoryEntity)

    @Delete
    suspend fun delete(category: ExpenseCategoryEntity)

    @Query("SELECT * FROM expense_categories ORDER BY createdAt ASC")
    fun getAll(): Flow<List<ExpenseCategoryEntity>>

    @Query("SELECT * FROM expense_categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ExpenseCategoryEntity?
}
