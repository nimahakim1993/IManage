package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "installment_items",
    foreignKeys = [
        ForeignKey(
            entity = InstallmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["installmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("installmentId")]
)
data class InstallmentItemEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var installmentId: Int,
    var dueDate: Long,
    var amount: Long,
    var settled: Boolean = false,
    var settledAt: Long = 0
)
