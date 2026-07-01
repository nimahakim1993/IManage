package com.nima.app.imanage.data.repository

import com.nima.app.imanage.data.db.dao.LoanDao
import com.nima.app.imanage.data.db.entity.LoanEntity

class LoanRepository(private val dao: LoanDao) {

    suspend fun insert(loanEntity: LoanEntity) = dao.insert(loanEntity)

    suspend fun update(loanEntity: LoanEntity) = dao.update(loanEntity)

    suspend fun delete(loanEntity: LoanEntity) = dao.delete(loanEntity)

    fun getAll() = dao.getAll()

    fun get(id: Int) = dao.get(id)

}