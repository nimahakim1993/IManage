package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_settlements",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromParticipantId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["toParticipantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("fromParticipantId"), Index("toParticipantId")]
)
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var tripId: Int,
    var fromParticipantId: Int,
    var toParticipantId: Int,
    var amount: Double,
    var date: Long,
    var note: String = ""
)
