package com.nima.app.imanage.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nima.app.imanage.data.db.entity.OfficeReminderEntity
import com.nima.app.imanage.receiver.ReminderReceiver

class ReminderScheduler(private val context: Context) {
    fun schedule(reminder: OfficeReminderEntity) {
        if (reminder.reminderAt <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(reminder)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.reminderAt,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.reminderAt,
                pendingIntent
            )
        }
    }

    fun cancel(reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(reminderId))
    }

    suspend fun rescheduleAll(repository: com.nima.app.imanage.data.repository.OfficeReminderRepository) {
        val now = System.currentTimeMillis()
        val futureReminders = repository.getFutureReminders(now)
        futureReminders.forEach { reminder ->
            schedule(reminder)
        }
    }

    private fun pendingIntent(reminder: OfficeReminderEntity): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE, reminder.text)
            putExtra(ReminderReceiver.EXTRA_ID, reminder.id)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun pendingIntent(reminderId: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            reminderId,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: PendingIntent.getBroadcast(
            context,
            reminderId,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
    }
}
