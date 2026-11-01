package com.adrian.cineillojuan.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/** Canal de Twitch donde se emite el Cine IlloJuan las 24 horas. */
const val CINEMA_TWITCH_CHANNEL = "illojuan"

/**
 * Si el Cine está en directo, según decapi.me (servicio público que responde "<canal> is offline"
 * o cuánto lleva emitiendo). Se usa porque la API oficial de Twitch pide una clave secreta que no
 * puede ir dentro de una app pública.
 */
object LiveStatus {

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /** true en directo, false apagado y null si no se ha podido saber. */
    suspend fun isCinemaLive(): Boolean? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://decapi.me/twitch/uptime/$CINEMA_TWITCH_CHANNEL")
                .header("User-Agent", "CineIlloJuanTV")
                .build()
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                parseUptime(response.body!!.string())
            }
        }.getOrNull()
    }

    internal fun parseUptime(body: String): Boolean? = when {
        body.contains("is offline", ignoreCase = true) -> false
        Regex("\\d+ (second|minute|hour|day)s?", RegexOption.IGNORE_CASE).containsMatchIn(body) -> true
        else -> null
    }
}
