package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.SettlementDao
import com.nima.app.imanage.data.db.entity.SettlementEntity

class SettlementRepository(private val dao: SettlementDao) {
    suspend fun insert(settlement: SettlementEntity) = dao.insert(settlement)
    suspend fun delete(settlement: SettlementEntity) = dao.delete(settlement)
    fun getByTrip(tripId: Int) = dao.getByTrip(tripId)
    suspend fun getByTripOnce(tripId: Int) = dao.getByTripOnce(tripId)
    suspend fun getOnce(id: Int) = dao.getOnce(id)
}
