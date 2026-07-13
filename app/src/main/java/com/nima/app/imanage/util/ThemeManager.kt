package com.nima.app.imanage.util

import android.content.Context

object ThemeManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_FONT_SCALE = "font_scale"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    const val FONT_SMALL = "small"
    const val FONT_NORMAL = "normal"
    const val FONT_LARGE = "large"

    private val fontScaleMap = mapOf(
        FONT_SMALL to 0.85f,
        FONT_NORMAL to 1.0f,
        FONT_LARGE to 1.15f
    )

    fun getThemeMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setThemeMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode).apply()
    }

    fun getFontScale(context: Context): Float {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val key = prefs.getString(KEY_FONT_SCALE, FONT_NORMAL) ?: FONT_NORMAL
        return fontScaleMap[key] ?: 1.0f
    }

    fun getFontSizeKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FONT_SCALE, FONT_NORMAL) ?: FONT_NORMAL
    }

    fun setFontSize(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FONT_SCALE, key).apply()
    }
}
