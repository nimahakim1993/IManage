package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_payments",
    indices = [Index(value = ["messageHash"], unique = true)]
)
data class PendingPaymentEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var sender: String,
    var rawMessage: String,
    var title: String,
    var amount: Long,
    var receivedAt: Long,
    var messageHash: String,
    var status: String = STATUS_PENDING
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_CONFIRMED = "CONFIRMED"
        const val STATUS_IGNORED = "IGNORED"
    }
}
