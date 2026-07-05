package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.PasswordItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PasswordItemEntity)

    @Update
    suspend fun update(item: PasswordItemEntity)

    @Delete
    suspend fun delete(item: PasswordItemEntity)

    @Query("SELECT * FROM passwords ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<PasswordItemEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<PasswordItemEntity?>
}