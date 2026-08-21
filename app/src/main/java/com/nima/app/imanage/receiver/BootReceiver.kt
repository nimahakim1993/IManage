package com.nima.app.imanage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.nima.app.imanage.worker.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyNotification(context)
        }
    }

    companion object {
        private const val WORK_NAME = "daily_notification_check"
        private const val DAY_BEFORE_WORK_NAME = "check_day_before_notification"

        fun scheduleDailyNotification(context: Context) {
            val now = Calendar.getInstance()

            val nineAM = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (nineAM.before(now)) {
                nineAM.add(Calendar.DAY_OF_YEAR, 1)
            }
            val delayMs = nineAM.timeInMillis - now.timeInMillis

            val dailyWork = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(NotificationWorker.KEY_MODE to NotificationWorker.MODE_DUE_TODAY)
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWork
            )

            val eightPM = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (eightPM.before(now)) {
                eightPM.add(Calendar.DAY_OF_YEAR, 1)
            }
            val dayBeforeDelayMs = eightPM.timeInMillis - now.timeInMillis

            val dayBeforeWork = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(dayBeforeDelayMs, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(NotificationWorker.KEY_MODE to NotificationWorker.MODE_DAY_BEFORE)
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DAY_BEFORE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dayBeforeWork
            )
        }
    }
}
