package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.TripExpenseDao
import com.nima.app.imanage.data.db.entity.TripExpenseEntity

class TripExpenseRepository(private val dao: TripExpenseDao) {
    suspend fun insert(expense: TripExpenseEntity) = dao.insert(expense)
    suspend fun update(expense: TripExpenseEntity) = dao.update(expense)
    suspend fun delete(expense: TripExpenseEntity) = dao.delete(expense)
    fun getByTrip(tripId: Int) = dao.getByTrip(tripId)
    suspend fun getByTripOnce(tripId: Int) = dao.getByTripOnce(tripId)
    fun get(id: Int) = dao.get(id)
    suspend fun getOnce(id: Int) = dao.getOnce(id)
}
