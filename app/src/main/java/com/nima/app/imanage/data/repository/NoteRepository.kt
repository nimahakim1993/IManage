package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.NoteDao
import com.nima.app.imanage.data.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val dao: NoteDao
) {
    suspend fun insert(note: NoteEntity) = dao.insert(note)
    suspend fun delete(note: NoteEntity) = dao.delete(note)
    fun getByBox(boxId: Int): Flow<List<NoteEntity>> = dao.getByBox(boxId)
    fun get(id: Int): Flow<NoteEntity?> = dao.get(id)
    fun countByBox(boxId: Int): Flow<Int> = dao.countByBox(boxId)
}
