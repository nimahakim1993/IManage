package com.nima.app.imanage.data.model

data class AppNotifications(
    val message_id: Int = 0,
    val notifications: List<AppNotification> = emptyList()
)

data class AppNotification(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val enabled: Boolean = false
)
