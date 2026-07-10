package com.nima.app.imanage.domain.model

data class ReportData(
    val totalExpenses: Long = 0,
    val totalIncomes: Long = 0,
    val totalDebt: Long = 0,
    val totalReceivable: Long = 0,
    val totalTripExpenses: Long = 0,
    val totalCarExpenses: Long = 0,
    val totalInstallments: Long = 0,
    val totalAssetValue: Long = 0,
    val expenseCount: Int = 0,
    val incomeCount: Int = 0,
    val debtCount: Int = 0,
    val receivableCount: Int = 0,
    val tripCount: Int = 0,
    val bankCardCount: Int = 0,
    val carServiceCount: Int = 0,
    val installmentCount: Int = 0,
    val assetCount: Int = 0,
    val netBalance: Long = 0,
    val monthlyExpenses: List<MonthAmount> = emptyList(),
    val monthlyIncomes: List<MonthAmount> = emptyList()
)

data class MonthAmount(
    val month: Int,
    val year: Int,
    val amount: Long
)

enum class FilterMode {
    CURRENT_YEAR,
    MONTHLY,
    YEARLY,
    CUSTOM
}
