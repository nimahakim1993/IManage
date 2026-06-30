package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeSourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: IncomeSourceEntity)

    @Update
    suspend fun update(source: IncomeSourceEntity)

    @Delete
    suspend fun delete(source: IncomeSourceEntity)

    @Query("SELECT * FROM income_sources ORDER BY createdAt ASC")
    fun getAll(): Flow<List<IncomeSourceEntity>>

    @Query("SELECT * FROM income_sources WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): IncomeSourceEntity?
}
