package com.nima.app.imanage.data.model

data class AppUpdateConfig(
    val message_id: Int = 0,
    val minimum_version: Int = 0,
    val force_update: Boolean = false,
    val message: String = "",
    val store_urls: List<UpdateStore> = emptyList()
)

data class UpdateStore(
    val title: String = "",
    val url: String = "",
    val logo: String = "",
    val enable: Boolean = false
)
