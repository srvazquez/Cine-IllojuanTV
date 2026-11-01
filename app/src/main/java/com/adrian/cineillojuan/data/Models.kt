package com.adrian.cineillojuan.data

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val name: String,
    val videos: List<Video>,
)

@Serializable
data class Video(
    val name: String,
    /** Segundos. */
    val duration: Long = 0,
    val url: String,
    val imageName: String? = null,
    /** Última vez que se emitió en el Cine (ISO-8601), si alguna vez se emitió. */
    val played: String? = null,
) {
    val thumbnailUrl: String?
        get() = imageName?.let { "${CatalogSource.BASE_URL}/minis/${Uri.encode(it)}.jpg" }

    val durationLabel: String
        get() {
            val h = duration / 3600
            val m = (duration % 3600) / 60
            val s = duration % 60
            return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
        }
}

/** Un vídeo junto con la categoría a la que pertenece. */
data class Entry(val category: String, val video: Video)

fun List<Category>.entries(): List<Entry> = flatMap { c -> c.videos.map { Entry(c.name, it) } }
