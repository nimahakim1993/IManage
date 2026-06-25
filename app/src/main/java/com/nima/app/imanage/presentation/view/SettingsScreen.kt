package com.nima.app.imanage.presentation.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.util.LanguageManager
import com.nima.app.imanage.util.ThemeManager

@Composable
fun SettingsScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    var currentLanguage by remember { mutableStateOf(LanguageManager.getLanguage(context)) }
    var currentTheme by remember { mutableStateOf(ThemeManager.getThemeMode(context)) }

    val settingsTitle = stringResource(R.string.settings_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = settingsTitle, showBack = true)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium
        )

        LanguageOption(
            label = stringResource(R.string.language_english),
            selected = currentLanguage == LanguageManager.LANG_EN,
            onClick = {
                if (currentLanguage != LanguageManager.LANG_EN) {
                    LanguageManager.setLanguage(context, LanguageManager.LANG_EN)
                    currentLanguage = LanguageManager.LANG_EN
                    activity?.recreate()
                }
            }
        )

        LanguageOption(
            label = stringResource(R.string.language_persian),
            selected = currentLanguage == LanguageManager.LANG_FA,
            onClick = {
                if (currentLanguage != LanguageManager.LANG_FA) {
                    LanguageManager.setLanguage(context, LanguageManager.LANG_FA)
                    currentLanguage = LanguageManager.LANG_FA
                    activity?.recreate()
                }
            }
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleMedium
        )

        ThemeOption(
            label = stringResource(R.string.theme_system),
            selected = currentTheme == ThemeManager.THEME_SYSTEM,
            onClick = {
                if (currentTheme != ThemeManager.THEME_SYSTEM) {
                    ThemeManager.setThemeMode(context, ThemeManager.THEME_SYSTEM)
                    currentTheme = ThemeManager.THEME_SYSTEM
                    activity?.recreate()
                }
            }
        )

        ThemeOption(
            label = stringResource(R.string.theme_light),
            selected = currentTheme == ThemeManager.THEME_LIGHT,
            onClick = {
                if (currentTheme != ThemeManager.THEME_LIGHT) {
                    ThemeManager.setThemeMode(context, ThemeManager.THEME_LIGHT)
                    currentTheme = ThemeManager.THEME_LIGHT
                    activity?.recreate()
                }
            }
        )

        ThemeOption(
            label = stringResource(R.string.theme_dark),
            selected = currentTheme == ThemeManager.THEME_DARK,
            onClick = {
                if (currentTheme != ThemeManager.THEME_DARK) {
                    ThemeManager.setThemeMode(context, ThemeManager.THEME_DARK)
                    currentTheme = ThemeManager.THEME_DARK
                    activity?.recreate()
                }
            }
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OptionRow(label = label, selected = selected, onClick = onClick)
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OptionRow(label = label, selected = selected, onClick = onClick)
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = label)
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
