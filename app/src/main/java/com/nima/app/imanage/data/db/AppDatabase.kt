package com.nima.app.imanage.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nima.app.imanage.data.db.dao.BankCardDao
import com.nima.app.imanage.data.db.dao.ExpenseCategoryDao
import com.nima.app.imanage.data.db.dao.ExpenseDao
import com.nima.app.imanage.data.db.dao.InstallmentDao
import com.nima.app.imanage.data.db.dao.InstallmentItemDao
import com.nima.app.imanage.data.db.dao.LoanDao
import com.nima.app.imanage.data.db.dao.IncomeDao
import com.nima.app.imanage.data.db.dao.IncomeSourceDao
import com.nima.app.imanage.data.db.dao.NoteBoxDao
import com.nima.app.imanage.data.db.dao.NoteDao
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import com.nima.app.imanage.data.db.entity.IncomeEntity
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import com.nima.app.imanage.data.db.entity.NoteEntity


@Database(
    entities = [
        BankCardEntity::class,
        LoanEntity::class,
        NoteBoxEntity::class,
        NoteEntity::class,
        ExpenseCategoryEntity::class,
        ExpenseEntity::class,
        IncomeSourceEntity::class,
        IncomeEntity::class,
        InstallmentEntity::class,
        InstallmentItemEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankCardDao(): BankCardDao
    abstract fun loanDao(): LoanDao
    abstract fun noteBoxDao(): NoteBoxDao
    abstract fun noteDao(): NoteDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeSourceDao(): IncomeSourceDao
    abstract fun incomeDao(): IncomeDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun installmentItemDao(): InstallmentItemDao
}