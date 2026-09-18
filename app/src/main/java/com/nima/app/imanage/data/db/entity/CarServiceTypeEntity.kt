package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_service_types")
data class CarServiceTypeEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String,
    var colorIndex: Int,
    var iconIndex: Int,
    var createdAt: Long
)
