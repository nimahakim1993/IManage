package com.nima.app.imanage.presentation.view

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.SettingsViewModel
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.BiometricHelper
import com.nima.app.imanage.util.LanguageManager
import com.nima.app.imanage.util.SecurityManager
import com.nima.app.imanage.util.ThemeManager
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun SettingsScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val viewModel: SettingsViewModel = koinViewModel()

    var currentLanguage by remember { mutableStateOf(LanguageManager.getLanguage(context)) }
    var currentTheme by remember { mutableStateOf(ThemeManager.getThemeMode(context)) }
    val backupState by viewModel.backupState.collectAsState()
    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val settingsTitle = stringResource(R.string.settings_title)

    val createBackupFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.export(context, it) }
    }

    val openRestoreFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirmation = true
        }
    }

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = settingsTitle, showBack = true)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LanguageSection(
            currentLanguage = currentLanguage,
            onLanguageChange = { lang ->
                LanguageManager.setLanguage(context, lang)
                currentLanguage = lang
                activity?.recreate()
            }
        )

        ThemeSection(
            currentTheme = currentTheme,
            onThemeChange = { theme ->
                ThemeManager.setThemeMode(context, theme)
                currentTheme = theme
                activity?.recreate()
            }
        )

        ModuleProtectionSection(context = context)

        BackupRestoreSection(
            backupState = backupState,
            onBackup = {
                val filename = "imanage_backup_${LocalDate.now()}.json"
                createBackupFile.launch(filename)
            },
            onRestore = {
                openRestoreFile.launch(arrayOf("application/json"))
            }
        )
    }

    if (showRestoreConfirmation && pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = { Text(stringResource(R.string.restore_data)) },
            text = { Text(stringResource(R.string.restore_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirmation = false
                    pendingRestoreUri?.let { uri ->
                        viewModel.import(context, uri)
                    }
                    pendingRestoreUri = null
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirmation = false
                    pendingRestoreUri = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val state = backupState
    if (state is SettingsViewModel.BackupState.Exporting || state is SettingsViewModel.BackupState.Importing) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    if (state is SettingsViewModel.BackupState.Exporting)
                        stringResource(R.string.backup_in_progress)
                    else
                        stringResource(R.string.restore_in_progress)
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    if (state is SettingsViewModel.BackupState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text(stringResource(R.string.error_title)) },
            text = {
                Text(state.message.ifBlank {
                    stringResource(R.string.backup_failed)
                })
            },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (state is SettingsViewModel.BackupState.Success) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetState()
                if (state.isRestore) {
                    activity?.recreate()
                }
            },
            title = { Text(stringResource(R.string.success)) },
            text = {
                Text(
                    if (state.isRestore)
                        stringResource(R.string.restore_success)
                    else
                        stringResource(R.string.backup_success)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetState()
                    if (state.isRestore) {
                        activity?.recreate()
                    }
                }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = vazirFontFamily
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = vazirFontFamily
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LanguageSection(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    SettingsCard(
        icon = Icons.Default.Language,
        iconTint = Color(0xFF1565C0),
        title = stringResource(R.string.language)
    ) {
        OptionRow(
            label = stringResource(R.string.language_english),
            selected = currentLanguage == LanguageManager.LANG_EN,
            onClick = {
                if (currentLanguage != LanguageManager.LANG_EN) {
                    onLanguageChange(LanguageManager.LANG_EN)
                }
            }
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        OptionRow(
            label = stringResource(R.string.language_persian),
            selected = currentLanguage == LanguageManager.LANG_FA,
            onClick = {
                if (currentLanguage != LanguageManager.LANG_FA) {
                    onLanguageChange(LanguageManager.LANG_FA)
                }
            }
        )
    }
}

@Composable
private fun ThemeSection(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    SettingsCard(
        icon = Icons.Default.Palette,
        iconTint = MaterialTheme.colorScheme.tertiary,
        title = stringResource(R.string.theme)
    ) {
        OptionRow(
            label = stringResource(R.string.theme_system),
            selected = currentTheme == ThemeManager.THEME_SYSTEM,
            onClick = {
                if (currentTheme != ThemeManager.THEME_SYSTEM) {
                    onThemeChange(ThemeManager.THEME_SYSTEM)
                }
            }
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        OptionRow(
            label = stringResource(R.string.theme_light),
            selected = currentTheme == ThemeManager.THEME_LIGHT,
            onClick = {
                if (currentTheme != ThemeManager.THEME_LIGHT) {
                    onThemeChange(ThemeManager.THEME_LIGHT)
                }
            }
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        OptionRow(
            label = stringResource(R.string.theme_dark),
            selected = currentTheme == ThemeManager.THEME_DARK,
            onClick = {
                if (currentTheme != ThemeManager.THEME_DARK) {
                    onThemeChange(ThemeManager.THEME_DARK)
                }
            }
        )
    }
}

@Composable
private fun ModuleProtectionSection(context: Context) {
    val modules = remember { SecurityManager.modules }
    val authType = remember { BiometricHelper.availableAuthType(context) }
    var biometricVerified by remember { mutableStateOf(false) }

    val activity = context.findFragmentActivity()
    val authLabel = if (authType == BiometricHelper.AuthType.BIOMETRIC)
        stringResource(R.string.biometric_tap_to_verify)
    else
        stringResource(R.string.biometric_tap_to_unlock)

    SettingsCard(
        icon = Icons.Default.Shield,
        iconTint = Color(0xFFE65100),
        title = stringResource(R.string.module_protection),
        description = if (biometricVerified) stringResource(R.string.module_protection_desc)
        else stringResource(R.string.module_protection_locked)
    ) {
        if (biometricVerified) {
            modules.forEachIndexed { index, module ->
                val isProtected = remember {
                    mutableStateOf(
                        SecurityManager.isModuleProtected(
                            context,
                            module.key
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newValue = !isProtected.value
                            isProtected.value = newValue
                            SecurityManager.setModuleProtected(context, module.key, newValue)
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(module.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = module.icon,
                                contentDescription = null,
                                tint = module.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(module.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = vazirFontFamily
                        )
                    }
                    Switch(
                        checked = isProtected.value,
                        onCheckedChange = { checked ->
                            isProtected.value = checked
                            SecurityManager.setModuleProtected(context, module.key, checked)
                        }
                    )
                }

                if (index < modules.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        activity?.let { act ->
                            BiometricHelper.authenticate(
                                activity = act,
                                title = context.getString(R.string.biometric_reveal_title),
                                subtitle = authLabel,
                                authType = authType,
                                onSuccess = { biometricVerified = true },
                                onError = { /* stay locked */ }
                            )
                        }
                    }
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = authLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupRestoreSection(
    backupState: SettingsViewModel.BackupState,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    val isBusy = backupState is SettingsViewModel.BackupState.Exporting ||
            backupState is SettingsViewModel.BackupState.Importing

    SettingsCard(
        icon = Icons.Default.Backup,
        iconTint = MaterialTheme.colorScheme.secondary,
        title = stringResource(R.string.backup_restore)
    ) {
        Button(
            onClick = onBackup,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                stringResource(R.string.backup_data),
                fontFamily = vazirFontFamily
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onRestore,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                stringResource(R.string.restore_data),
                fontFamily = vazirFontFamily
            )
        }
    }
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
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = vazirFontFamily
        )
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
