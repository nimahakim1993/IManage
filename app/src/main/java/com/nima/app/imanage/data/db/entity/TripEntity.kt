package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String,
    var startDate: Long,
    var endDate: Long?,
    var hostParticipantId: Int?,
    var createdAt: Long
)
