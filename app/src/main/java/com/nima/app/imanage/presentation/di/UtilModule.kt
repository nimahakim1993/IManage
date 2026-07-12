package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.util.BackupManager
import com.nima.app.imanage.util.NotificationHelper
import org.koin.dsl.module

val utilModule = module {
    single { NotificationHelper(get()) }
    single { BackupManager(get()) }
}