package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.AssetDao
import com.nima.app.imanage.data.db.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

class AssetRepository(private val dao: AssetDao) {
    suspend fun insert(asset: AssetEntity) = dao.insert(asset)
    suspend fun update(asset: AssetEntity) = dao.update(asset)
    suspend fun delete(asset: AssetEntity) = dao.delete(asset)
    fun getAll(): Flow<List<AssetEntity>> = dao.getAll()
    fun get(assetId: Int): Flow<AssetEntity?> = dao.get(assetId)
}
