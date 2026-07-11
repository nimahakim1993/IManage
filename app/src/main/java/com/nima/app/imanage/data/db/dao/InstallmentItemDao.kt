package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InstallmentItemEntity)

    @Update
    suspend fun update(item: InstallmentItemEntity)

    @Delete
    suspend fun delete(item: InstallmentItemEntity)

    @Query("SELECT * FROM installment_items WHERE installmentId = :installmentId ORDER BY dueDate ASC")
    fun getByInstallmentId(installmentId: Int): Flow<List<InstallmentItemEntity>>

    @Query("SELECT * FROM installment_items WHERE installmentId = :installmentId ORDER BY dueDate ASC")
    suspend fun getByInstallmentIdOnce(installmentId: Int): List<InstallmentItemEntity>

    @Query("SELECT * FROM installment_items ORDER BY dueDate ASC")
    fun getAllItems(): Flow<List<InstallmentItemEntity>>

    @Query("DELETE FROM installment_items WHERE installmentId = :installmentId")
    suspend fun deleteByInstallmentId(installmentId: Int)

    @Query("SELECT * FROM installment_items WHERE settled = 0 AND dueDate >= :dateStart AND dueDate < :dateEnd")
    suspend fun getUnsettledDueBetween(dateStart: Long, dateEnd: Long): List<InstallmentItemEntity>
}
