package com.nima.app.imanage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.nima.app.imanage.util.EventNotificationScheduler

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
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(DAY_BEFORE_WORK_NAME)
            EventNotificationScheduler(context).scheduleAll()
        }
    }
}
