package com.nima.app.imanage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.nima.app.imanage.data.db.entity.PendingPaymentEntity
import com.nima.app.imanage.data.repository.PendingPaymentRepository
import com.nima.app.imanage.util.NotificationHelper
import com.nima.app.imanage.util.SmsPaymentParser
import com.nima.app.imanage.util.SmsPaymentSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get

class SmsPaymentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages.firstOrNull()?.originatingAddress ?: run {
            pendingResult.finish()
            return
        }
        val body = messages.joinToString("") { it.messageBody.orEmpty() }
        val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings: SmsPaymentSettings = get(SmsPaymentSettings::class.java)
                if (!settings.isAllowed(sender)) return@launch
                val parsed = SmsPaymentParser.parse(body) ?: return@launch
                val payment = PendingPaymentEntity(
                    sender = sender,
                    rawMessage = body,
                    title = parsed.title,
                    amount = parsed.amount,
                    receivedAt = timestamp,
                    messageHash = SmsPaymentParser.hash(sender, timestamp, body)
                )
                val repository: PendingPaymentRepository = get(PendingPaymentRepository::class.java)
                if (repository.insert(payment)) {
                    val saved = repository.getPendingOnce(payment.messageHash)
                    if (saved != null) {
                        repository.confirm(saved.id)
                        val helper: NotificationHelper = get(NotificationHelper::class.java)
                        helper.showPendingPaymentNotification(saved)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
