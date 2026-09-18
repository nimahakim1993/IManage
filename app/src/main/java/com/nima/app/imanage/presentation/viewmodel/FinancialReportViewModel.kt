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
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.util.ShamsiDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FinancialReportData(
    val moduleOverview: ModuleOverviewData,
    val payableBreakdown: PayableBreakdownData,
    val receivableBreakdown: ReceivableBreakdownData,
    val settledVsUnsettled: SettledVsUnsettledData,
    val monthlyTrend: List<MonthlyFinancialData>
)

data class ModuleOverviewData(
    val expense: Long,
    val income: Long,
    val debt: Long,
    val receivable: Long,
    val installment: Long,
    val payableChecks: Long,
    val receivedChecks: Long
)

data class PayableBreakdownData(
    val expenses: Long,
    val debtLoans: Long,
    val payableChecks: Long,
    val installments: Long
) {
    val total: Long get() = expenses + debtLoans + payableChecks + installments
}

data class ReceivableBreakdownData(
    val incomes: Long,
    val receivableLoans: Long,
    val receivedChecks: Long
) {
    val total: Long get() = incomes + receivableLoans + receivedChecks
}

data class SettledVsUnsettledData(
    val installmentSettled: Long,
    val installmentUnsettled: Long,
    val checkSettled: Long,
    val checkUnsettled: Long,
    val loanSettled: Long,
    val loanUnsettled: Long
)

