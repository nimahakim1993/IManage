package com.nima.app.imanage.presentation.di

import com.nima.app.imanage.util.BackupManager
import com.nima.app.imanage.util.NotificationHelper
import com.nima.app.imanage.util.ReminderScheduler
import com.nima.app.imanage.util.SmsPaymentSettings
import com.nima.app.imanage.worker.EventNotificationService
import org.koin.dsl.module

val utilModule = module {
    single { NotificationHelper(get()) }
    single { ReminderScheduler(get()) }
    single { BackupManager(get()) }
    single { SmsPaymentSettings(get()) }
    single {
        EventNotificationService(
            checkDao = get(),
            loanDao = get(),
            itemDao = get(),
            carDao = get(),
            installmentDao = get(),
            notificationHelper = get()
        )
    }
}
