package com.nima.app.imanage.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE installments ADD COLUMN colorIndex INTEGER NOT NULL DEFAULT 1")
        }
    }

    val MIGRATIONS = arrayOf<Migration>(
        MIGRATION_1_2
    )
}
