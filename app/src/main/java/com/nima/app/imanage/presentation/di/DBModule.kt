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
        ).build()
    }
    single { get<AppDatabase>().bankCardDao() }
}