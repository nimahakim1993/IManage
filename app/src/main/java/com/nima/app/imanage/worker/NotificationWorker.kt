package com.nima.app.imanage.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.context.GlobalContext

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MODE = "mode"
        const val MODE_DUE_TODAY = EventNotificationService.MODE_DUE_TODAY
        const val MODE_DAY_BEFORE = EventNotificationService.MODE_DAY_BEFORE
    }

    override suspend fun doWork(): Result {
        val koin = GlobalContext.get()
        val mode = inputData.getString(KEY_MODE) ?: MODE_DUE_TODAY
        val service: EventNotificationService = koin.get()
        service.notifyEvents(mode)
        return Result.success()
    }
}
