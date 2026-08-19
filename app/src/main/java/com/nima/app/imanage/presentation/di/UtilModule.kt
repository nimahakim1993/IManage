package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.util.BackupManager
import com.nima.app.imanage.util.NotificationHelper
import com.nima.app.imanage.util.ReminderScheduler
import com.nima.app.imanage.util.SmsPaymentSettings
import org.koin.dsl.module

val utilModule = module {
    single { NotificationHelper(get()) }
    single { ReminderScheduler(get()) }
    single { BackupManager(get()) }
    single { SmsPaymentSettings(get()) }
}
