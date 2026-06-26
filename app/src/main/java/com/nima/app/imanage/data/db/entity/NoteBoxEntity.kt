package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_boxes")
data class NoteBoxEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val colorIndex: Int,
    val createdAt: Long,
    val updatedAt: Long
)
