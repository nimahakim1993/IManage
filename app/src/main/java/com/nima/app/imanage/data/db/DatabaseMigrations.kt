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

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS checks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, amount INTEGER NOT NULL, state TEXT NOT NULL, checkNumber TEXT NOT NULL, dueDate INTEGER NOT NULL, counterparty TEXT NOT NULL, description TEXT NOT NULL, createdAt INTEGER NOT NULL)"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS check_counterparties (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, createdAt INTEGER NOT NULL)"
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE checks ADD COLUMN settled INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE checks ADD COLUMN settledAt INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS car_service_types (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, colorIndex INTEGER NOT NULL, iconIndex INTEGER NOT NULL, createdAt INTEGER NOT NULL)"
            )
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (0, 'تعویض روغن', 0, 0, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (1, 'تعویض لاستیک', 1, 1, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (2, 'لنت ترمز', 2, 2, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (3, 'فیلتر', 3, 3, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (4, 'تسمه', 4, 4, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (5, 'لامپ', 5, 5, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (6, 'باتری', 6, 6, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (7, 'موتور', 7, 7, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (8, 'سرویس عمومی', 8, 8, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (9, 'بیمه', 9, 9, 0)")
            database.execSQL("INSERT INTO car_service_types (id, title, colorIndex, iconIndex, createdAt) VALUES (10, 'سایر', 10, 10, 0)")
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("UPDATE car_service_types SET title = 'تعویض روغن' WHERE id = 0")
            database.execSQL("UPDATE car_service_types SET title = 'تعویض لاستیک' WHERE id = 1")
            database.execSQL("UPDATE car_service_types SET title = 'لنت ترمز' WHERE id = 2")
            database.execSQL("UPDATE car_service_types SET title = 'فیلتر' WHERE id = 3")
            database.execSQL("UPDATE car_service_types SET title = 'تسمه' WHERE id = 4")
            database.execSQL("UPDATE car_service_types SET title = 'لامپ' WHERE id = 5")
            database.execSQL("UPDATE car_service_types SET title = 'باتری' WHERE id = 6")
            database.execSQL("UPDATE car_service_types SET title = 'موتور' WHERE id = 7")
            database.execSQL("UPDATE car_service_types SET title = 'سرویس عمومی' WHERE id = 8")
            database.execSQL("UPDATE car_service_types SET title = 'بیمه' WHERE id = 9")
            database.execSQL("UPDATE car_service_types SET title = 'سایر' WHERE id = 10")
        }
    }

    val MIGRATIONS = arrayOf<Migration>(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9
    )
}
