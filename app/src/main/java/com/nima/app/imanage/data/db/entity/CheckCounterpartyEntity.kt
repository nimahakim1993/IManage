package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_counterparties")
data class CheckCounterpartyEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String,
    var createdAt: Long
)
