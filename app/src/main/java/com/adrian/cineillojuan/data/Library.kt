package com.adrian.cineillojuan.data

import android.content.Context
import kotlinx.serialization.encodeToString
import java.util.concurrent.TimeUnit

/**
 * Lo que es de cada tele: vídeos vistos, "Mi lista" y qué vídeos son nuevos.
 * Todo se identifica por la URL de YouTube del vídeo.
 */
class Library(context: Context) {

    companion object {
        /** Cuánto tiempo se marca un vídeo como NUEVO desde que aparece en la web. */
        val NEW_FOR_MILLIS = TimeUnit.DAYS.toMillis(30)
    }

    private val prefs = context.getSharedPreferences("library", Context.MODE_PRIVATE)
    private val json = CatalogParser.json

    fun seen(): Set<String> = prefs.getStringSet("seen", emptySet())!!.toSet()

    fun markSeen(url: String): Set<String> = (seen() + url).also { prefs.edit().putStringSet("seen", it).apply() }

    fun favorites(): List<String> =
        prefs.getString("favorites", null)?.let { runCatching { json.decodeFromString<List<String>>(it) }.getOrNull() }
            ?: emptyList()

    /** Añade o quita de Mi lista (los últimos añadidos van primero). */
    fun toggleFavorite(url: String): List<String> {
        val current = favorites()
        val updated = if (url in current) current - url else listOf(url) + current
        prefs.edit().putString("favorites", json.encodeToString(updated)).apply()
        return updated
    }

    /** Categorías que el usuario no quiere ver en la portada ni en la ruleta. */
    fun hiddenCategories(): Set<String> = prefs.getStringSet("hidden_categories", emptySet())!!.toSet()

    fun setCategoryHidden(name: String, hidden: Boolean): Set<String> =
        (if (hidden) hiddenCategories() + name else hiddenCategories() - name)
            .also { prefs.edit().putStringSet("hidden_categories", it).apply() }

    fun newVideos(): Map<String, Long> =
        prefs.getString("new", null)?.let { runCatching { json.decodeFromString<Map<String, Long>>(it) }.getOrNull() }
            ?: emptyMap()

    /**
     * Compara el catálogo con el último que se vio y apunta como nuevos los vídeos que no estaban.
     * La primera vez no hay con qué comparar, así que solo se guarda como referencia.
     * Devuelve los vídeos nuevos (URL → cuándo aparecieron) que siguen vigentes.
     */
    fun registerCatalog(categories: List<Category>, now: Long = System.currentTimeMillis()): Map<String, Long> {
        val urls = categories.flatMap { c -> c.videos.map { it.url } }.toSet()
        val known = prefs.getStringSet("known", null)
        val fresh = newVideos()
            .filter { (url, since) -> url in urls && now - since < NEW_FOR_MILLIS }
            .toMutableMap()
        if (known != null) (urls - known).forEach { fresh[it] = now }
        prefs.edit()
            .putStringSet("known", urls)
            .putString("new", json.encodeToString(fresh.toMap()))
            .apply()
        return fresh
    }
}
