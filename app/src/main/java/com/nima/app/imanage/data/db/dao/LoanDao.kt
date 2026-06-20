package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nima.app.imanage.data.db.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loanEntity: LoanEntity)

    @Query("SELECT * FROM loans ORDER BY id DESC")
    fun getAll() : Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id")
    fun get(id: Int): Flow<LoanEntity?>
}