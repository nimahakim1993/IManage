package com.nima.app.imanage.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nima.app.imanage.data.repository.OfficeReminderRepository
import com.nima.app.imanage.util.EventNotificationScheduler
import com.nima.app.imanage.util.ReminderScheduler
import org.koin.core.context.GlobalContext

class AlarmRescheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val koin = GlobalContext.getOrNull() ?: return Result.retry()
        val reminderScheduler: ReminderScheduler = koin.get()
        val repository: OfficeReminderRepository = koin.get()
        EventNotificationScheduler(applicationContext).scheduleAll()
        reminderScheduler.rescheduleAll(repository)
        return Result.success()
    }
}
