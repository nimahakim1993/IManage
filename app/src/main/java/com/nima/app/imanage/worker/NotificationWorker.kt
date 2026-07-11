package com.nima.app.imanage.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nima.app.imanage.data.db.dao.CarServiceDao
import com.nima.app.imanage.data.db.dao.InstallmentDao
import com.nima.app.imanage.data.db.dao.InstallmentItemDao
import com.nima.app.imanage.data.db.dao.LoanDao
import com.nima.app.imanage.util.NotificationHelper
import com.nima.app.imanage.util.ShamsiDate
import org.koin.core.context.GlobalContext

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val koin = GlobalContext.get()
        val loanDao: LoanDao = koin.get()
        val itemDao: InstallmentItemDao = koin.get()
        val carDao: CarServiceDao = koin.get()
        val installmentDao: InstallmentDao = koin.get()
        val notifHelper: NotificationHelper = koin.get()

        val todayStart = ShamsiDate.todayMillis()
        val todayEnd = todayStart + 86_400_000L

        val loans = loanDao.getUnsettledDueBetween(todayStart, todayEnd)
        val items = itemDao.getUnsettledDueBetween(todayStart, todayEnd)
        val carServices = carDao.getServiceDueBetween(todayStart, todayEnd)

        val installmentItems = items.mapNotNull { item ->
            val installment = installmentDao.getById(item.installmentId)
            installment?.let { item to it.title }
        }

        notifHelper.showReminderNotification(loans, installmentItems, carServices)

        return Result.success()
    }
}
