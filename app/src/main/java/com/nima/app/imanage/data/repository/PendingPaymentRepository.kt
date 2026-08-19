package com.nima.app.imanage.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.AppDatabase
import com.nima.app.imanage.data.db.dao.PendingPaymentDao
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import com.nima.app.imanage.data.db.entity.PendingPaymentEntity
import com.nima.app.imanage.util.LanguageManager
import kotlinx.coroutines.flow.Flow

class PendingPaymentRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val dao: PendingPaymentDao
) {
    suspend fun insert(payment: PendingPaymentEntity): Boolean = dao.insert(payment) != -1L

    fun getPending(): Flow<List<PendingPaymentEntity>> = dao.getPending()

    suspend fun getPendingOnce(hash: String): PendingPaymentEntity? = dao.getByHash(hash)

    suspend fun confirm(id: Int): Boolean = database.withTransaction {
        val payment = dao.getById(id) ?: return@withTransaction false
        if (payment.status != PendingPaymentEntity.STATUS_PENDING) return@withTransaction false
        val localizedContext = LanguageManager.wrap(context)

        database.expenseDao().insert(
            ExpenseEntity(
                title = localizedContext.getString(R.string.sms_card_payment),
                description = localizedContext.getString(
                    R.string.sms_imported_from_sender,
                    payment.sender
                ),
                amount = payment.amount,
                categoryId = null,
                createdAt = payment.receivedAt
            )
        )
        dao.updateStatus(id, PendingPaymentEntity.STATUS_CONFIRMED)
        true
    }

    suspend fun ignore(id: Int) {
        dao.updateStatus(id, PendingPaymentEntity.STATUS_IGNORED)
    }
}
