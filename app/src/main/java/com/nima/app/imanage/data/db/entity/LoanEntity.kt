package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var type: Int,
    var price: Long,
    var targetPersonName: String,
    var description: String,
    var date: Long
)