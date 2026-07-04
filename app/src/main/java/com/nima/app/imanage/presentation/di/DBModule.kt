package com.nima.app.imanage.presentation.di

import androidx.room.Room
import com.nima.app.imanage.data.db.AppDatabase
import org.koin.dsl.module


val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app_db"
        ).fallbackToDestructiveMigration().build()
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
}