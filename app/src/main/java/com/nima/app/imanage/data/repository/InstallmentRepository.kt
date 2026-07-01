package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.InstallmentDao
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import kotlinx.coroutines.flow.Flow

class InstallmentRepository(
    private val dao: InstallmentDao
) {
    suspend fun insert(installment: InstallmentEntity): Long = dao.insert(installment)
    suspend fun update(installment: InstallmentEntity) = dao.update(installment)
    suspend fun delete(installment: InstallmentEntity) = dao.delete(installment)
    fun getAll(): Flow<List<InstallmentEntity>> = dao.getAll()
    fun get(id: Int): Flow<InstallmentEntity?> = dao.get(id)
}
