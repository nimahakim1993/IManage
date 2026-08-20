package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.CheckCounterpartyDao
import com.nima.app.imanage.data.db.entity.CheckCounterpartyEntity
import kotlinx.coroutines.flow.Flow

class CheckCounterpartyRepository(private val dao: CheckCounterpartyDao) {
    fun getAll(): Flow<List<CheckCounterpartyEntity>> = dao.getAll()
    suspend fun insert(counterparty: CheckCounterpartyEntity) = dao.insert(counterparty)
    suspend fun update(counterparty: CheckCounterpartyEntity) = dao.update(counterparty)
    suspend fun delete(counterparty: CheckCounterpartyEntity) = dao.delete(counterparty)
}
