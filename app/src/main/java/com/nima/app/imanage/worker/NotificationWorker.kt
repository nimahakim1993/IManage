package com.nima.app.imanage.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nima.app.imanage.data.db.dao.CarServiceDao
import com.nima.app.imanage.data.db.dao.CheckDao
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

    companion object {
        const val KEY_MODE = "mode"
        const val MODE_DUE_TODAY = "due_today"
        const val MODE_DAY_BEFORE = "day_before"
    }

    override suspend fun doWork(): Result {
        val koin = GlobalContext.get()
        val checkDao: CheckDao = koin.get()
        val notifHelper: NotificationHelper = koin.get()

        val mode = inputData.getString(KEY_MODE) ?: MODE_DUE_TODAY

        if (mode == MODE_DAY_BEFORE) {
            val todayStart = ShamsiDate.todayMillis()
            val tomorrowStart = todayStart + 86_400_000L
            val tomorrowEnd = tomorrowStart + 86_400_000L
            val dueChecks = checkDao.getDueBetween(tomorrowStart, tomorrowEnd)
            notifHelper.showCheckDayBeforeNotification(dueChecks)
            return Result.success()
        }

        val loanDao: LoanDao = koin.get()
        val itemDao: InstallmentItemDao = koin.get()
        val carDao: CarServiceDao = koin.get()
        val installmentDao: InstallmentDao = koin.get()

        val todayStart = ShamsiDate.todayMillis()
        val todayEnd = todayStart + 86_400_000L

        val dueLoans = loanDao.getLoanDateBetween(todayStart, todayEnd)
        val settlementLoans = loanDao.getUnsettledDueBetween(todayStart, todayEnd)
        val items = itemDao.getUnsettledDueBetween(todayStart, todayEnd)
        val serviceDateCarServices = carDao.getServiceDateBetween(todayStart, todayEnd)
        val nextServiceCarServices = carDao.getNextServiceDueBetween(todayStart, todayEnd)
        val dueChecks = checkDao.getDueBetween(todayStart, todayEnd)

        val installmentTitles = items.mapNotNull { item ->
            val installment = installmentDao.getById(item.installmentId)
            installment?.let { item to it.title }
        }

        notifHelper.showReminderNotification(
            dueLoans = dueLoans,
            settlementLoans = settlementLoans,
            installmentItems = installmentTitles,
            serviceDateCarServices = serviceDateCarServices,
            nextServiceCarServices = nextServiceCarServices,
            dueChecks = dueChecks
        )

        return Result.success()
    }
}
