package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "office_notes")
data class OfficeNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)
