package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_participants",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var tripId: Int,
    var name: String,
    var colorIndex: Int = 0
)
