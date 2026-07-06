package com.nima.app.imanage.domain.strategy

interface SplitStrategy {
    fun calculateShares(
        totalAmount: Double,
        participantCount: Int,
        params: Map<String, Double> = emptyMap()
    ): List<Double>
}
