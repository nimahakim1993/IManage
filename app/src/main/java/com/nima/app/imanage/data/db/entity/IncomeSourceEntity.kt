package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_sources")
data class IncomeSourceEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String,
    var colorIndex: Int,
    var createdAt: Long
)
