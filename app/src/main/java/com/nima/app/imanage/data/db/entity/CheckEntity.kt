package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checks")
data class CheckEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var type: String,
    var amount: Long,
    var state: String,
    var checkNumber: String,
    var dueDate: Long,
    var counterparty: String,
    var description: String,
    var settled: Boolean = false,
    var settledAt: Long = 0,
    var createdAt: Long
) {
    companion object {
        const val TYPE_RECEIVED = "received"
        const val TYPE_PAYABLE = "payable"

        const val STATE_IN_PROGRESS = "in_progress"
        const val STATE_IN_BANK = "in_bank"
        const val STATE_COLLECTED = "collected"
        const val STATE_RETURNED = "returned"
        const val STATE_BOUNCED = "bounced"
    }
}
