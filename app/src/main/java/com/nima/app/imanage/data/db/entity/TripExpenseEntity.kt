package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_expenses",
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
            childColumns = ["payerParticipantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("payerParticipantId")]
)
data class TripExpenseEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var tripId: Int,
    var title: String,
    var amount: Double,
    var date: Long,
    var payerParticipantId: Int,
    var description: String = "",
    var splitType: Int = 0
)
