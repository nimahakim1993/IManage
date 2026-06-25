package com.nima.app.imanage.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatUtils {

    private val westernFormatter = DecimalFormat(
        "#,###",
        DecimalFormatSymbols(Locale.ENGLISH)
    )

    private val persianFormatter = DecimalFormat(
        "#,###",
        DecimalFormatSymbols(Locale("fa", "IR")).apply {
            groupingSeparator = '٬'
        }
    )

    fun format(value: Long): String =
        (if (isPersianLocale()) persianFormatter else westernFormatter).format(value)

    fun format(value: Int): String = format(value.toLong())

    fun applySeparator(input: String): String {
        val asLong = parseToLong(input)
        if (asLong == 0L && input.none { it.isDigit() }) return ""
        return format(asLong)
    }

    fun parseToLong(input: String): Long {
        if (input.isEmpty()) return 0L
        val normalized = StringBuilder(input.length)
        for (c in input) {
            normalized.append(
                when (c) {
                    in '0'..'9' -> c
                    in '\u0660'..'\u0669' -> ('0'.code + (c.code - 0x0660)).toChar()
                    else -> return 0L
                }
            )
        }
        return normalized.toString().toLongOrNull() ?: 0L
    }

    fun toLocalizedDigits(input: String): String {
        if (input.isEmpty()) return input
        if (!isPersianLocale()) return input
        val out = StringBuilder(input.length)
        for (c in input) {
            out.append(
                if (c in '0'..'9') (0x0660 + (c.code - '0'.code)).toChar() else c
            )
        }
        return out.toString()
    }

    private fun isPersianLocale(): Boolean = Locale.getDefault().language == "fa"
}
