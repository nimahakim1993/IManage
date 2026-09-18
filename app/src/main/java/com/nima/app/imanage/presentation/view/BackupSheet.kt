package com.nima.app.imanage.presentation.view

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.nima.app.imanage.R
import com.nima.app.imanage.presentation.viewmodel.SettingsViewModel
import com.nima.app.imanage.ui.theme.vazirFontFamily
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val viewModel: SettingsViewModel = koinViewModel()
    val backupState by viewModel.backupState.collectAsState()

    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.backup_restore),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val filename = "imanage_backup_${LocalDate.now()}.json"
                    createBackupFile.launch(filename)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.backup_data),
                    fontFamily = vazirFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    openRestoreFile.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.restore_data),
                    fontFamily = vazirFontFamily
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
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

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
