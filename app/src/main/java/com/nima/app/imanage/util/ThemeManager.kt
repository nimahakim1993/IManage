package com.nima.app.imanage.util

import android.content.Context

object ThemeManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_THEME = "theme_mode"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun getThemeMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setThemeMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode).apply()
    }
}
