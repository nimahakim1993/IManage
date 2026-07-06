package com.nima.app.imanage.domain.model

enum class SplitType(val value: Int) {
    EQUAL(0),
    PERCENTAGE(1),
    CUSTOM_AMOUNT(2),
    SHARES(3);

    companion object {
        fun fromValue(value: Int): SplitType = entries.first { it.value == value }
    }
}
