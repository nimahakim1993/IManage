package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.OfficeReminderDao
import com.nima.app.imanage.data.db.entity.OfficeReminderEntity
import kotlinx.coroutines.flow.Flow

class OfficeReminderRepository(private val dao: OfficeReminderDao) {
    fun getAll(): Flow<List<OfficeReminderEntity>> = dao.getAll()
    suspend fun getAllOnce(): List<OfficeReminderEntity> = dao.getAllOnce()
    suspend fun getFutureReminders(now: Long): List<OfficeReminderEntity> =
        dao.getFutureReminders(now)
    suspend fun insert(reminder: OfficeReminderEntity): Long = dao.insert(reminder)
    suspend fun update(reminder: OfficeReminderEntity) = dao.update(reminder)
    suspend fun delete(reminder: OfficeReminderEntity) = dao.delete(reminder)
}
