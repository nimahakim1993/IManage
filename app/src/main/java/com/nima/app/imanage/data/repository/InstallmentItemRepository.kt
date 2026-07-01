package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.InstallmentItemDao
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import kotlinx.coroutines.flow.Flow

class InstallmentItemRepository(
    private val dao: InstallmentItemDao
) {
    suspend fun insert(item: InstallmentItemEntity) = dao.insert(item)
    suspend fun update(item: InstallmentItemEntity) = dao.update(item)
    suspend fun delete(item: InstallmentItemEntity) = dao.delete(item)
    fun getByInstallmentId(installmentId: Int): Flow<List<InstallmentItemEntity>> =
        dao.getByInstallmentId(installmentId)
    suspend fun getByInstallmentIdOnce(installmentId: Int): List<InstallmentItemEntity> =
        dao.getByInstallmentIdOnce(installmentId)

    fun getAllItems(): Flow<List<InstallmentItemEntity>> = dao.getAllItems()

    suspend fun deleteByInstallmentId(installmentId: Int) = dao.deleteByInstallmentId(installmentId)
}
