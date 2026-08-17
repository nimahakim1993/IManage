package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "office_reminders")
data class OfficeReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val reminderAt: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)
