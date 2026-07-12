package com.nima.app.imanage.data.model

import com.nima.app.imanage.data.db.entity.AssetEntity
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.data.db.entity.CarServiceEntity
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import com.nima.app.imanage.data.db.entity.IncomeEntity
import com.nima.app.imanage.data.db.entity.IncomeSourceEntity
import com.nima.app.imanage.data.db.entity.InstallmentEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import com.nima.app.imanage.data.db.entity.NoteEntity
import com.nima.app.imanage.data.db.entity.ParticipantEntity
import com.nima.app.imanage.data.db.entity.SettlementEntity
import com.nima.app.imanage.data.db.entity.TripEntity
import com.nima.app.imanage.data.db.entity.TripExpenseEntity
import com.nima.app.imanage.data.db.entity.TripExpenseSplitEntity
import com.nima.app.imanage.data.db.entity.PasswordItemEntity

data class BackupData(
    val version: Int = 1,
    val appVersion: String = "",
    val timestamp: Long = 0L,
    val settings: BackupSettings = BackupSettings(),
    val bankCards: List<BankCardEntity> = emptyList(),
    val loans: List<LoanEntity> = emptyList(),
    val noteBoxes: List<NoteBoxEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val expenseCategories: List<ExpenseCategoryEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val incomeSources: List<IncomeSourceEntity> = emptyList(),
    val incomes: List<IncomeEntity> = emptyList(),
    val installments: List<InstallmentEntity> = emptyList(),
    val installmentItems: List<InstallmentItemEntity> = emptyList(),
    val assets: List<AssetEntity> = emptyList(),
    val passwords: List<PasswordItemEntity> = emptyList(),
    val trips: List<TripEntity> = emptyList(),
    val participants: List<ParticipantEntity> = emptyList(),
    val tripExpenses: List<TripExpenseEntity> = emptyList(),
    val tripExpenseSplits: List<TripExpenseSplitEntity> = emptyList(),
    val settlements: List<SettlementEntity> = emptyList(),
    val carServices: List<CarServiceEntity> = emptyList()
)

data class BackupSettings(
    val language: String = "en",
    val theme: String = "system"
)
