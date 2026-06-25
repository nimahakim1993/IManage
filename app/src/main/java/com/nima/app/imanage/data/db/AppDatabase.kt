package com.nima.app.imanage.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nima.app.imanage.data.db.dao.BankCardDao
import com.nima.app.imanage.data.db.dao.LoanDao
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.db.entity.LoanEntity


@Database(
    entities = [BankCardEntity::class, LoanEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankCardDao(): BankCardDao
    abstract fun loanDao(): LoanDao
}