package com.nima.app.imanage.presentation.di

import androidx.room.Room
import com.nima.app.imanage.data.db.AppDatabase
import com.nima.app.imanage.data.db.DatabaseMigrations
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "imanage_db"
        )
            .addMigrations(*DatabaseMigrations.MIGRATIONS)
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
    single { get<AppDatabase>().tripDao() }
    single { get<AppDatabase>().participantDao() }
    single { get<AppDatabase>().tripExpenseDao() }
    single { get<AppDatabase>().tripExpenseSplitDao() }
    single { get<AppDatabase>().settlementDao() }
    single { get<AppDatabase>().carServiceDao() }
}
