package com.nima.app.imanage.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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

    private val persianSymbols = DecimalFormatSymbols(Locale("fa", "IR")).apply {
        groupingSeparator = '٬'
    }
    private val persianFormatter = DecimalFormat("#,###", persianSymbols)

    fun formatPersian(value: Long): String = persianFormatter.format(value)
}
