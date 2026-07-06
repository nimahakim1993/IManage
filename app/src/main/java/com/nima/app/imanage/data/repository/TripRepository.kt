package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.TripDao
import com.nima.app.imanage.data.db.entity.TripEntity

class TripRepository(private val dao: TripDao) {
    suspend fun insert(trip: TripEntity) = dao.insert(trip)
    suspend fun update(trip: TripEntity) = dao.update(trip)
    suspend fun delete(trip: TripEntity) = dao.delete(trip)
    fun getAll() = dao.getAll()
    fun get(id: Int) = dao.get(id)
    suspend fun getOnce(id: Int) = dao.getOnce(id)
    suspend fun getLastInserted() = dao.getLastInserted()
}
