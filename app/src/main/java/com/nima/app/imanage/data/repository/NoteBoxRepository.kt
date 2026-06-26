package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.NoteBoxDao
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import kotlinx.coroutines.flow.Flow

class NoteBoxRepository(
    private val dao: NoteBoxDao
) {
    suspend fun insert(box: NoteBoxEntity) = dao.insert(box)
    suspend fun delete(box: NoteBoxEntity) = dao.delete(box)
    fun getAll(): Flow<List<NoteBoxEntity>> = dao.getAll()
    fun get(id: Int): Flow<NoteBoxEntity?> = dao.get(id)
}
