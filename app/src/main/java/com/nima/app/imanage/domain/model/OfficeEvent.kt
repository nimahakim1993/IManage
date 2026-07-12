package com.nima.app.imanage.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class OfficeEvent(
    val id: String,
    val title: String,
    val amount: Long?,
    val type: EventType,
    val icon: ImageVector,
    val color: Color,
    val date: Long,
    val serviceType: Int? = null,
    val loanType: Int? = null,
    val isSettlementDue: Boolean = false
)

enum class EventType {
    EXPENSE,
    INCOME,
    LOAN,
    TRIP,
    CAR_SERVICE,
    INSTALLMENT
}
