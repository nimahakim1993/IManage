package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.BankCardDao
import com.nima.app.imanage.data.db.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow

class BankCardRepository(
    private val dao: BankCardDao
) {
    suspend fun insert(card: BankCardEntity) = dao.insert(card)

    suspend fun delete(card: BankCardEntity) = dao.delete(card)

    fun getAll(): Flow<List<BankCardEntity>> = dao.observeAll()

    fun get(cardId: Int): Flow<BankCardEntity?> = dao.get(cardId)
}