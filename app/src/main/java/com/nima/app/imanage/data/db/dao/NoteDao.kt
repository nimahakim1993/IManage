package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nima.app.imanage.data.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE boxId = :boxId ORDER BY updatedAt DESC")
    fun getByBox(boxId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<NoteEntity?>

    @Query("SELECT COUNT(*) FROM notes WHERE boxId = :boxId")
    fun countByBox(boxId: Int): Flow<Int>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAllOnce(): List<NoteEntity>
}
