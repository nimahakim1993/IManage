package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_services")
data class CarServiceEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var serviceType: Int,
    var serviceDate: Long,
    var serviceKilometer: Int,
    var nextServiceDate: Long,
    var nextServiceKilometer: Int,
    var amountPaid: Long,
    var productBrand: String,
    var partName: String,
    var description: String,
    var createdAt: Long = System.currentTimeMillis()
)
