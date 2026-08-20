package com.nima.app.imanage.data.repository

import com.google.gson.Gson
import com.nima.app.imanage.data.model.AppUpdateConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class AppUpdateRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val gson: Gson = Gson()
) {
    companion object {
        private const val UPDATE_URL =
            "https://imanage.nimahakim-ad.workers.dev/force.json"
    }

    suspend fun getUpdateConfig(): AppUpdateConfig? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(UPDATE_URL).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                response.body?.string()?.let { gson.fromJson(it, AppUpdateConfig::class.java) }
            }
        }.getOrNull()
    }
}
