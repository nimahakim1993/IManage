package com.nima.app.imanage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nima.app.imanage.data.repository.PendingPaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get

class PendingPaymentActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, -1)
        if (id < 0) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository: PendingPaymentRepository = get(PendingPaymentRepository::class.java)
                if (intent.action == ACTION_CONFIRM) repository.confirm(id) else repository.ignore(
                    id
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_CONFIRM = "com.nima.app.imanage.CONFIRM_PAYMENT"
        const val ACTION_IGNORE = "com.nima.app.imanage.IGNORE_PAYMENT"
        const val EXTRA_ID = "pending_payment_id"
    }
}
