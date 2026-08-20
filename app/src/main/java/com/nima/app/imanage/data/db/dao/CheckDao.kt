package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.CheckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(check: CheckEntity)

    @Update
    suspend fun update(check: CheckEntity)

    @Delete
    suspend fun delete(check: CheckEntity)

    @Query("SELECT * FROM checks ORDER BY dueDate ASC, id DESC")
    fun getAll(): Flow<List<CheckEntity>>

    @Query("SELECT * FROM checks WHERE state NOT IN ('collected', 'returned', 'bounced') AND dueDate >= :dateStart AND dueDate < :dateEnd")
    suspend fun getDueBetween(dateStart: Long, dateEnd: Long): List<CheckEntity>
}
