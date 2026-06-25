package com.nima.app.imanage.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"

    const val LANG_EN = "en"
    const val LANG_FA = "fa"

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANG_EN) ?: LANG_EN
    }

    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun wrap(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
