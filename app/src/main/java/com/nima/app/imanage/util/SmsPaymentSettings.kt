package com.nima.app.imanage.util

import android.content.Context
import android.provider.Telephony

class SmsPaymentSettings(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getSenders(): Set<String> = preferences.getStringSet(KEY_SENDERS, emptySet())
        ?.map(::normalizeSender)
        ?.filter(String::isNotBlank)
        ?.toSet()
        ?: emptySet()

    fun addSender(sender: String) {
        val normalized = normalizeSender(sender)
        if (normalized.isBlank()) return
        preferences.edit().putStringSet(KEY_SENDERS, getSenders() + normalized).apply()
    }

    fun removeSender(sender: String) {
        preferences.edit().putStringSet(KEY_SENDERS, getSenders() - normalizeSender(sender)).apply()
    }

    fun isAllowed(sender: String): Boolean = normalizeSender(sender) in getSenders()

    fun getObservedSenders(): Set<String> = preferences
        .getStringSet(KEY_OBSERVED_SENDERS, emptySet())
        ?.map(::normalizeSender)
        ?.filter(String::isNotBlank)
        ?.toSet()
        ?: emptySet()

    fun discoverBankSenders(): Set<String> {
        val discovered = mutableSetOf<String>()
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY)
        appContext.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex).orEmpty()
                val body = cursor.getString(bodyIndex).orEmpty()
                if (address.isNotBlank() && SmsPaymentParser.isLikelyBankMessage(body)) {
                    discovered += normalizeSender(address)
                }
            }
        }
        preferences.edit().putStringSet(KEY_OBSERVED_SENDERS, discovered).apply()
        return discovered
    }

    companion object {
        private const val NAME = "sms_payment_settings"
        private const val KEY_SENDERS = "allowed_senders"
        private const val KEY_OBSERVED_SENDERS = "observed_senders"

        fun normalizeSender(sender: String): String = sender.trim()
            .replace(Regex("[^A-Za-z0-9]"), "")
    }
}
