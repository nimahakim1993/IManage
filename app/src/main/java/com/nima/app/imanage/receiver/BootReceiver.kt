package com.nima.app.imanage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nima.app.imanage.util.EventNotificationScheduler
import com.nima.app.imanage.worker.AlarmRescheduleWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyNotification(context)
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                rescheduleOfficeReminders(context)
            }
        }
    }

    companion object {
        private const val WORK_NAME = "daily_notification_check"
        private const val DAY_BEFORE_WORK_NAME = "check_day_before_notification"
        private const val RESCHEDULE_WORK_NAME = "alarm_reschedule_worker"

        fun scheduleDailyNotification(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(DAY_BEFORE_WORK_NAME)
            EventNotificationScheduler(context).scheduleAll()
            scheduleAlarmRescheduleWorker(context)
        }

        fun scheduleAlarmRescheduleWorker(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<AlarmRescheduleWorker>(
                6, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                RESCHEDULE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        private suspend fun rescheduleOfficeReminders(context: Context) {
            val koin = GlobalContext.getOrNull() ?: return
            val reminderScheduler = koin.get<com.nima.app.imanage.util.ReminderScheduler>()
            val repository =
                koin.get<com.nima.app.imanage.data.repository.OfficeReminderRepository>()
            reminderScheduler.rescheduleAll(repository)
        }
    }
}
