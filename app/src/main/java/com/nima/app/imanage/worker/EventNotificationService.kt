package com.nima.app.imanage.worker

import com.nima.app.imanage.data.db.dao.CarServiceDao
import com.nima.app.imanage.data.db.dao.CarServiceTypeDao
import com.nima.app.imanage.data.db.dao.CheckDao
import com.nima.app.imanage.data.db.dao.InstallmentDao
import com.nima.app.imanage.data.db.dao.InstallmentItemDao
import com.nima.app.imanage.data.db.dao.LoanDao
import com.nima.app.imanage.util.NotificationHelper
import com.nima.app.imanage.util.ShamsiDate

class EventNotificationService(
    private val checkDao: CheckDao,
    private val loanDao: LoanDao,
    private val itemDao: InstallmentItemDao,
    private val carDao: CarServiceDao,
    private val carServiceTypeDao: CarServiceTypeDao,
    private val installmentDao: InstallmentDao,
    private val notificationHelper: NotificationHelper
) {
    suspend fun notifyEvents(mode: String) {
        val todayStart = ShamsiDate.todayMillis()

        if (mode == MODE_DAY_BEFORE) {
            val tomorrowStart = todayStart + DAY_MS
            notificationHelper.showCheckDayBeforeNotification(
                checkDao.getDueBetween(tomorrowStart, tomorrowStart + DAY_MS)
            )
            return
        }

        val todayEnd = todayStart + DAY_MS
        val dueLoans = loanDao.getLoanDateBetween(todayStart, todayEnd)
        val settlementLoans = loanDao.getUnsettledDueBetween(todayStart, todayEnd)
        val items = itemDao.getUnsettledDueBetween(todayStart, todayEnd)
        val serviceDateCarServices = carDao.getServiceDateBetween(todayStart, todayEnd)
        val nextServiceCarServices = carDao.getNextServiceDueBetween(todayStart, todayEnd)
        val carServiceTypes = carServiceTypeDao.getAllOnce()
        val carTypeMap = carServiceTypes.associateBy { it.id }
        val dueChecks = checkDao.getDueBetween(todayStart, todayEnd)

        val installmentTitles = buildList {
            for (item in items) {
                val installment = installmentDao.getById(item.installmentId)
                if (installment != null) add(item to installment.title)
            }
        }

        notificationHelper.showReminderNotification(
            dueLoans = dueLoans,
            settlementLoans = settlementLoans,
            installmentItems = installmentTitles,
            serviceDateCarServices = serviceDateCarServices,
            nextServiceCarServices = nextServiceCarServices,
            dueChecks = dueChecks,
            carTypeMap = carTypeMap
        )
    }

    companion object {
        private const val DAY_MS = 86_400_000L
        const val MODE_DUE_TODAY = "due_today"
        const val MODE_DAY_BEFORE = "day_before"
    }
}
