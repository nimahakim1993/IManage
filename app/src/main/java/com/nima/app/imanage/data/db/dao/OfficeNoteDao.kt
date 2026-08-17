package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nima.app.imanage.data.db.entity.OfficeNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficeNoteDao {
    @Insert
    suspend fun insert(note: OfficeNoteEntity)

    @Update
    suspend fun update(note: OfficeNoteEntity)

    @Delete
    suspend fun delete(note: OfficeNoteEntity)

    @Query("SELECT * FROM office_notes ORDER BY date DESC, createdAt DESC")
    fun getAll(): Flow<List<OfficeNoteEntity>>
}
