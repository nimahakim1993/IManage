package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.CheckCounterpartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckCounterpartyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counterparty: CheckCounterpartyEntity)

    @Update
    suspend fun update(counterparty: CheckCounterpartyEntity)

    @Delete
    suspend fun delete(counterparty: CheckCounterpartyEntity)

    @Query("SELECT * FROM check_counterparties ORDER BY title COLLATE NOCASE ASC")
    fun getAll(): Flow<List<CheckCounterpartyEntity>>
}
