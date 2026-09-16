package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import com.nima.app.imanage.data.db.entity.IncomeEntity
import com.nima.app.imanage.data.db.entity.InstallmentItemEntity
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.repository.CheckRepository
import com.nima.app.imanage.data.repository.ExpenseRepository
import com.nima.app.imanage.data.repository.IncomeRepository
import com.nima.app.imanage.data.repository.InstallmentItemRepository
import com.nima.app.imanage.data.repository.LoanRepository
import com.nima.app.imanage.util.ShamsiDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MonthlyFinancialData(
    val month: Int,
    val year: Int,
    val expense: Long,
    val income: Long
)

data class FinancialModuleData(
    val expense: Long,
    val income: Long,
    val debt: Long,
    val receivable: Long,
    val installment: Long,
    val check: Long
)

data class FinancialSummaryData(
    val monthlyData: List<MonthlyFinancialData>,
    val moduleData: FinancialModuleData
)

class FinancialSummaryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val loanRepository: LoanRepository,
    private val installmentItemRepository: InstallmentItemRepository,
    private val checkRepository: CheckRepository
) : ViewModel() {

    private val expenses = expenseRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<ExpenseEntity>())

    private val incomes = incomeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<IncomeEntity>())

    private val loans = loanRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<LoanEntity>())

    private val installmentItems = installmentItemRepository.getAllItems()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList<InstallmentItemEntity>()
        )

    private val checks = checkRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<CheckEntity>())

    val summaryData: StateFlow<FinancialSummaryData> = combine(
        expenses, incomes, loans, installmentItems, checks
    ) { expenseList, incomeList, loanList, installmentList, checkList ->
        computeSummary(expenseList, incomeList, loanList, installmentList, checkList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FinancialSummaryData(
            monthlyData = emptyList(),
            moduleData = FinancialModuleData(0, 0, 0, 0, 0, 0)
        )
    )

    private fun computeSummary(
        expenses: List<ExpenseEntity>,
        incomes: List<IncomeEntity>,
        loans: List<LoanEntity>,
        installmentItems: List<InstallmentItemEntity>,
        checks: List<CheckEntity>
    ): FinancialSummaryData {
        val (currentJy, _, _) = ShamsiDate.today()

        val monthlyMap = mutableMapOf<Int, Pair<Long, Long>>()

        for (month in 1..12) {
            val monthExpenses = expenses.filter {
                val (jy, jm, _) = ShamsiDate.fromMillis(it.createdAt)
                jy == currentJy && jm == month
            }.sumOf { it.amount }

            val monthIncomes = incomes.filter {
                val (jy, jm, _) = ShamsiDate.fromMillis(it.incomeDate)
                jy == currentJy && jm == month
            }.sumOf { it.amount }

            monthlyMap[month] = Pair(monthExpenses, monthIncomes)
        }

        val monthlyData = (1..12).map { month ->
            val (expense, income) = monthlyMap[month] ?: Pair(0L, 0L)
            MonthlyFinancialData(
                month = month,
                year = currentJy,
                expense = expense,
                income = income
            )
        }

        val totalExpense = expenses.filter {
            val (jy, _, _) = ShamsiDate.fromMillis(it.createdAt)
            jy == currentJy
        }.sumOf { it.amount }

        val totalIncome = incomes.filter {
            val (jy, _, _) = ShamsiDate.fromMillis(it.incomeDate)
            jy == currentJy
        }.sumOf { it.amount }

        val totalDebt = loans.filter {
            it.type == LoanEntity.TYPE_DEBT && !it.settled
        }.sumOf { it.price }

        val totalReceivable = loans.filter {
            it.type == LoanEntity.TYPE_RECEIVABLE && !it.settled
        }.sumOf { it.price }

        val totalInstallment = installmentItems.filter {
            !it.settled
        }.sumOf { it.amount }

        val totalCheck = checks.filter {
            !it.settled
        }.sumOf { it.amount }

        val moduleData = FinancialModuleData(
            expense = totalExpense,
            income = totalIncome,
            debt = totalDebt,
            receivable = totalReceivable,
            installment = totalInstallment,
            check = totalCheck
        )

        return FinancialSummaryData(
            monthlyData = monthlyData,
            moduleData = moduleData
        )
    }
}
