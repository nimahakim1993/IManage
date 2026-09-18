package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.CarServiceTypeDao
import com.nima.app.imanage.data.db.entity.CarServiceTypeEntity
import kotlinx.coroutines.flow.Flow

class CarServiceTypeRepository(
    private val dao: CarServiceTypeDao
) {
    suspend fun insert(type: CarServiceTypeEntity) = dao.insert(type)
    suspend fun update(type: CarServiceTypeEntity) = dao.update(type)
    suspend fun delete(type: CarServiceTypeEntity) = dao.delete(type)
    fun getAll(): Flow<List<CarServiceTypeEntity>> = dao.getAll()
    suspend fun getById(id: Int): CarServiceTypeEntity? = dao.getById(id)
}
