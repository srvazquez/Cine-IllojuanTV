package com.adrian.cineillojuan

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.adrian.cineillojuan.data.Video

private val youtubePackages = listOf(
    "com.google.android.youtube.tv", // YouTube para Android TV (si está desactivado, salta al siguiente)
    "org.smarttube.stable",          // SmartTube
    "org.smarttube.beta",
    "com.liskovsoft.smarttubetv",    // SmartTube (nombres antiguos)
    "com.liskovsoft.smarttubetv.beta",
    "com.google.android.youtube",    // YouTube móvil/tablet
    null,                            // cualquier app que abra enlaces de YouTube
)

/** Canal de Twitch donde se emite el Cine IlloJuan las 24 horas. */
private const val TWITCH_URL = "https://www.twitch.tv/illojuan"

private val twitchPackages = listOf(
    "tv.twitch.android.viewer", // Twitch para Android TV
    "tv.twitch.android.app",    // Twitch móvil
    null,
)

fun Context.playVideo(video: Video) {
    open(video.url, youtubePackages, "No hay ninguna app instalada para abrir YouTube")
}

fun Context.openLiveCinema() {
    open(TWITCH_URL, twitchPackages, "Instala la app de Twitch para ver el Cine en directo")
}

private fun Context.open(url: String, packages: List<String?>, notFound: String) {
    for (pkg in packages) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (pkg != null) intent.setPackage(pkg)
        try {
            startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
        }
    }
    Toast.makeText(this, notFound, Toast.LENGTH_LONG).show()
}
