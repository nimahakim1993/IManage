package com.nima.app.imanage.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nima.app.imanage.data.db.dao.BankCardDao
import com.nima.app.imanage.data.db.entity.BankCardEntity


@Database(
    entities = [BankCardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankCardDao(): BankCardDao
}