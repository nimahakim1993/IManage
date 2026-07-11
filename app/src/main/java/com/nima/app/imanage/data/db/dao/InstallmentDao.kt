package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: InstallmentEntity): Long

    @Update
    suspend fun update(installment: InstallmentEntity)

    @Delete
    suspend fun delete(installment: InstallmentEntity)

    @Query("SELECT * FROM installments ORDER BY createdAt DESC")
    fun getAll(): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<InstallmentEntity?>

    @Query("SELECT * FROM installments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): InstallmentEntity?
}
