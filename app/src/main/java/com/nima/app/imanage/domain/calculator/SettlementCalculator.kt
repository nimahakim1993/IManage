package com.nima.app.imanage.domain.calculator

import kotlin.math.abs
import kotlin.math.roundToLong

object SettlementCalculator {

    fun calculateSettlements(balances: List<ParticipantBalance>): List<SettlementTransaction> {
        val creditors = balances
            .filter { it.netBalance > 0.001 }
            .sortedByDescending { it.netBalance }
            .toMutableList()

        val debtors = balances
            .filter { it.netBalance < -0.001 }
            .map { it.copy(netBalance = abs(it.netBalance)) }
            .sortedByDescending { it.netBalance }
            .toMutableList()

        val transactions = mutableListOf<SettlementTransaction>()

        var ci = 0
        var di = 0
        while (ci < creditors.size && di < debtors.size) {
            val creditor = creditors[ci]
            val debtor = debtors[di]
            val amount = round(minOf(creditor.netBalance, debtor.netBalance))

            if (amount > 0.0) {
                transactions.add(
                    SettlementTransaction(
                        fromParticipantId = debtor.participantId,
                        fromParticipantName = debtor.participantName,
                        toParticipantId = creditor.participantId,
                        toParticipantName = creditor.participantName,
                        amount = amount
                    )
                )
            }

            val newCreditorBalance = round(creditor.netBalance - amount)
            val newDebtorBalance = round(debtor.netBalance - amount)

            if (newCreditorBalance < 0.001) {
                ci++
            } else {
                creditors[ci] = creditor.copy(netBalance = newCreditorBalance)
            }

            if (newDebtorBalance < 0.001) {
                di++
            } else {
                debtors[di] = debtor.copy(netBalance = newDebtorBalance)
            }
        }

        return transactions
    }

    private fun round(value: Double): Double = (value * 100).roundToLong() / 100.0
}
