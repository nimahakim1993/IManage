package com.nima.app.imanage.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_cards")
data class BankCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardNumber: String,
    val cvv: String,
    val month: String,
    val year: String,
    val bankName: String,
    val color: Long,
    val shebaNumber: String? = null,
    val accountNumber: String? = null
)