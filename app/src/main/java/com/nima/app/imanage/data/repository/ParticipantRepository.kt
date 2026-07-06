package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.ParticipantDao
import com.nima.app.imanage.data.db.entity.ParticipantEntity

class ParticipantRepository(private val dao: ParticipantDao) {
    suspend fun insert(participant: ParticipantEntity) = dao.insert(participant)
    suspend fun update(participant: ParticipantEntity) = dao.update(participant)
    suspend fun delete(participant: ParticipantEntity) = dao.delete(participant)
    fun getByTrip(tripId: Int) = dao.getByTrip(tripId)
    suspend fun getByTripOnce(tripId: Int) = dao.getByTripOnce(tripId)
    fun get(id: Int) = dao.get(id)
    suspend fun getOnce(id: Int) = dao.getOnce(id)
}
