package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = IncomeSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("sourceId")]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String,
    var amount: Long,
    var sourceId: Int?,
    var description: String,
    var incomeDate: Long,
    var createdAt: Long
)
