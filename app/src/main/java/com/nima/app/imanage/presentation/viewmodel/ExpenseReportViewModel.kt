package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.ExpenseCategoryEntity
import com.nima.app.imanage.data.db.entity.ExpenseEntity
import com.nima.app.imanage.data.db.entity.IncomeEntity
import com.nima.app.imanage.data.repository.ExpenseCategoryRepository
import com.nima.app.imanage.data.repository.ExpenseRepository
import com.nima.app.imanage.data.repository.IncomeRepository
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.util.ShamsiDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategoryAmount(
    val categoryId: Int?,
    val categoryName: String,
    val colorIndex: Int,
    val amount: Long,
    val percent: Float
)

data class MonthComparison(
    val currentMonthAmount: Long,
    val previousMonthAmount: Long,
    val sameMonthLastYearAmount: Long,
    val currentMonthLabel: String,
    val previousMonthLabel: String,
    val lastYearMonthLabel: String
)

data class CategoryComparison(
    val categoryName: String,
    val colorIndex: Int,
    val currentMonthAmount: Long,
    val lastMonthAmount: Long
)

data class MonthlyTrend(
    val month: Int,
    val year: Int,
    val amount: Long
)

data class ExpenseReportData(
    val totalExpenses: Long,
    val totalIncomes: Long,
    val categoryBreakdown: List<CategoryAmount>,
    val expenseIncomeRatio: Pair<Long, Long>,
    val monthComparison: MonthComparison,
    val categoryComparison: List<CategoryComparison>,
    val monthlyTrend: List<MonthlyTrend>
)

class ExpenseReportViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: ExpenseCategoryRepository
) : ViewModel() {

    private val expenses = expenseRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<ExpenseEntity>())

    private val incomes = incomeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<IncomeEntity>())

    private val categories = categoryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<ExpenseCategoryEntity>())

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

    private val _comparisonMonth1 = MutableStateFlow<Pair<Int, Int>?>(null)
    val comparisonMonth1: StateFlow<Pair<Int, Int>?> = _comparisonMonth1.asStateFlow()

    private val _comparisonMonth2 = MutableStateFlow<Pair<Int, Int>?>(null)
    val comparisonMonth2: StateFlow<Pair<Int, Int>?> = _comparisonMonth2.asStateFlow()

    init {
        val (todayJy, todayJm, _) = ShamsiDate.today()
        val prevJm = if (todayJm > 1) todayJm - 1 else 12
        val prevJy = if (todayJm > 1) todayJy else todayJy - 1
        _comparisonMonth1.value = todayJm to todayJy
        _comparisonMonth2.value = prevJm to prevJy
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportData: StateFlow<ExpenseReportData> = combine(
        expenses, incomes, categories,
        _filterMode, _selectedMonthYear, _selectedYear,
        _customFromDate, _customToDate,
        _comparisonMonth1, _comparisonMonth2
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        computeReportData(
            expenses = values[0] as List<ExpenseEntity>,
            incomes = values[1] as List<IncomeEntity>,
            categories = values[2] as List<ExpenseCategoryEntity>,
            filterMode = values[3] as FilterMode,
            selectedMonthYear = values[4] as Pair<Int, Int>?,
            selectedYear = values[5] as Int?,
            customFrom = values[6] as Long?,
            customTo = values[7] as Long?,
            comparisonMonth1 = values[8] as Pair<Int, Int>?,
            comparisonMonth2 = values[9] as Pair<Int, Int>?
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseReportData(
            totalExpenses = 0,
            totalIncomes = 0,
            categoryBreakdown = emptyList(),
            expenseIncomeRatio = 0L to 0L,
            monthComparison = MonthComparison(0, 0, 0, "", "", ""),
            categoryComparison = emptyList(),
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

    fun setCategoryComparisonMonths(
        month1: Pair<Int, Int>?,
        month2: Pair<Int, Int>?
    ) {
        _comparisonMonth1.value = month1
        _comparisonMonth2.value = month2
    }

    private fun computeReportData(
        expenses: List<ExpenseEntity>,
        incomes: List<IncomeEntity>,
        categories: List<ExpenseCategoryEntity>,
        filterMode: FilterMode,
        selectedMonthYear: Pair<Int, Int>?,
        selectedYear: Int?,
        customFrom: Long?,
        customTo: Long?,
        comparisonMonth1: Pair<Int, Int>?,
        comparisonMonth2: Pair<Int, Int>?
    ): ExpenseReportData {
        val (currentJy, currentJm, _) = ShamsiDate.today()

        fun isInRange(
            timestamp: Long,
            mode: FilterMode,
            my: Pair<Int, Int>?,
            yr: Int?,
            from: Long?,
            to: Long?
        ): Boolean {
            return when (mode) {
                FilterMode.CURRENT_YEAR -> {
                    val (jy, _, _) = ShamsiDate.fromMillis(timestamp)
                    jy == currentJy
                }

                FilterMode.MONTHLY -> {
                    my?.let { (month, year) ->
                        val (jy, jm, _) = ShamsiDate.fromMillis(timestamp)
                        jy == year && jm == month
                    } ?: true
                }

                FilterMode.YEARLY -> {
                    yr?.let { year ->
                        val (jy, _, _) = ShamsiDate.fromMillis(timestamp)
                        jy == year
                    } ?: true
                }

                FilterMode.CUSTOM -> {
                    val f = from ?: return true
                    val t = to ?: return true
                    timestamp in f..t
                }
            }
        }

        val filteredExpenses = expenses.filter {
            isInRange(
                it.createdAt,
                filterMode,
                selectedMonthYear,
                selectedYear,
                customFrom,
                customTo
            )
        }
        val filteredIncomes = incomes.filter {
            isInRange(
                it.incomeDate,
                filterMode,
                selectedMonthYear,
                selectedYear,
                customFrom,
                customTo
            )
        }

        val totalExpenses = filteredExpenses.sumOf { it.amount }
        val totalIncomes = filteredIncomes.sumOf { it.amount }

        val categoryMap = mutableMapOf<Int?, Long>()
        filteredExpenses.forEach { exp ->
            categoryMap[exp.categoryId] = (categoryMap[exp.categoryId] ?: 0) + exp.amount
        }

        val categoryBreakdown = categoryMap.map { (catId, amount) ->
            val cat = categories.firstOrNull { it.id == catId }
            val percent =
                if (totalExpenses > 0) (amount.toFloat() / totalExpenses.toFloat()) * 100f else 0f
            CategoryAmount(
                categoryId = catId,
                categoryName = cat?.title ?: "—",
                colorIndex = cat?.colorIndex ?: (categories.size),
                amount = amount,
                percent = percent
            )
        }.sortedByDescending { it.amount }

        val prevJm = if (currentJm > 1) currentJm - 1 else 12
        val prevJy = if (currentJm > 1) currentJy else currentJy - 1
        val lastYearJy = currentJy - 1

        val currentMonthExpenses = expenses.filter {
            val (jy, jm, _) = ShamsiDate.fromMillis(it.createdAt)
            jy == currentJy && jm == currentJm
        }.sumOf { it.amount }

        val sameMonthLastYearExpenses = expenses.filter {
            val (jy, jm, _) = ShamsiDate.fromMillis(it.createdAt)
            jy == lastYearJy && jm == currentJm
        }.sumOf { it.amount }

        val previousMonthExpenses = expenses.filter {
            val (jy, jm, _) = ShamsiDate.fromMillis(it.createdAt)
            jy == prevJy && jm == prevJm
        }.sumOf { it.amount }

        val currentMonthLabel = "${ShamsiDate.getMonthName(currentJm)} امسال"
        val lastYearMonthLabel = "${ShamsiDate.getMonthName(currentJm)} پارسال"
        val previousMonthLabel = "${ShamsiDate.getMonthName(prevJm)} امسال"

        val monthComparison = MonthComparison(
            currentMonthAmount = currentMonthExpenses,
            previousMonthAmount = previousMonthExpenses,
            sameMonthLastYearAmount = sameMonthLastYearExpenses,
            currentMonthLabel = currentMonthLabel,
            previousMonthLabel = previousMonthLabel,
            lastYearMonthLabel = lastYearMonthLabel
        )

        val month1Cats = mutableMapOf<Int?, Long>()
        comparisonMonth1?.let { (month1, year1) ->
            expenses.filter {
                val (jy, jm, _) = ShamsiDate.fromMillis(it.createdAt)
                jy == year1 && jm == month1
            }.forEach { exp ->
                month1Cats[exp.categoryId] = (month1Cats[exp.categoryId] ?: 0) + exp.amount
            }
        }

        val month2Cats = mutableMapOf<Int?, Long>()
        comparisonMonth2?.let { (month2, year2) ->
            expenses.filter {
                val (jy, jm, _) = ShamsiDate.fromMillis(it.createdAt)
                jy == year2 && jm == month2
            }.forEach { exp ->
                month2Cats[exp.categoryId] = (month2Cats[exp.categoryId] ?: 0) + exp.amount
            }
        }

        val allCatIds = (month1Cats.keys + month2Cats.keys).filterNotNull().distinct()
        val categoryComparison = allCatIds.map { catId ->
            val cat = categories.firstOrNull { it.id == catId }
            CategoryComparison(
                categoryName = cat?.title ?: "—",
                colorIndex = cat?.colorIndex ?: 0,
                currentMonthAmount = month1Cats[catId] ?: 0,
                lastMonthAmount = month2Cats[catId] ?: 0
            )
        }.sortedByDescending { it.currentMonthAmount + it.lastMonthAmount }

        val monthlyTrendMap = mutableMapOf<Pair<Int, Int>, Long>()
        val yearToFilter = when (filterMode) {
            FilterMode.YEARLY -> selectedYear ?: currentJy
            FilterMode.CURRENT_YEAR -> currentJy
            else -> currentJy
        }
        expenses.filter {
            val (jy, _, _) = ShamsiDate.fromMillis(it.createdAt)
            jy == yearToFilter
        }.forEach { exp ->
            val (jy, jm, _) = ShamsiDate.fromMillis(exp.createdAt)
            val key = Pair(jm, jy)
            monthlyTrendMap[key] = (monthlyTrendMap[key] ?: 0) + exp.amount
        }

        val monthlyTrend = (1..12).map { month ->
            val amount = monthlyTrendMap[Pair(month, yearToFilter)] ?: 0L
            MonthlyTrend(month = month, year = yearToFilter, amount = amount)
        }

        return ExpenseReportData(
            totalExpenses = totalExpenses,
            totalIncomes = totalIncomes,
            categoryBreakdown = categoryBreakdown,
            expenseIncomeRatio = totalExpenses to totalIncomes,
            monthComparison = monthComparison,
            categoryComparison = categoryComparison,
            monthlyTrend = monthlyTrend
        )
    }
}
