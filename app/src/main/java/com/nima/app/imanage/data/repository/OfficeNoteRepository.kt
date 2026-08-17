package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.OfficeNoteDao
import com.nima.app.imanage.data.db.entity.OfficeNoteEntity
import kotlinx.coroutines.flow.Flow

class OfficeNoteRepository(private val dao: OfficeNoteDao) {
    fun getAll(): Flow<List<OfficeNoteEntity>> = dao.getAll()
    suspend fun insert(note: OfficeNoteEntity): Unit = dao.insert(note)
    suspend fun update(note: OfficeNoteEntity) = dao.update(note)
    suspend fun delete(note: OfficeNoteEntity) = dao.delete(note)
}
