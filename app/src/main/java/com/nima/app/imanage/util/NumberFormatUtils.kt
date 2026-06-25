package com.nima.app.imanage.util

import java.text.DecimalFormat

object NumberFormatUtils {

    private val formatter = DecimalFormat("#,###")

    fun format(value: Long): String = formatter.format(value)

    fun format(value: Int): String = formatter.format(value.toLong())

    fun applySeparator(input: String): String {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return formatter.format(digits.toLong())
    }

    fun parseToLong(input: String): Long =
        input.filter { it.isDigit() }.toLongOrNull() ?: 0L
}
