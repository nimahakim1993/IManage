package com.nima.app.imanage.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.repository.CarServiceRepository
import com.nima.app.imanage.data.repository.ExpenseRepository
import com.nima.app.imanage.data.repository.IncomeRepository
import com.nima.app.imanage.data.repository.InstallmentItemRepository
import com.nima.app.imanage.data.repository.InstallmentRepository
import com.nima.app.imanage.data.repository.LoanRepository
import com.nima.app.imanage.data.repository.TripRepository
import com.nima.app.imanage.domain.model.EventType
import com.nima.app.imanage.domain.model.OfficeEvent
import com.nima.app.imanage.util.ShamsiDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class OfficeViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val loanRepository: LoanRepository,
    private val tripRepository: TripRepository,
    private val carServiceRepository: CarServiceRepository,
    private val installmentItemRepository: InstallmentItemRepository,
    private val installmentRepository: InstallmentRepository
) : ViewModel() {

    private val expenses = expenseRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val incomes = incomeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val loans = loanRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val trips = tripRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val carServices = carServiceRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val installmentItems = installmentItemRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val installments = installmentRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allEvents: StateFlow<List<OfficeEvent>> = combine(
        expenses, incomes, loans, trips, carServices, installmentItems, installments
    ) { values ->
        val expList = values[0] as List<com.nima.app.imanage.data.db.entity.ExpenseEntity>
        val incList = values[1] as List<com.nima.app.imanage.data.db.entity.IncomeEntity>
        val loanList = values[2] as List<LoanEntity>
        val tripList = values[3] as List<com.nima.app.imanage.data.db.entity.TripEntity>
        val carList = values[4] as List<com.nima.app.imanage.data.db.entity.CarServiceEntity>
        val instList = values[5] as List<com.nima.app.imanage.data.db.entity.InstallmentItemEntity>
        val instMap = (values[6] as List<com.nima.app.imanage.data.db.entity.InstallmentEntity>)
            .associateBy { it.id }

        buildList {
            expList.forEach { exp ->
                add(
                    OfficeEvent(
                        id = "expense_${exp.id}",
                        title = exp.title,
                        amount = exp.amount,
                        type = EventType.EXPENSE,
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFF44336),
                        date = exp.createdAt
                    )
                )
            }

            incList.forEach { inc ->
                add(
                    OfficeEvent(
                        id = "income_${inc.id}",
                        title = inc.title,
                        amount = inc.amount,
                        type = EventType.INCOME,
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4CAF50),
                        date = inc.incomeDate
                    )
                )
            }

            loanList.forEach { loan ->
                val isDebt = loan.type == LoanEntity.TYPE_DEBT
                add(
                    OfficeEvent(
                        id = "loan_${loan.id}",
                        title = loan.targetPersonName,
                        amount = loan.price,
                        type = EventType.LOAN,
                        icon = if (isDebt) Icons.Default.MoneyOff else Icons.Default.Payment,
                        color = if (isDebt) Color(0xFFFF9800) else Color(0xFF2196F3),
                        date = loan.dateLoan,
                        loanType = loan.type
                    )
                )
            }

            tripList.forEach { trip ->
                add(
                    OfficeEvent(
                        id = "trip_${trip.id}",
                        title = trip.name,
                        amount = null,
                        type = EventType.TRIP,
                        icon = Icons.Default.Groups,
                        color = Color(0xFF9C27B0),
                        date = trip.startDate
                    )
                )
            }

            carList.forEach { car ->
                add(
                    OfficeEvent(
                        id = "car_${car.id}",
                        title = "",
                        amount = car.amountPaid,
                        type = EventType.CAR_SERVICE,
                        icon = Icons.Default.DirectionsCar,
                        color = Color(0xFF795548),
                        date = car.serviceDate,
                        serviceType = car.serviceType
                    )
                )
                if (car.nextServiceDate > 0) {
                    add(
                        OfficeEvent(
                            id = "car_next_${car.id}",
                            title = "",
                            amount = null,
                            type = EventType.CAR_SERVICE,
                            icon = Icons.Default.DirectionsCar,
                            color = Color(0xFF795548),
                            date = car.nextServiceDate,
                            serviceType = car.serviceType
                        )
                    )
                }
            }

            instList.forEach { inst ->
                val instTitle = instMap[inst.installmentId]?.title ?: "Installment"
                add(
                    OfficeEvent(
                        id = "installment_${inst.id}",
                        title = instTitle,
                        amount = inst.amount,
                        type = EventType.INSTALLMENT,
                        icon = Icons.Default.AccountBalanceWallet,
                        color = Color(0xFF009688),
                        date = inst.dueDate
                    )
                )
            }
        }.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getEventsForDate(timestamp: Long): List<OfficeEvent> {
        val (targetYear, targetMonth, targetDay) = ShamsiDate.fromMillis(timestamp)
        return allEvents.value.filter { event ->
            val (eventYear, eventMonth, eventDay) = ShamsiDate.fromMillis(event.date)
            eventYear == targetYear && eventMonth == targetMonth && eventDay == targetDay
        }
    }

    fun getDaysWithEvents(year: Int, month: Int): Set<Int> {
        return allEvents.value.mapNotNull { event ->
            val (eventYear, eventMonth, eventDay) = ShamsiDate.fromMillis(event.date)
            if (eventYear == year && eventMonth == month) eventDay else null
        }.toSet()
    }
}
