package com.nima.app.imanage.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nima.app.imanage.MainActivity
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.db.entity.PendingPaymentEntity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "daily_reminders"
        const val GROUP_KEY = "daily_reminders_group"
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_ID_DAY_BEFORE = 1002
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        private const val PAYMENT_NOTIFICATION_BASE = 30_000
    }

    fun createChannel() {
        val localizedContext = LanguageManager.wrap(context)
        val channel = NotificationChannel(
            CHANNEL_ID,
            localizedContext.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = localizedContext.getString(R.string.notif_channel_desc)
            setShowBadge(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("SuspiciousIndentation")
    fun showReminderNotification(
        dueLoans: List<LoanEntity>,
        settlementLoans: List<LoanEntity>,
        installmentItems: List<Pair<InstallmentItemEntity, String>>,
        serviceDateCarServices: List<CarServiceEntity>,
        nextServiceCarServices: List<CarServiceEntity>,
        dueChecks: List<CheckEntity>
    ) {
        val localizedContext = LanguageManager.wrap(context)
        val count = dueLoans.size + settlementLoans.size + installmentItems.size +
                serviceDateCarServices.size + nextServiceCarServices.size
        +dueChecks.size
        if (count == 0) return

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(localizedContext.getString(R.string.notif_title_reminders))

        var targetScreen: String? = null

        dueLoans.forEach { loan ->
            val (label, screen) = if (loan.type == LoanEntity.TYPE_DEBT) {
                localizedContext.getString(R.string.notif_label_debt_due) to "loans"
            } else {
                localizedContext.getString(R.string.notif_label_receivable_due) to "loans"
            }
            if (targetScreen == null) targetScreen = screen
            inboxStyle.addLine(
                localizedContext.getString(
                    R.string.notif_line_amount,
                    label,
                    loan.targetPersonName,
                    NumberFormatUtils.format(loan.price)
                )
            )
        }

        settlementLoans.forEach { loan ->
            val (label, screen) = if (loan.type == LoanEntity.TYPE_DEBT) {
                localizedContext.getString(R.string.notif_label_debt_settlement) to "loans"
            } else {
                localizedContext.getString(R.string.notif_label_receivable_settlement) to "loans"
            }
            if (targetScreen == null) targetScreen = screen
            inboxStyle.addLine(
                localizedContext.getString(
                    R.string.notif_line_amount,
                    label,
                    loan.targetPersonName,
                    NumberFormatUtils.format(loan.price)
                )
            )
        }

        installmentItems.forEach { (item, title) ->
            if (targetScreen == null) targetScreen = "installments"
            inboxStyle.addLine(
                localizedContext.getString(
                    R.string.notif_line_amount,
                    localizedContext.getString(R.string.notif_label_installment),
                    title,
                    NumberFormatUtils.format(item.amount)
                )
            )
        }

        serviceDateCarServices.forEach { service ->
            if (targetScreen == null) targetScreen = "car_services"
            val typeName = getCarServiceTypeName(service.serviceType, localizedContext)
            inboxStyle.addLine(
                localizedContext.getString(
                    R.string.notif_line_amount,
                    localizedContext.getString(R.string.notif_label_car_service),
                    typeName,
                    NumberFormatUtils.format(service.amountPaid)
                )
            )
        }

        nextServiceCarServices.forEach { service ->
            if (targetScreen == null) targetScreen = "car_services"
            val typeName = getCarServiceTypeName(service.serviceType, localizedContext)
            inboxStyle.addLine(
                localizedContext.getString(
                    R.string.notif_line_no_amount,
                    localizedContext.getString(R.string.notif_label_car_next_service),
                    typeName
                )
            )
        }

        dueChecks.forEach { check ->
            if (targetScreen == null) targetScreen = "checks"
            inboxStyle.addLine(
                localizedContext.getString(
                    R.string.notif_line_amount,
                    localizedContext.getString(R.string.notif_label_check_due),
                    check.counterparty,
                    NumberFormatUtils.format(check.amount)
                )
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            targetScreen?.let { putExtra(EXTRA_NAVIGATE_TO, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val summaryText = localizedContext.resources.getQuantityString(
            R.plurals.notif_summary, count, count
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.imanage_logo)
            .setContentTitle(localizedContext.getString(R.string.notif_title_reminders))
            .setContentText(summaryText)
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showCheckDayBeforeNotification(checks: List<CheckEntity>) {
        if (checks.isEmpty()) return

        val localizedContext = LanguageManager.wrap(context)

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(
                localizedContext.getString(R.string.check_due_tomorrow_notification_title)
            )

        checks.forEach { check ->
            inboxStyle.addLine(
                localizedContext.getString(
                    R.string.check_due_tomorrow_notification_text,
                    check.counterparty,
                    NumberFormatUtils.format(check.amount)
                )
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAVIGATE_TO, "checks")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DAY_BEFORE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val summaryText = localizedContext.resources.getQuantityString(
            R.plurals.check_due_tomorrow_summary, checks.size, checks.size
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.imanage_logo)
            .setContentTitle(
                localizedContext.getString(R.string.check_due_tomorrow_notification_title)
            )
            .setContentText(summaryText)
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_DAY_BEFORE, notification)
    }

    fun showUserReminderNotification(title: String, description: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.imanage_logo)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    fun showPendingPaymentNotification(payment: PendingPaymentEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val localizedContext = LanguageManager.wrap(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAVIGATE_TO, "expenses")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            PAYMENT_NOTIFICATION_BASE + payment.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.imanage_logo)
            .setContentTitle(localizedContext.getString(R.string.sms_new_payment))
            .setContentText(
                localizedContext.getString(
                    R.string.sms_payment_amount,
                    NumberFormatUtils.format(payment.amount)
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(PAYMENT_NOTIFICATION_BASE + payment.id, notification)
    }

    private fun getCarServiceTypeName(serviceType: Int, localizedContext: Context): String {
        return when (serviceType) {
            0 -> localizedContext.getString(R.string.car_type_oil_change)
            1 -> localizedContext.getString(R.string.car_type_tire_change)
            2 -> localizedContext.getString(R.string.car_type_brake_pad)
            3 -> localizedContext.getString(R.string.car_type_belt)
            4 -> localizedContext.getString(R.string.car_type_lamp)
            5 -> localizedContext.getString(R.string.car_type_battery)
            6 -> localizedContext.getString(R.string.car_type_engine)
            7 -> localizedContext.getString(R.string.car_type_general)
            8 -> localizedContext.getString(R.string.car_type_insurance)
            9 -> localizedContext.getString(R.string.car_type_other)
            else -> localizedContext.getString(R.string.car_type_other)
        }

    }
}
