package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.repository.AssetRepository
import com.nima.app.imanage.data.repository.BankCardRepository
import com.nima.app.imanage.data.repository.CarServiceRepository
import com.nima.app.imanage.data.repository.CheckRepository
import com.nima.app.imanage.data.repository.ExpenseRepository
import com.nima.app.imanage.data.repository.IncomeRepository
import com.nima.app.imanage.data.repository.InstallmentRepository
import com.nima.app.imanage.data.repository.LoanRepository
import com.nima.app.imanage.data.repository.NoteBoxRepository
import com.nima.app.imanage.data.repository.OfficeNoteRepository
import com.nima.app.imanage.data.repository.OfficeReminderRepository
import com.nima.app.imanage.data.repository.PasswordItemRepository
import com.nima.app.imanage.data.repository.TripExpenseRepository
import com.nima.app.imanage.data.repository.TripRepository
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.domain.model.MonthAmount
import com.nima.app.imanage.domain.model.ReportData
import com.nima.app.imanage.util.ShamsiDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ReportViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val loanRepository: LoanRepository,
    private val tripRepository: TripRepository,
    private val tripExpenseRepository: TripExpenseRepository,
    private val bankCardRepository: BankCardRepository,
    private val carServiceRepository: CarServiceRepository,
    private val installmentRepository: InstallmentRepository,
    private val assetRepository: AssetRepository,
    private val checkRepository: CheckRepository,
    private val passwordItemRepository: PasswordItemRepository,
    private val noteBoxRepository: NoteBoxRepository,
    private val officeNoteRepository: OfficeNoteRepository,
    private val officeReminderRepository: OfficeReminderRepository
) : ViewModel() {

    private val expenses = expenseRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val incomes = incomeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val loans = loanRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val trips = tripRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val tripExpenses = tripExpenseRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val bankCards = bankCardRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val carServices = carServiceRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val installments = installmentRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val assets = assetRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val checks = checkRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val passwords = passwordItemRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val noteBoxes = noteBoxRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val officeNotes = officeNoteRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val officeReminders = officeReminderRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
    val reportData: StateFlow<ReportData> = combine(
        expenses, incomes, loans, trips, tripExpenses,
        bankCards, carServices, installments, assets,
        checks, passwords, noteBoxes, officeNotes, officeReminders,
        _filterMode, _selectedMonthYear, _selectedYear,
        _customFromDate, _customToDate
    ) { values ->
        computeReportData(
            expenses = values[0] as List<com.nima.app.imanage.data.db.entity.ExpenseEntity>,
            incomes = values[1] as List<com.nima.app.imanage.data.db.entity.IncomeEntity>,
            loans = values[2] as List<LoanEntity>,
            trips = values[3] as List<com.nima.app.imanage.data.db.entity.TripEntity>,
            tripExpenses = values[4] as List<com.nima.app.imanage.data.db.entity.TripExpenseEntity>,
            bankCards = values[5] as List<com.nima.app.imanage.data.db.entity.BankCardEntity>,
            carServices = values[6] as List<com.nima.app.imanage.data.db.entity.CarServiceEntity>,
            installments = values[7] as List<com.nima.app.imanage.data.db.entity.InstallmentEntity>,
            assets = values[8] as List<com.nima.app.imanage.data.db.entity.AssetEntity>,
            checks = values[9] as List<CheckEntity>,
            passwords = values[10] as List<com.nima.app.imanage.data.db.entity.PasswordItemEntity>,
            noteBoxes = values[11] as List<com.nima.app.imanage.data.db.entity.NoteBoxEntity>,
            officeNotes = values[12] as List<com.nima.app.imanage.data.db.entity.OfficeNoteEntity>,
            officeReminders = values[13] as List<com.nima.app.imanage.data.db.entity.OfficeReminderEntity>,
            filterMode = values[14] as FilterMode,
            selectedMonthYear = values[15] as Pair<Int, Int>?,
            selectedYear = values[16] as Int?,
            customFrom = values[17] as Long?,
            customTo = values[18] as Long?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportData())

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

    @Suppress("UNCHECKED_CAST")
    private fun computeReportData(
        expenses: List<com.nima.app.imanage.data.db.entity.ExpenseEntity>,
        incomes: List<com.nima.app.imanage.data.db.entity.IncomeEntity>,
        loans: List<LoanEntity>,
        trips: List<com.nima.app.imanage.data.db.entity.TripEntity>,
        tripExpenses: List<com.nima.app.imanage.data.db.entity.TripExpenseEntity>,
        bankCards: List<com.nima.app.imanage.data.db.entity.BankCardEntity>,
        carServices: List<com.nima.app.imanage.data.db.entity.CarServiceEntity>,
        installments: List<com.nima.app.imanage.data.db.entity.InstallmentEntity>,
        assets: List<com.nima.app.imanage.data.db.entity.AssetEntity>,
        checks: List<CheckEntity>,
        passwords: List<com.nima.app.imanage.data.db.entity.PasswordItemEntity>,
        noteBoxes: List<com.nima.app.imanage.data.db.entity.NoteBoxEntity>,
        officeNotes: List<com.nima.app.imanage.data.db.entity.OfficeNoteEntity>,
        officeReminders: List<com.nima.app.imanage.data.db.entity.OfficeReminderEntity>,
        filterMode: FilterMode,
        selectedMonthYear: Pair<Int, Int>?,
        selectedYear: Int?,
        customFrom: Long?,
        customTo: Long?
    ): ReportData {
        val currentYear = ShamsiDate.today().first

        fun isInRange(timestamp: Long): Boolean {
            return when (filterMode) {
                FilterMode.CURRENT_YEAR -> {
                    val (jy, _, _) = ShamsiDate.fromMillis(timestamp)
                    jy == currentYear
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
                    val from = customFrom ?: return true
                    val to = customTo ?: return true
                    timestamp in from..to
                }
            }
        }

        val filteredExpenses = expenses.filter { isInRange(it.createdAt) }
        val filteredIncomes = incomes.filter { isInRange(it.incomeDate) }
        val filteredLoans = loans.filter { isInRange(it.dateLoan) }
        val filteredTrips = trips.filter { isInRange(it.startDate) }
        val filteredTripExpenses = tripExpenses.filter { isInRange(it.date) }
        val filteredCarServices = carServices.filter { isInRange(it.serviceDate) }
        val filteredInstallments = installments.filter { isInRange(it.startDate) }
        val filteredAssets = assets.filter { isInRange(it.createdAt) }
        val filteredChecks = checks.filter { isInRange(it.createdAt) }
        val filteredPasswords = passwords.filter { isInRange(it.createdAt) }
        val filteredNoteBoxes = noteBoxes.filter { isInRange(it.createdAt) }
        val filteredOfficeNotes = officeNotes.filter { isInRange(it.date) }
        val filteredOfficeReminders = officeReminders.filter { isInRange(it.date) }

        val totalExpenses = filteredExpenses.sumOf { it.amount }
        val totalIncomes = filteredIncomes.sumOf { it.amount }
        val totalDebt = filteredLoans.filter { it.type == LoanEntity.TYPE_DEBT && !it.settled }
            .sumOf { it.price }
        val totalReceivable =
            filteredLoans.filter { it.type == LoanEntity.TYPE_RECEIVABLE && !it.settled }
                .sumOf { it.price }
        val totalTripExpenses = filteredTripExpenses.sumOf { it.amount.toLong() }
        val totalCarExpenses = filteredCarServices.sumOf { it.amountPaid }
        val totalInstallments = filteredInstallments.sumOf { it.amount }
        val totalAssetValue = filteredAssets.sumOf { (it.unitCount * it.pricePerUnit).toLong() }
        val totalPayableChecks = filteredChecks.filter { it.type == CheckEntity.TYPE_PAYABLE }
            .sumOf { it.amount }
        val totalReceivedChecks = filteredChecks.filter { it.type == CheckEntity.TYPE_RECEIVED }
            .sumOf { it.amount }
        val netBalance = totalIncomes - totalExpenses - totalDebt + totalReceivable

        val monthlyExps = mutableMapOf<Pair<Int, Int>, Long>()
        val monthlyIncs = mutableMapOf<Pair<Int, Int>, Long>()

        filteredExpenses.forEach { exp ->
            val (jy, jm, _) = ShamsiDate.fromMillis(exp.createdAt)
            val key = Pair(jm, jy)
            monthlyExps[key] = (monthlyExps[key] ?: 0) + exp.amount
        }

        filteredIncomes.forEach { inc ->
            val (jy, jm, _) = ShamsiDate.fromMillis(inc.incomeDate)
            val key = Pair(jm, jy)
            monthlyIncs[key] = (monthlyIncs[key] ?: 0) + inc.amount
        }

        val monthlyExpensesList = monthlyExps.map { (k, v) -> MonthAmount(k.first, k.second, v) }
            .sortedWith(compareBy({ it.year }, { it.month }))
        val monthlyIncomesList = monthlyIncs.map { (k, v) -> MonthAmount(k.first, k.second, v) }
            .sortedWith(compareBy({ it.year }, { it.month }))

        return ReportData(
            totalExpenses = totalExpenses,
            totalIncomes = totalIncomes,
            totalDebt = totalDebt,
            totalReceivable = totalReceivable,
            totalTripExpenses = totalTripExpenses,
            totalCarExpenses = totalCarExpenses,
            totalInstallments = totalInstallments,
            totalAssetValue = totalAssetValue,
            totalPayableChecks = totalPayableChecks,
            totalReceivedChecks = totalReceivedChecks,
            expenseCount = filteredExpenses.size,
            incomeCount = filteredIncomes.size,
            debtCount = filteredLoans.count { it.type == LoanEntity.TYPE_DEBT && !it.settled },
            receivableCount = filteredLoans.count { it.type == LoanEntity.TYPE_RECEIVABLE && !it.settled },
            tripCount = filteredTrips.size,
            bankCardCount = bankCards.size,
            carServiceCount = filteredCarServices.size,
            installmentCount = filteredInstallments.size,
            assetCount = filteredAssets.size,
            passwordCount = filteredPasswords.size,
            noteBoxCount = filteredNoteBoxes.size,
            payableCheckCount = filteredChecks.count { it.type == CheckEntity.TYPE_PAYABLE },
            receivedCheckCount = filteredChecks.count { it.type == CheckEntity.TYPE_RECEIVED },
            officeNoteCount = filteredOfficeNotes.size,
            officeReminderCount = filteredOfficeReminders.size,
            netBalance = netBalance,
            monthlyExpenses = monthlyExpensesList,
            monthlyIncomes = monthlyIncomesList
        )
    }
}
