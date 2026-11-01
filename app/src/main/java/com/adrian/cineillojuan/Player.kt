package com.adrian.cineillojuan

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.adrian.cineillojuan.data.CINEMA_TWITCH_CHANNEL
import com.adrian.cineillojuan.data.Video

/** Una forma de abrir algo: el enlace y, si hace falta, la app concreta. */
private data class Target(val uri: String, val pkg: String)

fun Context.playVideo(video: Video) {
    open(
        preferred = listOf(
            Target(video.url, "com.google.android.youtube.tv"), // YouTube para Android TV (si está desactivado, salta)
            Target(video.url, "org.smarttube.stable"),          // SmartTube
            Target(video.url, "org.smarttube.beta"),
            Target(video.url, "com.liskovsoft.smarttubetv"),    // SmartTube (nombres antiguos)
            Target(video.url, "com.liskovsoft.smarttubetv.beta"),
            Target(video.url, "com.google.android.youtube"),    // YouTube móvil/tablet
        ),
        fallbackUri = video.url,
        hosts = listOf("youtube.com", "youtu.be"),
        notFound = "Instala YouTube o SmartTube para ver los vídeos",
    )
}

fun Context.openLiveCinema() {
    val web = "https://www.twitch.tv/$CINEMA_TWITCH_CHANNEL"
    open(
        preferred = listOf(
            Target("s0undtv://stream/$CINEMA_TWITCH_CHANNEL", "com.s0und.s0undtv"), // S0undTV, con su propio enlace
            Target(web, "tv.twitch.android.viewer"),                         // Twitch para Android TV
            Target(web, "tv.twitch.android.app"),                            // Twitch móvil
        ),
        fallbackUri = web,
        hosts = listOf("twitch.tv"),
        notFound = "Instala S0undTV o la app de Twitch para ver el Cine en directo",
    )
}

/**
 * Prueba las apps preferidas en orden y, si no hay ninguna, cualquier otra que declare
 * expresamente esos dominios (otro cliente de YouTube o de Twitch). Nunca un navegador ni apps
 * genéricas como Downloader, que abren cualquier web.
 */
private fun Context.open(preferred: List<Target>, fallbackUri: String, hosts: List<String>, notFound: String) {
    for (target in preferred) {
        if (start(Intent(Intent.ACTION_VIEW, Uri.parse(target.uri)).setPackage(target.pkg))) return
    }
    val generic = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri))
    val specific = packageManager.queryIntentActivities(generic, PackageManager.GET_RESOLVED_FILTER)
        .firstOrNull { info ->
            val filter = info.filter ?: return@firstOrNull false
            (0 until filter.countDataAuthorities()).any { i ->
                val host = filter.getDataAuthority(i).host
                hosts.any { host == it || host.endsWith(".$it") }
            }
        }
    if (specific != null && start(generic.setClassName(specific.activityInfo.packageName, specific.activityInfo.name))) return
    Toast.makeText(this, notFound, Toast.LENGTH_LONG).show()
}

private fun Context.start(intent: Intent): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (_: ActivityNotFoundException) {
    false
}