class FinancialReportViewModel(
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

    private val _filterMode = MutableStateFlow(FilterMode.CURRENT_YEAR)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    private val _selectedMonthYear = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedMonthYear: StateFlow<Pair<Int, Int>?> = _selectedMonthYear.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _customFromDate = MutableStateFlow<Long?>(null)
    val customFromDate: StateFlow<Long?> = _customFromDate.asStateFlow()

    private val _customToDate = MutableStateFlow<Long?>(null)
    val customToDate: StateFlow<Long?> = _customToDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportData: StateFlow<FinancialReportData> = combine(
        expenses, incomes, loans, installmentItems, checks,
        _filterMode, _selectedMonthYear, _selectedYear, _customFromDate, _customToDate
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        computeReport(
            expenses = values[0] as List<ExpenseEntity>,
            incomes = values[1] as List<IncomeEntity>,
            loans = values[2] as List<LoanEntity>,
            installmentItems = values[3] as List<InstallmentItemEntity>,
            checks = values[4] as List<CheckEntity>,
            filterMode = values[5] as FilterMode,
            selectedMonthYear = values[6] as Pair<Int, Int>?,
            selectedYear = values[7] as Int?,
            customFrom = values[8] as Long?,
            customTo = values[9] as Long?
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FinancialReportData(
            moduleOverview = ModuleOverviewData(0, 0, 0, 0, 0, 0, 0),
            payableBreakdown = PayableBreakdownData(0, 0, 0, 0),
            receivableBreakdown = ReceivableBreakdownData(0, 0, 0),
            settledVsUnsettled = SettledVsUnsettledData(0, 0, 0, 0, 0, 0),
            monthlyTrend = emptyList()
        )
    )

    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
        when (mode) {
            FilterMode.CURRENT_YEAR -> {
                _selectedMonthYear.value = null
                _selectedYear.value = null
                _customFromDate.value = null
                _customToDate.value = null
            }

            FilterMode.MONTHLY -> {
                val today = ShamsiDate.today()
                _selectedMonthYear.value = Pair(today.second, today.first)
                _selectedYear.value = null
                _customFromDate.value = null
                _customToDate.value = null
            }

            FilterMode.YEARLY -> {
                _selectedYear.value = ShamsiDate.today().first
                _selectedMonthYear.value = null
                _customFromDate.value = null
                _customToDate.value = null
            }

            FilterMode.CUSTOM -> {
                val now = System.currentTimeMillis()
                _customFromDate.value = now
                _customToDate.value = now
            }
        }
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonthYear.value = Pair(month, year)
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
    }

    fun setCustomRange(from: Long, to: Long) {
        _customFromDate.value = from
        _customToDate.value = to
    }

    private fun computeReport(
        expenses: List<ExpenseEntity>,
        incomes: List<IncomeEntity>,
        loans: List<LoanEntity>,
        installmentItems: List<InstallmentItemEntity>,
        checks: List<CheckEntity>,
        filterMode: FilterMode,
        selectedMonthYear: Pair<Int, Int>?,
        selectedYear: Int?,
        customFrom: Long?,
        customTo: Long?
    ): FinancialReportData {
        val (currentJy, _, _) = ShamsiDate.today()

        fun isInRange(timestamp: Long): Boolean {
            return when (filterMode) {
                FilterMode.CURRENT_YEAR -> {
                    val (jy, _, _) = ShamsiDate.fromMillis(timestamp)
                    jy == currentJy
                }

                FilterMode.MONTHLY -> {
                    selectedMonthYear?.let { (month, year) ->
                        val (jy, jm, _) = ShamsiDate.fromMillis(timestamp)
                        jy == year && jm == month
                    } ?: true
                }

                FilterMode.YEARLY -> {
                    selectedYear?.let { year ->
                        val (jy, _, _) = ShamsiDate.fromMillis(timestamp)
                        jy == year
                    } ?: true
                }

                FilterMode.CUSTOM -> {
                    val f = customFrom ?: return true
                    val t = customTo ?: return true
                    timestamp in f..t
                }
            }
        }

        val filteredExpenses = expenses.filter { isInRange(it.createdAt) }
        val filteredIncomes = incomes.filter { isInRange(it.incomeDate) }

        val totalExpense = filteredExpenses.sumOf { it.amount }
        val totalIncome = filteredIncomes.sumOf { it.amount }

        val unsettledDebtLoans = loans.filter {
            it.type == LoanEntity.TYPE_DEBT && !it.settled
        }
        val unsettledReceivableLoans = loans.filter {
            it.type == LoanEntity.TYPE_RECEIVABLE && !it.settled
        }
        val settledLoans = loans.filter { it.settled }

        val totalDebt = unsettledDebtLoans.sumOf { it.price }
        val totalReceivable = unsettledReceivableLoans.sumOf { it.price }

        val unsettledInstallments = installmentItems.filter { !it.settled }
        val settledInstallments = installmentItems.filter { it.settled }
        val totalInstallmentUnsettled = unsettledInstallments.sumOf { it.amount }
        val totalInstallmentSettled = settledInstallments.sumOf { it.amount }

        val payableChecks = checks.filter {
            it.type == CheckEntity.TYPE_PAYABLE && !it.settled
        }
        val receivedChecks = checks.filter {
            it.type == CheckEntity.TYPE_RECEIVED && !it.settled
        }
        val settledChecks = checks.filter { it.settled }

        val totalPayableChecks = payableChecks.sumOf { it.amount }
        val totalReceivedChecks = receivedChecks.sumOf { it.amount }
        val totalCheckSettled = settledChecks.sumOf { it.amount }
        val totalCheckUnsettled = checks.filter { !it.settled }.sumOf { it.amount }

        val totalLoanSettled = settledLoans.sumOf { it.price }
        val totalLoanUnsettled = totalDebt + totalReceivable

        val moduleOverview = ModuleOverviewData(
            expense = totalExpense,
            income = totalIncome,
            debt = totalDebt,
            receivable = totalReceivable,
            installment = totalInstallmentUnsettled,
            payableChecks = totalPayableChecks,
            receivedChecks = totalReceivedChecks
        )

        val payableBreakdown = PayableBreakdownData(
            expenses = totalExpense,
            debtLoans = totalDebt,
            payableChecks = totalPayableChecks,
            installments = totalInstallmentUnsettled
        )

        val receivableBreakdown = ReceivableBreakdownData(
            incomes = totalIncome,
            receivableLoans = totalReceivable,
            receivedChecks = totalReceivedChecks
        )

        val settledVsUnsettled = SettledVsUnsettledData(
            installmentSettled = totalInstallmentSettled,
            installmentUnsettled = totalInstallmentUnsettled,
            checkSettled = totalCheckSettled,
            checkUnsettled = totalCheckUnsettled,
            loanSettled = totalLoanSettled,
            loanUnsettled = totalLoanUnsettled
        )

        val yearToFilter = when (filterMode) {
            FilterMode.YEARLY -> selectedYear ?: currentJy
            FilterMode.CURRENT_YEAR -> currentJy
            else -> currentJy
        }

        val monthlyTrend = (1..12).map { month ->
            val monthExpenses = filteredExpenses.filter {
                val (jy, jm, _) = ShamsiDate.fromMillis(it.createdAt)
                jy == yearToFilter && jm == month
            }.sumOf { it.amount }

            val monthIncomes = filteredIncomes.filter {
                val (jy, jm, _) = ShamsiDate.fromMillis(it.incomeDate)
                jy == yearToFilter && jm == month
            }.sumOf { it.amount }

            MonthlyFinancialData(
                month = month,
                year = yearToFilter,
                expense = monthExpenses,
                income = monthIncomes
            )
        }

        return FinancialReportData(
            moduleOverview = moduleOverview,
            payableBreakdown = payableBreakdown,
            receivableBreakdown = receivableBreakdown,
            settledVsUnsettled = settledVsUnsettled,
            monthlyTrend = monthlyTrend
        )
    }
}
