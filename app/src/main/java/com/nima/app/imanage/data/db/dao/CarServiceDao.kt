package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(carServiceEntity: CarServiceEntity)

    @Update
    suspend fun update(carServiceEntity: CarServiceEntity)

    @Delete
    suspend fun delete(carServiceEntity: CarServiceEntity)

    @Query("SELECT * FROM car_services ORDER BY id DESC")
    fun getAll(): Flow<List<CarServiceEntity>>

    @Query("SELECT * FROM car_services WHERE id = :id")
    fun get(id: Int): Flow<CarServiceEntity?>

    @Query("SELECT * FROM car_services WHERE nextServiceDate > 0 AND nextServiceDate >= :dateStart AND nextServiceDate < :dateEnd")
    suspend fun getServiceDueBetween(dateStart: Long, dateEnd: Long): List<CarServiceEntity>
}
