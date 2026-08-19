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

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS pending_payments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sender TEXT NOT NULL, rawMessage TEXT NOT NULL, title TEXT NOT NULL, amount INTEGER NOT NULL, receivedAt INTEGER NOT NULL, messageHash TEXT NOT NULL, status TEXT NOT NULL)"
            )
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_pending_payments_messageHash ON pending_payments(messageHash)"
            )
        }
    }

    val MIGRATIONS = arrayOf<Migration>(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4
    )
}
