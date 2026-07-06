package com.nima.app.imanage.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.ParticipantEntity
import com.nima.app.imanage.data.db.entity.SettlementEntity
import com.nima.app.imanage.data.db.entity.TripExpenseEntity
import com.nima.app.imanage.data.db.entity.TripExpenseSplitEntity
import com.nima.app.imanage.data.repository.ParticipantRepository
import com.nima.app.imanage.data.repository.SettlementRepository
import com.nima.app.imanage.data.repository.TripExpenseRepository
import com.nima.app.imanage.data.repository.TripExpenseSplitRepository
import com.nima.app.imanage.data.repository.TripRepository
import com.nima.app.imanage.domain.calculator.BalanceCalculator
import com.nima.app.imanage.domain.calculator.ExpenseData
import com.nima.app.imanage.domain.calculator.ExpenseSplitData
import com.nima.app.imanage.domain.calculator.ParticipantBalance
import com.nima.app.imanage.domain.calculator.SettlementCalculator
import com.nima.app.imanage.domain.calculator.SettlementData
import com.nima.app.imanage.domain.calculator.SettlementTransaction
import com.nima.app.imanage.domain.model.SplitType
import com.nima.app.imanage.domain.strategy.EqualSplitStrategy
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TripDetailViewModel(
    private val tripRepository: TripRepository,
    private val participantRepository: ParticipantRepository,
    private val expenseRepository: TripExpenseRepository,
    private val splitRepository: TripExpenseSplitRepository,
    private val settlementRepository: SettlementRepository
) : ViewModel() {

    private val _participants = MutableStateFlow<List<ParticipantEntity>>(emptyList())
    val participants: StateFlow<List<ParticipantEntity>> = _participants.asStateFlow()

    private val _expenses = MutableStateFlow<List<TripExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<TripExpenseEntity>> = _expenses.asStateFlow()

    private val _settlements = MutableStateFlow<List<SettlementEntity>>(emptyList())
    val settlements: StateFlow<List<SettlementEntity>> = _settlements.asStateFlow()

    private val _balances = MutableStateFlow<List<ParticipantBalance>>(emptyList())
    val balances: StateFlow<List<ParticipantBalance>> = _balances.asStateFlow()

    private val _settlementTransactions = MutableStateFlow<List<SettlementTransaction>>(emptyList())
    val settlementTransactions: StateFlow<List<SettlementTransaction>> =
        _settlementTransactions.asStateFlow()

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _tripName = MutableStateFlow("")
    val tripName: StateFlow<String> = _tripName.asStateFlow()

    private var collectJob: Job? = null

    init {
        viewModelScope.launch {
            combine(_participants, _expenses, _settlements) { parts, exps, settles ->
                Triple(parts, exps, settles)
            }.collect { (parts, exps, settles) ->
                recalculate(parts, exps, settles)
            }
        }
    }

    fun loadTrip(tripId: Int) {
        if (tripId <= 0) return
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            launch { participantRepository.getByTrip(tripId).collect { _participants.value = it } }
            launch { expenseRepository.getByTrip(tripId).collect { _expenses.value = it } }
            launch { settlementRepository.getByTrip(tripId).collect { _settlements.value = it } }
            launch {
                tripRepository.get(tripId).collect { trip -> _tripName.value = trip?.name ?: "" }
            }
        }
    }

    private suspend fun recalculate(
        participants: List<ParticipantEntity>,
        expenses: List<TripExpenseEntity>,
        settlements: List<SettlementEntity>
    ) {
        if (participants.isEmpty()) {
            _balances.value = emptyList()
            _settlementTransactions.value = emptyList()
            _totalExpenses.value = 0.0
            return
        }

        val participantMap = participants.associate { it.id to it.name }
        val expenseIds = expenses.map { it.id }
        val allSplits =
            if (expenseIds.isNotEmpty()) splitRepository.getByExpenseIds(expenseIds) else emptyList()
        val splitsMap = allSplits.groupBy { it.expenseId }

        val expenseDataList = expenses.map {
            ExpenseData(id = it.id, amount = it.amount, payerParticipantId = it.payerParticipantId)
        }
        val settlementDataList = settlements.map {
            SettlementData(
                fromParticipantId = it.fromParticipantId,
                toParticipantId = it.toParticipantId,
                amount = it.amount
            )
        }

        val balances = BalanceCalculator.calculateBalances(
            participantIds = participantMap,
            expenses = expenseDataList,
            splits = splitsMap.mapValues { entry ->
                entry.value.map { s -> ExpenseSplitData(s.participantId, s.amount) }
            },
            settlements = settlementDataList
        )
        _balances.value = balances
        _settlementTransactions.value = SettlementCalculator.calculateSettlements(balances)
        _totalExpenses.value = expenses.sumOf { it.amount }
    }

    fun createExpense(
        title: String,
        amount: Double,
        date: Long,
        payerParticipantId: Int,
        description: String,
        splitType: SplitType,
        involvedParticipantIds: List<Int>
    ) {
        viewModelScope.launch {
            val tripId = _participants.value.firstOrNull()?.tripId ?: return@launch

            val expense = TripExpenseEntity(
                tripId = tripId,
                title = title,
                amount = amount,
                date = date,
                payerParticipantId = payerParticipantId,
                description = description,
                splitType = splitType.value
            )
            val expenseId = expenseRepository.insert(expense)

            val strategy = EqualSplitStrategy()
            val shares = strategy.calculateShares(amount, involvedParticipantIds.size)
            val splitEntities = involvedParticipantIds.mapIndexed { index, participantId ->
                TripExpenseSplitEntity(
                    expenseId = expenseId.toInt(),
                    participantId = participantId,
                    amount = kotlin.math.round(shares[index] * 100.0) / 100.0
                )
            }
            splitRepository.insertAll(splitEntities)
        }
    }

    fun updateExpense(
        expenseId: Int,
        title: String,
        amount: Double,
        date: Long,
        payerParticipantId: Int,
        description: String,
        splitType: SplitType,
        involvedParticipantIds: List<Int>
    ) {
        viewModelScope.launch {
            val existing = expenseRepository.getOnce(expenseId) ?: return@launch
            existing.title = title
            existing.amount = amount
            existing.date = date
            existing.payerParticipantId = payerParticipantId
            existing.description = description
            existing.splitType = splitType.value
            expenseRepository.update(existing)

            splitRepository.deleteByExpense(expenseId)

            val strategy = EqualSplitStrategy()
            val shares = strategy.calculateShares(amount, involvedParticipantIds.size)
            val splitEntities = involvedParticipantIds.mapIndexed { index, participantId ->
                TripExpenseSplitEntity(
                    expenseId = expenseId,
                    participantId = participantId,
                    amount = kotlin.math.round(shares[index] * 100.0) / 100.0
                )
            }
            splitRepository.insertAll(splitEntities)
        }
    }

    fun deleteExpense(expense: TripExpenseEntity) {
        viewModelScope.launch { expenseRepository.delete(expense) }
    }

    fun recordSettlement(
        fromParticipantId: Int,
        toParticipantId: Int,
        amount: Double,
        date: Long,
        note: String
    ) {
        viewModelScope.launch {
            val tripId = _participants.value.firstOrNull()?.tripId ?: return@launch
            val settlement = SettlementEntity(
                tripId = tripId,
                fromParticipantId = fromParticipantId,
                toParticipantId = toParticipantId,
                amount = amount,
                date = date,
                note = note
            )
            settlementRepository.insert(settlement)
        }
    }

    fun deleteSettlement(settlement: SettlementEntity) {
        viewModelScope.launch { settlementRepository.delete(settlement) }
    }
}
