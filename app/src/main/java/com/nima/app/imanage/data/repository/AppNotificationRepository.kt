package com.nima.app.imanage.data.repository

import com.google.gson.Gson
import com.nima.app.imanage.data.model.AppNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class AppNotificationRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val gson: Gson = Gson()
) {
    companion object {
        private const val NOTIFICATIONS_URL =
            "https://imanage.nimahakim-ad.workers.dev/notifications.json"
    }

    suspend fun getNotifications(): AppNotifications? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(NOTIFICATIONS_URL).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                response.body?.string()?.let { gson.fromJson(it, AppNotifications::class.java) }
            }
        }.getOrNull()
    }
}
