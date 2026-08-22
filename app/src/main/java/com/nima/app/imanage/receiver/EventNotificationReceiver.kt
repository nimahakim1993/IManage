package com.nima.app.imanage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nima.app.imanage.util.EventNotificationScheduler
import com.nima.app.imanage.worker.EventNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get

class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_DUE_TODAY
        EventNotificationScheduler(context).schedule(mode)

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val service: EventNotificationService = get(EventNotificationService::class.java)
                service.notifyEvents(mode)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_MODE = "event_notification_mode"
        const val MODE_DUE_TODAY = "due_today"
        const val MODE_DAY_BEFORE = "day_before"
    }
}
