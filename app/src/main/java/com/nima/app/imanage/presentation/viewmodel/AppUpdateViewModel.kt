package com.nima.app.imanage.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.model.AppUpdateConfig
import com.nima.app.imanage.data.repository.AppUpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppUpdateViewModel(
    application: Application,
    private val repository: AppUpdateRepository
) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(PREFERENCES_NAME, 0)
    private val _update = MutableStateFlow<AppUpdateConfig?>(null)
    val update: StateFlow<AppUpdateConfig?> = _update.asStateFlow()

    fun checkForUpdate() {
        viewModelScope.launch {
            val config = repository.getUpdateConfig() ?: return@launch
            if (currentVersionCode() < config.minimum_version &&
                (config.force_update || preferences.getInt(
                    DISMISSED_MESSAGE_ID,
                    -1
                ) != config.message_id) &&
                config.store_urls.any { it.enable && it.url.isNotBlank() }
            ) {
                _update.value = config
            }
        }
    }

    fun dismissOptionalUpdate(messageId: Int) {
        preferences.edit().putInt(DISMISSED_MESSAGE_ID, messageId).apply()
        _update.value = null
    }

    private fun currentVersionCode(): Long {
        val packageInfo = getApplication<Application>().packageManager.getPackageInfo(
            getApplication<Application>().packageName,
            0
        )
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "app_update_preferences"
        const val DISMISSED_MESSAGE_ID = "dismissed_message_id"
    }
}
