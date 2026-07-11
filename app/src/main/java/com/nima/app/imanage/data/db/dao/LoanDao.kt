package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loanEntity: LoanEntity)

    @Update
    suspend fun update(loanEntity: LoanEntity)

    @Delete
    suspend fun delete(loanEntity: LoanEntity)

    @Query("SELECT * FROM loans ORDER BY id DESC")
    fun getAll(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id")
    fun get(id: Int): Flow<LoanEntity?>

    @Query("SELECT * FROM loans WHERE settled = 0 AND dateReceiveBack >= :dateStart AND dateReceiveBack < :dateEnd")
    suspend fun getUnsettledDueBetween(dateStart: Long, dateEnd: Long): List<LoanEntity>
}