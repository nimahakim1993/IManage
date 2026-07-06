package com.nima.app.imanage.domain.calculator

import kotlin.math.roundToLong

data class ParticipantBalance(
    val participantId: Int,
    val participantName: String,
    val totalPaid: Double,
    val totalShare: Double,
    val netBalance: Double
)

data class SettlementTransaction(
    val fromParticipantId: Int,
    val fromParticipantName: String,
    val toParticipantId: Int,
    val toParticipantName: String,
    val amount: Double
)

object BalanceCalculator {

    fun calculateBalances(
        participantIds: Map<Int, String>,
        expenses: List<ExpenseData>,
        splits: Map<Int, List<ExpenseSplitData>>,
        settlements: List<SettlementData>
    ): List<ParticipantBalance> {
        val paid = mutableMapOf<Int, Double>()
        val share = mutableMapOf<Int, Double>()

        participantIds.keys.forEach { id ->
            paid[id] = 0.0
            share[id] = 0.0
        }

        for (expense in expenses) {
            val payerId = expense.payerParticipantId
            paid[payerId] = (paid[payerId] ?: 0.0) + expense.amount

            val expenseSplits = splits[expense.id] ?: emptyList()
            for (split in expenseSplits) {
                share[split.participantId] = (share[split.participantId] ?: 0.0) + split.amount
            }
        }

        for (settlement in settlements) {
            paid[settlement.fromParticipantId] =
                (paid[settlement.fromParticipantId] ?: 0.0) + settlement.amount
            share[settlement.fromParticipantId] =
                (share[settlement.fromParticipantId] ?: 0.0) + settlement.amount
        }

        return participantIds.map { (id, name) ->
            val totalPaid = round(paid[id] ?: 0.0)
            val totalShare = round(share[id] ?: 0.0)
            val netBalance = round(totalPaid - totalShare)
            ParticipantBalance(
                participantId = id,
                participantName = name,
                totalPaid = totalPaid,
                totalShare = totalShare,
                netBalance = netBalance
            )
        }
    }

    private fun round(value: Double): Double = (value * 100).roundToLong() / 100.0
}

data class ExpenseData(
    val id: Int,
    val amount: Double,
    val payerParticipantId: Int
)

data class ExpenseSplitData(
    val participantId: Int,
    val amount: Double
)

data class SettlementData(
    val fromParticipantId: Int,
    val toParticipantId: Int,
    val amount: Double
)
