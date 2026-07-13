package com.nima.app.imanage.data.db

import androidx.room.migration.Migration

object DatabaseMigrations {
    val MIGRATIONS = arrayOf<Migration>(
        // Example for future use:
        // MIGRATION_1_2 = object : Migration(1, 2) {
        //     override fun migrate(database: SupportSQLiteDatabase) {
        //         database.execSQL("ALTER TABLE loans ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
        //     }
        // },
    )
}
