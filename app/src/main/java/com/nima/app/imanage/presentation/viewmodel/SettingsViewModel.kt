package com.nima.app.imanage.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.util.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val backupManager: BackupManager) : ViewModel() {

    sealed class BackupState {
        data object Idle : BackupState()
        data object Exporting : BackupState()
        data object Importing : BackupState()
        data class Success(val isRestore: Boolean) : BackupState()
        data class Error(val message: String) : BackupState()
    }

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    fun export(context: Context, uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting
            backupManager.export(context, uri)
                .onSuccess {
                    _backupState.value = BackupState.Success(isRestore = false)
                }
                .onFailure { e ->
                    _backupState.value = BackupState.Error(e.message ?: "")
                }
        }
    }

    fun import(context: Context, uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Importing
            backupManager.import(context, uri)
                .onSuccess {
                    _backupState.value = BackupState.Success(isRestore = true)
                }
                .onFailure { e ->
                    _backupState.value = BackupState.Error(e.message ?: "")
                }
        }
    }

    fun resetState() {
        _backupState.value = BackupState.Idle
    }
}
