package com.nima.app.imanage.presentation.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nima.app.imanage.data.db.AppDatabase
import org.koin.dsl.module


val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE bank_cards ADD COLUMN sortKey INTEGER NOT NULL DEFAULT 0")
        database.execSQL("UPDATE bank_cards SET sortKey = id")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS passwords (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                username TEXT NOT NULL,
                encryptedPassword TEXT NOT NULL,
                iconType INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app_db"
        )
            .addMigrations(MIGRATION_15_16, MIGRATION_16_17)
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().bankCardDao() }
    single { get<AppDatabase>().loanDao() }
    single { get<AppDatabase>().noteBoxDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().expenseCategoryDao() }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().incomeSourceDao() }
    single { get<AppDatabase>().incomeDao() }
    single { get<AppDatabase>().installmentDao() }
    single { get<AppDatabase>().installmentItemDao() }
    single { get<AppDatabase>().assetDao() }
    single { get<AppDatabase>().passwordItemDao() }
}