package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.TripExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: TripExpenseEntity): Long

    @Update
    suspend fun update(expense: TripExpenseEntity)

    @Delete
    suspend fun delete(expense: TripExpenseEntity)

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId ORDER BY date DESC, id DESC")
    fun getByTrip(tripId: Int): Flow<List<TripExpenseEntity>>

    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId ORDER BY date DESC, id DESC")
    suspend fun getByTripOnce(tripId: Int): List<TripExpenseEntity>

    @Query("SELECT * FROM trip_expenses WHERE id = :id")
    fun get(id: Int): Flow<TripExpenseEntity?>

    @Query("SELECT * FROM trip_expenses WHERE id = :id")
    suspend fun getOnce(id: Int): TripExpenseEntity?
}
