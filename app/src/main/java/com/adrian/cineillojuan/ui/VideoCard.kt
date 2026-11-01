package com.adrian.cineillojuan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.adrian.cineillojuan.data.Video

/** Marcas que se pintan sobre la miniatura. */
data class VideoMarks(val seen: Boolean = false, val new: Boolean = false, val favorite: Boolean = false)

@Composable
fun VideoCard(
    video: Video,
    marks: VideoMarks,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    // Al mantener OK para quitar un vídeo de Mi lista, la tarjeta desaparece y el foco salta a la
    // siguiente, que recibe el "soltar OK" y lo tomaría como un clic. Solo se acepta si esta
    // tarjeta recibió también el "pulsar OK".
    var pressedHere by remember { mutableStateOf(false) }
    Column(modifier.width(260.dp)) {
        Card(
            onClick = onClick,
            onLongClick = onLongClick,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onPreviewKeyEvent { event ->
                    if (event.key !in okKeys) return@onPreviewKeyEvent false
                    when (event.type) {
                        KeyEventType.KeyDown -> { pressedHere = true; false }
                        KeyEventType.KeyUp -> { val stray = !pressedHere; pressedHere = false; stray }
                        else -> false
                    }
                },
            scale = CardDefaults.scale(focusedScale = 1.07f),
            border = CardDefaults.border(
                border = Border(BorderStroke(2.dp, Ink), shape = shape),
                focusedBorder = Border(BorderStroke(4.dp, Coral), shape = shape),
            ),
            colors = CardDefaults.colors(containerColor = Paper),
            shape = CardDefaults.shape(shape),
        ) {
            Box(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Row(
                    Modifier.align(Alignment.TopStart).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (marks.new) Badge("NUEVO", background = Coral, content = Color.White)
                    if (marks.seen) Badge("✓ VISTO", background = Paper, content = Ink)
                }
                if (marks.favorite) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(Paper, CircleShape)
                            .border(1.5.dp, Ink, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("♥", style = MaterialTheme.typography.labelLarge, color = Coral)
                    }
                }
                if (video.duration > 0) {
                    Badge(
                        video.durationLabel,
                        background = Paper,
                        content = Ink,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    )
                }
            }
        }
        // Altura fija de dos líneas para que la fila no salte al cambiar a marquee.
        Box(Modifier.padding(top = 12.dp).height(46.dp)) {
            if (focused) {
                // Enfocado: una sola línea que se desplaza para leer el título completo.
                Text(
                    video.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Coral,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 600),
                )
            } else {
                Text(
                    video.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private val okKeys = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)

@Composable
private fun Badge(text: String, background: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .border(1.5.dp, Ink, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 1.dp),
    )
}
