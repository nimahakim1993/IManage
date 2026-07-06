package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_expense_splits",
    foreignKeys = [
        ForeignKey(
            entity = TripExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId"), Index("participantId")]
)
data class TripExpenseSplitEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var expenseId: Int,
    var participantId: Int,
    var amount: Double
)
