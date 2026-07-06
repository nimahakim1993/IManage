package com.nima.app.imanage.domain.strategy

import kotlin.math.roundToLong

class EqualSplitStrategy : SplitStrategy {
    override fun calculateShares(
        totalAmount: Double,
        participantCount: Int,
        params: Map<String, Double>
    ): List<Double> {
        if (participantCount <= 0) return emptyList()
        val share = totalAmount / participantCount
        val roundedShare = (share * 100).roundToLong() / 100.0
        val shares = MutableList(participantCount) { roundedShare }
        val totalRounded = roundedShare * participantCount
        val remainder = ((totalAmount - totalRounded) * 100).roundToLong()
        for (i in 0 until remainder.toInt().coerceAtLeast(0)) {
            shares[i % participantCount] = (shares[i % participantCount] * 100 + 1) / 100.0
        }
        for (i in 0 until (-remainder).toInt().coerceAtLeast(0)) {
            shares[i % participantCount] = (shares[i % participantCount] * 100 - 1) / 100.0
        }
        return shares
    }
}
