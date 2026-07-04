package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconType: Int,
    val unitCount: Double,
    val pricePerUnit: Long,
    val createdAt: Long,
    val updatedAt: Long
)
