package com.adrian.cineillojuan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.adrian.cineillojuan.data.DurationFilter
import com.adrian.cineillojuan.data.Entry
import com.adrian.cineillojuan.data.Video
import kotlinx.coroutines.delay

/**
 * Como la ruleta del Cine: va pasando vídeos al azar hasta pararse en uno.
 * Por defecto no repite vídeos ya vistos, como el Cine no repite uno en una semana.
 */
@Composable
fun RouletteDialog(
    entries: List<Entry>,
    categories: List<String>,
    seen: Set<String>,
    onPlay: (Video) -> Unit,
    onDismiss: () -> Unit,
) {
    // -1 = todas las categorías.
    var category by remember { mutableIntStateOf(-1) }
    var duration by remember { mutableStateOf(DurationFilter.ANY) }
    var onlyUnseen by remember { mutableStateOf(true) }

    val filtered = remember(category, duration) {
        entries.filter {
            (category < 0 || it.category == categories[category]) && it.video.duration in duration.range
        }
    }
    val unseen = remember(filtered, seen) { filtered.filter { it.video.url !in seen } }
    // Si con "sin ver" no queda nada, se tira entre todos los del filtro.
    val allSeen = onlyUnseen && unseen.isEmpty() && filtered.isNotEmpty()
    val pool = if (onlyUnseen && !allSeen) unseen else filtered

    var spin by remember { mutableIntStateOf(0) }
    // Solo se lleva el foco a "Ver" en las tiradas pedidas, no al cambiar un filtro.
    var focusAfterSpin by remember { mutableStateOf(true) }
    var current by remember { mutableStateOf(pool.randomOrNull()) }
    var spinning by remember { mutableStateOf(true) }
    val playFocus = remember { FocusRequester() }
    val firstFilter = remember { FocusRequester() }

    LaunchedEffect(spin, pool) {
        if (pool.isEmpty()) {
            current = null
            spinning = false
            return@LaunchedEffect
        }
        spinning = true
        repeat(14) { step ->
            current = pool.random()
            delay(50L + step * 18L)
        }
        spinning = false
        if (focusAfterSpin) runCatching { playFocus.requestFocus() }
    }

    fun reroll() {
        focusAfterSpin = true
        spin++
    }

    fun changeFilter(change: () -> Unit) {
        focusAfterSpin = false
        change()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .width(920.dp)
                .background(Paper, RoundedCornerShape(16.dp))
                .border(3.dp, Ink, RoundedCornerShape(16.dp)),
        ) {
            FilmStrip(Modifier.clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)))
            Column(
                Modifier
                    .padding(horizontal = 28.dp, vertical = 20.dp)
                    .ticketBorder()
                    .padding(20.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    Box(
                        Modifier
                            .width(360.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, Ink, RoundedCornerShape(10.dp))
                            .background(Cream),
                    ) {
                        AsyncImage(
                            model = current?.video?.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                current == null -> "NO HAY VÍDEOS ASÍ"
                                spinning -> "GIRANDO LA RULETA…"
                                else -> "LA RULETA HA ELEGIDO"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = Coral,
                        )
                        Box(Modifier.padding(vertical = 8.dp).height(2.dp).width(240.dp).background(Ink))
                        val picked = current
                        if (picked != null) {
                            Text(
                                picked.video.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Pill(picked.category, Modifier.widthIn(max = 260.dp))
                                Pill(picked.video.durationLabel)
                            }
                        } else {
                            Text("Prueba con otros filtros.", style = MaterialTheme.typography.bodyLarge, color = Muted)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CineButton(
                                "▶ Ver",
                                onClick = { picked?.let { onPlay(it.video) } },
                                enabled = !spinning && picked != null,
                                filled = true,
                                // Al bajar a los filtros se entra siempre por el primero.
                                modifier = Modifier.focusRequester(playFocus).focusProperties { down = firstFilter },
                            )
                            CineButton(
                                "🎲 Otra tirada",
                                onClick = ::reroll,
                                enabled = !spinning && pool.isNotEmpty(),
                                modifier = Modifier.focusProperties { down = firstFilter },
                            )
                        }
                    }
                }

                Box(Modifier.padding(vertical = 14.dp).height(2.dp).width(820.dp).background(Ink))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("FILTROS", style = MaterialTheme.typography.titleMedium, color = Coral)
                    CineButton(
                        if (category < 0) "Todas las categorías" else categories[category],
                        onClick = { changeFilter { category = if (category + 1 >= categories.size) -1 else category + 1 } },
                        compact = true,
                        maxLines = 1,
                        modifier = Modifier.widthIn(max = 300.dp).focusRequester(firstFilter),
                    )
                    CineButton(
                        duration.label,
                        onClick = {
                            changeFilter { duration = DurationFilter.entries[(duration.ordinal + 1) % DurationFilter.entries.size] }
                        },
                        compact = true,
                    )
                    CineButton(
                        if (onlyUnseen) "✓ Solo sin ver" else "Incluye vistos",
                        onClick = { changeFilter { onlyUnseen = !onlyUnseen } },
                        compact = true,
                    )
                }
                Text(
                    if (allSeen) "Ya has visto todo lo de este filtro: la ruleta tira entre todos."
                    else "${pool.size} vídeos en la ruleta · pulsa un filtro para cambiarlo",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
