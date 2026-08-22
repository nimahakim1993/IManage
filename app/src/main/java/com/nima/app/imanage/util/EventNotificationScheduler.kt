package com.nima.app.imanage.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nima.app.imanage.receiver.EventNotificationReceiver
import java.util.Calendar

class EventNotificationScheduler(private val context: Context) {
    fun scheduleAll() {
        schedule(EventNotificationReceiver.MODE_DUE_TODAY)
        schedule(EventNotificationReceiver.MODE_DAY_BEFORE)
    }

    fun schedule(mode: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerTime(mode)
        val alarmIntent = pendingIntent(mode)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                alarmIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                alarmIntent
            )
        }
    }

    fun cancelAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(EventNotificationReceiver.MODE_DUE_TODAY))
        alarmManager.cancel(pendingIntent(EventNotificationReceiver.MODE_DAY_BEFORE))
    }

    private fun nextTriggerTime(mode: String): Long {
        val now = Calendar.getInstance()
        val targetHour = if (mode == EventNotificationReceiver.MODE_DAY_BEFORE) 20 else 9
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis
    }

    private fun pendingIntent(mode: String): PendingIntent {
        val requestCode = if (mode == EventNotificationReceiver.MODE_DAY_BEFORE) {
            REQUEST_CODE_DAY_BEFORE
        } else {
            REQUEST_CODE_DUE_TODAY
        }
        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            putExtra(EventNotificationReceiver.EXTRA_MODE, mode)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val REQUEST_CODE_DUE_TODAY = 4001
        private const val REQUEST_CODE_DAY_BEFORE = 4002
    }
}
