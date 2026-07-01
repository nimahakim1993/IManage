package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installments")
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String,
    var description: String,
    var numberOfInstallments: Int,
    var periodType: Int,
    var periodDays: Int,
    var amount: Long,
    var startDate: Long,
    var createdAt: Long
) {
    companion object {
        const val PERIOD_MONTHLY = 0
        const val PERIOD_WEEKLY = 1
        const val PERIOD_CUSTOM = 2
    }
}
