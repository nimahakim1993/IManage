package com.nima.app.imanage.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE installments ADD COLUMN colorIndex INTEGER NOT NULL DEFAULT 1")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS office_notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date INTEGER NOT NULL, text TEXT NOT NULL, createdAt INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS office_reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date INTEGER NOT NULL, reminderAt INTEGER NOT NULL, text TEXT NOT NULL, createdAt INTEGER NOT NULL)"
            )
        }
    }

    val MIGRATIONS = arrayOf<Migration>(
        MIGRATION_1_2,
        MIGRATION_2_3
    )
}
