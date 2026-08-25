package com.nima.app.imanage.util

import java.security.MessageDigest

data class ParsedPayment(val title: String, val amount: Long)

object SmsPaymentParser {
    private val paymentWords = listOf("پرداخت", "خرید", "برداشت", "purchase", "payment", "debit")
    private val incomingWords = listOf("واریز", "دریافت", "وصول", "deposit", "credit")
    private val refundWords = listOf("برگشت", "بازگشت", "refund", "reversal")
    private val verificationCodeMarkers = listOf("رمز پویا", "رمزپویا")
    private val amountRegex = Regex(
        "(?:مبلغ|amount|مبلغ خرید|خرید)\\s*[:：]?\\s*([۰-۹٠-٩\\d][۰-۹٠-٩\\d,،. ]*)",
        RegexOption.IGNORE_CASE
    )
    private val transactionAmountRegex =
        Regex("(?:برداشت|خرید|پرداخت)\\s*[:：]?\\s*([0-9][0-9,،.]*)", RegexOption.IGNORE_CASE)
    private val balanceRegex =
        Regex("(?:مانده|موجودی|موجودي)[\\s:：]{1,3}[0-9]", RegexOption.IGNORE_CASE)
    private val debitLineRegex = Regex("(?m)^\\s*-\\s*([0-9][0-9,،.]*)\\s*$")

    fun parse(text: String): ParsedPayment? {
        val normalized = normalizeDigits(text)
        val lower = normalized.lowercase()

        // Verification-code messages can contain "خرید" and "مبلغ", but are not payments.
        if (verificationCodeMarkers.any(normalized::contains)) return null

        // Only bank messages containing the account-balance line are payment messages.
        if (!isLikelyBankMessage(normalized)) return null

        if (refundWords.any(lower::contains) || incomingWords.any(lower::contains)) return null
        val debitAmountText = debitLineRegex.find(normalized)?.groupValues?.get(1)
        if (debitAmountText == null && paymentWords.none(lower::contains)) return null

        val amountText = debitAmountText
            ?: transactionAmountRegex.find(normalized)?.groupValues?.get(1)
            ?: amountRegex.find(normalized)?.groupValues?.get(1)
            ?: return null
        val rialAmount = amountText.filter(Char::isDigit).toLongOrNull() ?: return null
        if (rialAmount <= 0) return null
        val tomanAmount = rialAmount / RIALS_PER_TOMAN
        if (tomanAmount <= 0) return null

        val title = Regex(
            "(?:فروشگاه|merchant|خرید از|از)\\s*[:：]?\\s*([^\\n،,]+)",
            RegexOption.IGNORE_CASE
        )
            .find(normalized)?.groupValues?.get(1)?.trim()
            ?.takeIf(String::isNotBlank)
            ?: "Card payment"
        return ParsedPayment(title, tomanAmount)
    }

    fun isLikelyBankMessage(text: String): Boolean {
        val normalized = normalizeDigits(text)
        return balanceRegex.containsMatchIn(normalized)
    }

    fun hash(sender: String, timestamp: Long, text: String): String {
        val value = "$sender|$timestamp|$text"
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun normalizeDigits(value: String): String = value.map {
        when (it) {
            in '۰'..'۹' -> ('0'.code + (it.code - '۰'.code)).toChar()
            in '٠'..'٩' -> ('0'.code + (it.code - '٠'.code)).toChar()
            else -> it
        }
    }.joinToString("")

    private const val RIALS_PER_TOMAN = 10L
}
