package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nima.app.imanage.data.db.entity.PendingPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPaymentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(payment: PendingPaymentEntity): Long

    @Query("SELECT * FROM pending_payments WHERE messageHash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): PendingPaymentEntity?

    @Query("SELECT * FROM pending_payments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PendingPaymentEntity?

    @Query("SELECT * FROM pending_payments WHERE status = 'PENDING' ORDER BY receivedAt DESC")
    fun getPending(): Flow<List<PendingPaymentEntity>>

    @Query("SELECT * FROM pending_payments ORDER BY receivedAt DESC")
    fun getAll(): Flow<List<PendingPaymentEntity>>

    @Query("UPDATE pending_payments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}
