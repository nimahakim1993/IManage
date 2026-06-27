package com.nima.app.imanage.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nima.app.imanage.data.db.dao.BankCardDao
import com.nima.app.imanage.data.db.dao.LoanDao
import com.nima.app.imanage.data.db.dao.NoteBoxDao
import com.nima.app.imanage.data.db.dao.NoteDao
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import com.nima.app.imanage.data.db.entity.NoteEntity


@Database(
    entities = [BankCardEntity::class, LoanEntity::class, NoteBoxEntity::class, NoteEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankCardDao(): BankCardDao
    abstract fun loanDao(): LoanDao
    abstract fun noteBoxDao(): NoteBoxDao
    abstract fun noteDao(): NoteDao
}