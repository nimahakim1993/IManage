package com.nima.app.imanage.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nima.app.imanage.MainActivity
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.db.entity.LoanEntity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "daily_reminders"
        const val GROUP_KEY = "daily_reminders_group"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_NAVIGATE_TO = "navigate_to"
    }

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notif_channel_desc)
            setShowBadge(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showReminderNotification(
        dueLoans: List<LoanEntity>,
        settlementLoans: List<LoanEntity>,
        installmentItems: List<Pair<InstallmentItemEntity, String>>,
        serviceDateCarServices: List<CarServiceEntity>,
        nextServiceCarServices: List<CarServiceEntity>
    ) {
        val count = dueLoans.size + settlementLoans.size + installmentItems.size +
                serviceDateCarServices.size + nextServiceCarServices.size
        if (count == 0) return

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(context.getString(R.string.notif_title_reminders))

        var targetScreen: String? = null

        dueLoans.forEach { loan ->
            val (label, screen) = if (loan.type == LoanEntity.TYPE_DEBT) {
                context.getString(R.string.notif_label_debt_due) to "loans"
            } else {
                context.getString(R.string.notif_label_receivable_due) to "loans"
            }
            if (targetScreen == null) targetScreen = screen
            inboxStyle.addLine(
                context.getString(
                    R.string.notif_line_amount,
                    label,
                    loan.targetPersonName,
                    NumberFormatUtils.format(loan.price)
                )
            )
        }

        settlementLoans.forEach { loan ->
            val (label, screen) = if (loan.type == LoanEntity.TYPE_DEBT) {
                context.getString(R.string.notif_label_debt_settlement) to "loans"
            } else {
                context.getString(R.string.notif_label_receivable_settlement) to "loans"
            }
            if (targetScreen == null) targetScreen = screen
            inboxStyle.addLine(
                context.getString(
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
                context.getString(
                    R.string.notif_line_amount,
                    context.getString(R.string.notif_label_installment),
                    title,
                    NumberFormatUtils.format(item.amount)
                )
            )
        }

        serviceDateCarServices.forEach { service ->
            if (targetScreen == null) targetScreen = "car_services"
            val typeName = getCarServiceTypeName(service.serviceType)
            inboxStyle.addLine(
                context.getString(
                    R.string.notif_line_amount,
                    context.getString(R.string.notif_label_car_service),
                    typeName,
                    NumberFormatUtils.format(service.amountPaid)
                )
            )
        }

        nextServiceCarServices.forEach { service ->
            if (targetScreen == null) targetScreen = "car_services"
            val typeName = getCarServiceTypeName(service.serviceType)
            inboxStyle.addLine(
                context.getString(
                    R.string.notif_line_no_amount,
                    context.getString(R.string.notif_label_car_next_service),
                    typeName
                )
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            targetScreen?.let { putExtra(EXTRA_NAVIGATE_TO, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val summaryText = context.resources.getQuantityString(
            R.plurals.notif_summary, count, count
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.imanage_logo)
            .setContentTitle(context.getString(R.string.notif_title_reminders))
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

    private fun getCarServiceTypeName(serviceType: Int): String {
        return when (serviceType) {
            0 -> context.getString(R.string.car_type_oil_change)
            1 -> context.getString(R.string.car_type_tire_change)
            2 -> context.getString(R.string.car_type_brake_pad)
            3 -> context.getString(R.string.car_type_belt)
            4 -> context.getString(R.string.car_type_lamp)
            5 -> context.getString(R.string.car_type_battery)
            6 -> context.getString(R.string.car_type_engine)
            7 -> context.getString(R.string.car_type_general)
            8 -> context.getString(R.string.car_type_insurance)
            9 -> context.getString(R.string.car_type_other)
            else -> context.getString(R.string.car_type_other)
        }
    }
}
