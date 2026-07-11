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
        loans: List<LoanEntity>,
        installmentItems: List<Pair<InstallmentItemEntity, String>>,
        carServices: List<CarServiceEntity>
    ) {
        val totalCount = loans.size + installmentItems.size + carServices.size
        if (totalCount == 0) return

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(context.getString(R.string.notif_title_reminders))

        loans.forEach { loan ->
            val label = if (loan.type == LoanEntity.TYPE_DEBT)
                context.getString(R.string.notif_type_debt)
            else
                context.getString(R.string.notif_type_receivable)
            inboxStyle.addLine(
                context.getString(
                    R.string.notif_line_loan,
                    loan.targetPersonName,
                    NumberFormatUtils.format(loan.price)
                )
            )
        }

        installmentItems.forEach { (item, title) ->
            inboxStyle.addLine(
                context.getString(
                    R.string.notif_line_installment,
                    title,
                    NumberFormatUtils.format(item.amount)
                )
            )
        }

        carServices.forEach { service ->
            val typeName = getCarServiceTypeName(service.serviceType)
            inboxStyle.addLine(
                context.getString(
                    R.string.notif_line_car,
                    typeName
                )
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val summaryText = context.resources.getQuantityString(
            R.plurals.notif_summary, totalCount, totalCount
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
