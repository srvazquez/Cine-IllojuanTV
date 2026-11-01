package com.adrian.cineillojuan.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

enum class Origin { WEB, CACHE, BUNDLED }

data class Catalog(val categories: List<Category>, val origin: Origin)

/**
 * Obtiene el catálogo de illojuan.tv. Orden de preferencia:
 *  1. la web en vivo (y se guarda en caché),
 *  2. la última copia guardada,
 *  3. la instantánea que viene dentro del APK (assets/catalog.json).
 */
class CatalogSource(private val context: Context) {

    companion object {
        const val BASE_URL = "https://illojuan.tv"
        private const val CACHE_FILE = "catalog.json"
        private const val BUNDLED_ASSET = "catalog.json"
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val cacheFile get() = File(context.filesDir, CACHE_FILE)

    suspend fun loadOffline(): Catalog = withContext(Dispatchers.IO) {
        runCatching { Catalog(CatalogParser.parseJson(cacheFile.readText()), Origin.CACHE) }
            .getOrElse {
                val raw = context.assets.open(BUNDLED_ASSET).bufferedReader().use { it.readText() }
                Catalog(CatalogParser.parseJson(raw), Origin.BUNDLED)
            }
    }

    suspend fun fetchFromWeb(): Catalog = withContext(Dispatchers.IO) {
        val html = get("$BASE_URL/")
        val bundle = CatalogParser.findBundlePath(html)
            ?: throw IllegalStateException("No se encontró el bundle en la portada")
        val categories = CatalogParser.parseBundle(get(BASE_URL + bundle))
        cacheFile.writeText(CatalogParser.json.encodeToString(categories))
        Catalog(categories, Origin.WEB)
    }

    private fun get(url: String): String {
        val request = Request.Builder().url(url).header("User-Agent", "CineIlloJuanTV/1.0").build()
        http.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code} en $url" }
            return response.body!!.string()
        }
    }
}
