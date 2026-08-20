package com.nima.app.imanage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nima.app.imanage.util.LanguageManager
import com.nima.app.imanage.util.NotificationHelper
import org.koin.java.KoinJavaComponent.get

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        if (title.isNotBlank()) {
            val helper: NotificationHelper = get(NotificationHelper::class.java)
            val localizedContext = LanguageManager.wrap(context)
            helper.showUserReminderNotification(
                title = localizedContext.getString(com.nima.app.imanage.R.string.reminder_notification_title),
                description = title,
                notificationId = 2000 + intent.getIntExtra(EXTRA_ID, 0)
            )
        }
    }

    companion object {
        const val EXTRA_TITLE = "reminder_text"
        const val EXTRA_ID = "reminder_id"
    }
}
