package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.OfficeReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficeReminderDao {
    @Insert
    suspend fun insert(reminder: OfficeReminderEntity): Long

    @Update
    suspend fun update(reminder: OfficeReminderEntity)

    @Delete
    suspend fun delete(reminder: OfficeReminderEntity)

    @Query("SELECT * FROM office_reminders ORDER BY reminderAt ASC")
    fun getAll(): Flow<List<OfficeReminderEntity>>

    @Query("SELECT * FROM office_reminders ORDER BY reminderAt ASC")
    suspend fun getAllOnce(): List<OfficeReminderEntity>

    @Query("SELECT * FROM office_reminders WHERE reminderAt > :now ORDER BY reminderAt ASC")
    suspend fun getFutureReminders(now: Long): List<OfficeReminderEntity>
}
