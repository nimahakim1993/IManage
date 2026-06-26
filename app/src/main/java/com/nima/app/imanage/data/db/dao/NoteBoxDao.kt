package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteBoxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(box: NoteBoxEntity)

    @Delete
    suspend fun delete(box: NoteBoxEntity)

    @Query("SELECT * FROM note_boxes ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<NoteBoxEntity>>

    @Query("SELECT * FROM note_boxes WHERE id = :id LIMIT 1")
    fun get(id: Int): Flow<NoteBoxEntity?>
}
