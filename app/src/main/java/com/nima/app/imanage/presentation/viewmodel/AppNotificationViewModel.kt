package com.nima.app.imanage.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nima.app.imanage.data.model.AppNotifications
import com.nima.app.imanage.data.repository.AppNotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppNotificationViewModel(
    application: Application,
    private val repository: AppNotificationRepository
) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(PREFERENCES_NAME, 0)
    private val _notifications = MutableStateFlow<AppNotifications?>(null)
    val notifications: StateFlow<AppNotifications?> = _notifications.asStateFlow()

    fun checkForNotifications() {
        viewModelScope.launch {
            val result = repository.getNotifications() ?: return@launch
            if (result.notifications.any { it.enabled } &&
                preferences.getInt(SEEN_MESSAGE_ID, -1) != result.message_id
            ) {
                _notifications.value = result
            }
        }
    }

    fun markAsSeen(messageId: Int) {
        preferences.edit().putInt(SEEN_MESSAGE_ID, messageId).apply()
        _notifications.value = null
    }

    private companion object {
        const val PREFERENCES_NAME = "app_notification_preferences"
        const val SEEN_MESSAGE_ID = "in_app_notification_message_id"
    }
}
