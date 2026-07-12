package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nima.app.imanage.data.db.entity.SettlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settlement: SettlementEntity): Long

    @Delete
    suspend fun delete(settlement: SettlementEntity)

    @Query("SELECT * FROM trip_settlements WHERE tripId = :tripId ORDER BY date DESC, id DESC")
    fun getByTrip(tripId: Int): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM trip_settlements WHERE tripId = :tripId ORDER BY date DESC, id DESC")
    suspend fun getByTripOnce(tripId: Int): List<SettlementEntity>

    @Query("SELECT * FROM trip_settlements WHERE id = :id")
    suspend fun getOnce(id: Int): SettlementEntity?

    @Query("SELECT * FROM trip_settlements ORDER BY date DESC, id DESC")
    suspend fun getAllOnce(): List<SettlementEntity>
}
