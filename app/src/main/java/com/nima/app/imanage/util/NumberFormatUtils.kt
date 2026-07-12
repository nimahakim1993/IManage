package com.nima.app.imanage.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun String.normalizeDigits(): String {
    val out = StringBuilder(length)
    for (c in this) {
        when {
            c in '\u06F0'..'\u06F9' -> out.append(('0'.code + (c.code - '\u06F0'.code)).toChar())
            c in '\u0660'..'\u0669' -> out.append(('0'.code + (c.code - '\u0660'.code)).toChar())
            else -> out.append(c)
        }
    }
    return out.toString()
}

object NumberFormatUtils {

    private val westernFormatter = DecimalFormat(
        "#,###",
        DecimalFormatSymbols(Locale.ENGLISH)
    )

    private val persianFormatter = DecimalFormat(
        "#,###",
        DecimalFormatSymbols(Locale.ENGLISH).apply {
            zeroDigit = '۰'
            groupingSeparator = '٬'
        }
    )

    fun format(value: Long): String {
        if (isPersianLocale()) {
            val absVal = kotlin.math.abs(value)
            val formatted = persianFormatter.format(absVal)
            return if (value < 0) "\u200E-$formatted" else formatted
        }
        return westernFormatter.format(value)
    }

    fun format(value: Int): String = format(value.toLong())

    fun applySeparator(input: String): String {
        if (input.isEmpty()) return ""
        val asLong = parseToLong(input)
        if (asLong == 0L && input.none { it.isDigit() }) return ""
        return format(asLong)
    }

    fun parseToLong(input: String): Long {
        if (input.isEmpty()) return 0L
        val normalized = StringBuilder(input.length)
        for (c in input) {
            when {
                c in '0'..'9' -> normalized.append(c)
                c in '\u06F0'..'\u06F9' -> // Persian: ۰۱۲۳۴۵۶۷۸۹
                    normalized.append(('0'.code + (c.code - '\u06F0'.code)).toChar())
                c in '\u0660'..'\u0669' -> // Arabic: ٠١٢٣٤٥٦٧٨٩
                    normalized.append(('0'.code + (c.code - '\u0660'.code)).toChar())
                c == ',' || c == '.' || c == '\u066C' || c == '\u066B' || c == ' ' -> Unit
                else -> return 0L
            }
        }
        return normalized.toString().toLongOrNull() ?: 0L
    }

    fun toLocalizedDigits(input: String): String {
        if (input.isEmpty() || !isPersianLocale()) return input
        val out = StringBuilder(input.length)
        for (c in input) {
            out.append(
                if (c in '0'..'9') (0x06F0 + (c.code - '0'.code)).toChar() else c
            )
        }
        return out.toString()
    }

    /**
     * Applies [applySeparator] to the [TextFieldValue] and re-derives the caret
     * so that it stays next to the same digit the user was editing. This avoids
     * the caret jumping to the end of the field when a group separator is added
     * or removed, and works for both Latin and Persian digits.
     */
    fun formatWithCursor(value: TextFieldValue): TextFieldValue {
        val oldText = value.text
        val newText = applySeparator(oldText)
        if (newText == oldText) return value
        val oldCursor = value.selection.end.coerceIn(0, oldText.length)
        val newCursor = computeCursorPosition(oldText, newText, oldCursor)
        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    private fun computeCursorPosition(oldText: String, newText: String, oldCursor: Int): Int {
        if (oldCursor <= 0) return 0
        if (oldCursor >= oldText.length) return newText.length
        val digitsBeforeCaret = oldText.substring(0, oldCursor).count { it.isDigit() }
        if (digitsBeforeCaret == 0) return oldCursor.coerceAtMost(newText.length)
        var seen = 0
        for (i in newText.indices) {
            if (newText[i].isDigit()) {
                seen++
                if (seen == digitsBeforeCaret) {
                    return (i + 1).coerceAtMost(newText.length)
                }
            }
        }
        return newText.length
    }

    private fun isPersianLocale(): Boolean = Locale.getDefault().language == "fa"
}
