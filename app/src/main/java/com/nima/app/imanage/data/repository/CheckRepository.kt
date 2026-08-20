package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.CheckDao
import com.nima.app.imanage.data.db.entity.CheckEntity
import kotlinx.coroutines.flow.Flow

class CheckRepository(private val dao: CheckDao) {
    suspend fun insert(check: CheckEntity) = dao.insert(check)
    suspend fun update(check: CheckEntity) = dao.update(check)
    suspend fun delete(check: CheckEntity) = dao.delete(check)
    fun getAll(): Flow<List<CheckEntity>> = dao.getAll()
    suspend fun getDueBetween(dateStart: Long, dateEnd: Long) =
        dao.getDueBetween(dateStart, dateEnd)
}
