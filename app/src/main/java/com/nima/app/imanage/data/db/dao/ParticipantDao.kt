package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(participant: ParticipantEntity)

    @Update
    suspend fun update(participant: ParticipantEntity)

    @Delete
    suspend fun delete(participant: ParticipantEntity)

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId ORDER BY id ASC")
    fun getByTrip(tripId: Int): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId ORDER BY id ASC")
    suspend fun getByTripOnce(tripId: Int): List<ParticipantEntity>

    @Query("SELECT * FROM trip_participants WHERE id = :id")
    fun get(id: Int): Flow<ParticipantEntity?>

    @Query("SELECT * FROM trip_participants WHERE id = :id")
    suspend fun getOnce(id: Int): ParticipantEntity?
}
