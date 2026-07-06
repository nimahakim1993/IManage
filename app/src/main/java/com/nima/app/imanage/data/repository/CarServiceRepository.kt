package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.CarServiceDao
import com.nima.app.imanage.data.db.entity.CarServiceEntity

class CarServiceRepository(private val dao: CarServiceDao) {

    suspend fun insert(carServiceEntity: CarServiceEntity) = dao.insert(carServiceEntity)

    suspend fun update(carServiceEntity: CarServiceEntity) = dao.update(carServiceEntity)

    suspend fun delete(carServiceEntity: CarServiceEntity) = dao.delete(carServiceEntity)

    fun getAll() = dao.getAll()

    fun get(id: Int) = dao.get(id)
}
