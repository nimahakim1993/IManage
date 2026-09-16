package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.CheckEntity
import com.nima.app.imanage.data.repository.CheckRepository
import com.nima.app.imanage.domain.model.FilterMode
import com.nima.app.imanage.util.ShamsiDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ChecksReportData(
    val totalReceived: Long,
    val totalPayable: Long,
    val checkCount: Int,
    val yearlyComparison: List<YearlyCheckData>,
    val counterpartyBreakdown: List<CounterpartyCheckData>,
    val stateBreakdown: List<StateCheckData>
)

data class YearlyCheckData(
    val year: Int,
    val receivedAmount: Long,
    val payableAmount: Long
)

data class CounterpartyCheckData(
    val counterparty: String,
    val receivedAmount: Long,
    val payableAmount: Long,
    val totalAmount: Long,
    val percent: Float
)

data class StateCheckData(
    val state: String,
    val count: Int,
    val amount: Long,
    val percent: Float
)

class ChecksReportViewModel(
    private val checkRepository: CheckRepository
) : ViewModel() {

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
    val reportData: StateFlow<ChecksReportData> = combine(
        checks,
        _filterMode, _selectedMonthYear, _selectedYear,
        _customFromDate, _customToDate
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        computeReportData(
            checks = values[0] as List<CheckEntity>,
            filterMode = values[1] as FilterMode,
            selectedMonthYear = values[2] as Pair<Int, Int>?,
            selectedYear = values[3] as Int?,
            customFrom = values[4] as Long?,
            customTo = values[5] as Long?
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), ChecksReportData(
            totalReceived = 0,
            totalPayable = 0,
            checkCount = 0,
            yearlyComparison = emptyList(),
            counterpartyBreakdown = emptyList(),
            stateBreakdown = emptyList()
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

    private fun computeReportData(
        checks: List<CheckEntity>,
        filterMode: FilterMode,
        selectedMonthYear: Pair<Int, Int>?,
        selectedYear: Int?,
        customFrom: Long?,
        customTo: Long?
    ): ChecksReportData {
        val (currentJy, _, _) = ShamsiDate.today()

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

        val filteredChecks = checks.filter {
            isInRange(
                it.dueDate,
                filterMode,
                selectedMonthYear,
                selectedYear,
                customFrom,
                customTo
            )
        }

        val totalReceived = filteredChecks
            .filter { it.type == CheckEntity.TYPE_RECEIVED }
            .sumOf { it.amount }
        val totalPayable = filteredChecks
            .filter { it.type == CheckEntity.TYPE_PAYABLE }
            .sumOf { it.amount }

        val yearlyComparison = buildYearlyComparison(checks, currentJy)

        val counterpartyBreakdown =
            buildCounterpartyBreakdown(filteredChecks, totalReceived + totalPayable)

        val stateBreakdown = buildStateBreakdown(filteredChecks)

        return ChecksReportData(
            totalReceived = totalReceived,
            totalPayable = totalPayable,
            checkCount = filteredChecks.size,
            yearlyComparison = yearlyComparison,
            counterpartyBreakdown = counterpartyBreakdown,
            stateBreakdown = stateBreakdown
        )
    }

    private fun buildYearlyComparison(
        allChecks: List<CheckEntity>,
        currentYear: Int
    ): List<YearlyCheckData> {
        val years = (currentYear - 2)..(currentYear + 2)
        return years.map { year ->
            val yearChecks = allChecks.filter {
                val (jy, _, _) = ShamsiDate.fromMillis(it.dueDate)
                jy == year
            }
            val received = yearChecks
                .filter { it.type == CheckEntity.TYPE_RECEIVED }
                .sumOf { it.amount }
            val payable = yearChecks
                .filter { it.type == CheckEntity.TYPE_PAYABLE }
                .sumOf { it.amount }
            YearlyCheckData(
                year = year,
                receivedAmount = received,
                payableAmount = payable
            )
        }
    }

    private fun buildCounterpartyBreakdown(
        checks: List<CheckEntity>,
        totalAmount: Long
    ): List<CounterpartyCheckData> {
        val counterpartyMap = mutableMapOf<String, Pair<Long, Long>>()
        checks.forEach { check ->
            val (received, payable) = counterpartyMap[check.counterparty] ?: (0L to 0L)
            if (check.type == CheckEntity.TYPE_RECEIVED) {
                counterpartyMap[check.counterparty] = (received + check.amount) to payable
            } else {
                counterpartyMap[check.counterparty] = received to (payable + check.amount)
            }
        }

        return counterpartyMap.map { (counterparty, amounts) ->
            val (received, payable) = amounts
            val total = received + payable
            val percent =
                if (totalAmount > 0) (total.toFloat() / totalAmount.toFloat()) * 100f else 0f
            CounterpartyCheckData(
                counterparty = counterparty,
                receivedAmount = received,
                payableAmount = payable,
                totalAmount = total,
                percent = percent
            )
        }.sortedByDescending { it.totalAmount }
    }

    private fun buildStateBreakdown(checks: List<CheckEntity>): List<StateCheckData> {
        val stateMap = mutableMapOf<String, Pair<Int, Long>>()
        checks.forEach { check ->
            val (count, amount) = stateMap[check.state] ?: (0 to 0L)
            stateMap[check.state] = (count + 1) to (amount + check.amount)
        }

        val totalCount = checks.size
        return stateMap.map { (state, data) ->
            val (count, amount) = data
            val percent =
                if (totalCount > 0) (count.toFloat() / totalCount.toFloat()) * 100f else 0f
            StateCheckData(
                state = state,
                count = count,
                amount = amount,
                percent = percent
            )
        }.sortedByDescending { it.count }
    }
}
