package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.CarServiceTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarServiceTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(type: CarServiceTypeEntity)

    @Update
    suspend fun update(type: CarServiceTypeEntity)

    @Delete
    suspend fun delete(type: CarServiceTypeEntity)

    @Query("SELECT * FROM car_service_types ORDER BY createdAt ASC")
    fun getAll(): Flow<List<CarServiceTypeEntity>>

    @Query("SELECT * FROM car_service_types ORDER BY createdAt ASC")
    suspend fun getAllOnce(): List<CarServiceTypeEntity>

    @Query("SELECT * FROM car_service_types WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CarServiceTypeEntity?
}
