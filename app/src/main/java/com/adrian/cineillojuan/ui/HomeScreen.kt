@file:OptIn(ExperimentalComposeUiApi::class)

package com.adrian.cineillojuan.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.adrian.cineillojuan.CatalogViewModel
import com.adrian.cineillojuan.UiState
import com.adrian.cineillojuan.data.Catalog
import com.adrian.cineillojuan.data.Entry
import com.adrian.cineillojuan.data.Origin
import com.adrian.cineillojuan.data.Video
import com.adrian.cineillojuan.data.dailyPick
import com.adrian.cineillojuan.data.entries
import com.adrian.cineillojuan.openLiveCinema
import com.adrian.cineillojuan.playVideo

/** Margen lateral para que nada quede cortado por el overscan de la tele. */
internal val Gutter = 48.dp

/** Lo que la tele recuerda de cada vídeo: vistos, Mi lista y novedades. */
data class LibraryState(
    val seen: Set<String> = emptySet(),
    val favorites: List<String> = emptyList(),
    val newVideos: Map<String, Long> = emptyMap(),
    /** Categorías ocultas: no salen en la portada, la ruleta ni el "hoy toca" (sí en la búsqueda). */
    val hiddenCategories: Set<String> = emptySet(),
) {
    fun marks(video: Video) = VideoMarks(
        seen = video.url in seen,
        new = video.url in newVideos,
        favorite = video.url in favorites,
    )
}

/** Acciones sobre un vídeo, comunes a la portada, la búsqueda y la ruleta. */
class VideoActions(val play: (Video) -> Unit, val toggleFavorite: (Video) -> Unit)

@Composable
fun HomeScreen(viewModel: CatalogViewModel) {
    val context = LocalContext.current
    // Mientras la app está en pantalla, se mira cada 2 minutos si el Cine está en directo.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.checkLive()
                delay(2 * 60 * 1000L)
            }
        }
    }
    val actions = remember(viewModel) {
        VideoActions(
            play = { viewModel.markSeen(it.url); context.playVideo(it) },
            toggleFavorite = {
                val added = viewModel.toggleFavorite(it.url)
                Toast.makeText(context, if (added) "♥ Añadido a Mi lista" else "Quitado de Mi lista", Toast.LENGTH_SHORT)
                    .show()
            },
        )
    }
    when (val state = viewModel.state) {
        UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CineLogo()
                Text("Preparando las palomitas…", style = MaterialTheme.typography.bodyLarge, color = Muted)
            }
        }
        is UiState.Error -> Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CineLogo()
            Spacer(Modifier.height(24.dp))
            Text("NO SE PUDO CARGAR EL CATÁLOGO", style = MaterialTheme.typography.headlineSmall, color = Coral)
            Text(state.message, color = Muted, modifier = Modifier.padding(16.dp))
            CineButton("Reintentar", onClick = viewModel::refresh, filled = true)
        }
        is UiState.Ready -> CatalogContent(
            catalog = state.catalog,
            library = LibraryState(viewModel.seen, viewModel.favorites, viewModel.newVideos, viewModel.hiddenCategories),
            actions = actions,
            onSetCategoryHidden = viewModel::setCategoryHidden,
            live = viewModel.live,
            refreshing = viewModel.refreshing,
            refreshError = viewModel.refreshError,
            onRefresh = viewModel::refresh,
        )
    }
}

@Composable
internal fun CatalogContent(
    catalog: Catalog,
    library: LibraryState,
    actions: VideoActions,
    onSetCategoryHidden: (String, Boolean) -> Unit,
    live: Boolean?,
    refreshing: Boolean,
    refreshError: String?,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    val hidden = library.hiddenCategories
    val entries = remember(catalog) { catalog.categories.entries() }
    val visibleEntries = remember(entries, hidden) { entries.filter { it.category !in hidden } }
    // La letra de cada categoría es la de la web (por su posición), aunque haya otras ocultas.
    val visibleCategories = remember(catalog, hidden) {
        catalog.categories.withIndex().filter { it.value.name !in hidden }
    }
    val byUrl = remember(entries) { entries.associateBy { it.video.url } }
    // Cambia una vez al día; se calcula al cargar el catálogo para que no salte al ver el vídeo.
    val pick = remember(visibleEntries) { dailyPick(visibleEntries, library.seen) }
    val newRow = remember(entries, library.newVideos, hidden) {
        library.newVideos.entries.sortedByDescending { it.value }
            .mapNotNull { byUrl[it.key] }
            .filter { it.category !in hidden }
            .map { it.video }
    }
    val favoritesRow = remember(entries, library.favorites) { library.favorites.mapNotNull { byUrl[it]?.video } }

    var showRoulette by rememberSaveable { mutableStateOf(false) }
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var showCategories by rememberSaveable { mutableStateOf(false) }
    val rouletteFocus = remember { FocusRequester() }

    if (searching) {
        SearchScreen(entries, library, actions, onClose = { searching = false })
        return
    }
    LaunchedEffect(Unit) { runCatching { rouletteFocus.requestFocus() } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item(key = "header") {
            Column {
                Header(
                    videoCount = entries.size,
                    categoryCount = catalog.categories.size,
                    status = when {
                        refreshing -> "ACTUALIZANDO…"
                        refreshError != null -> "SIN CONEXIÓN"
                        catalog.origin == Origin.WEB -> "AL DÍA"
                        else -> "COPIA GUARDADA"
                    },
                    pick = pick,
                    onPlay = actions.play,
                    onInfo = { showInfo = true },
                    onCategories = { showCategories = true },
                    onRoulette = { showRoulette = true },
                    onSearch = { searching = true },
                    live = live,
                    onLive = { context.openLiveCinema() },
                    rouletteFocus = rouletteFocus,
                )
                FilmStrip()
            }
        }
        if (newRow.isNotEmpty()) {
            item(key = "new") { VideoRow("Nuevo en el Cine", newRow, library, actions) }
        }
        if (favoritesRow.isNotEmpty()) {
            item(key = "favorites") { VideoRow("Mi lista", favoritesRow, library, actions, symbol = '♥') }
        }
        items(visibleCategories, key = { it.value.name }) { (i, category) ->
            VideoRow(category.name, category.videos, library, actions, symbol = ('A' + i).takeIf { it <= 'Z' })
        }
    }

    if (showRoulette) {
        RouletteDialog(
            entries = visibleEntries,
            categories = visibleCategories.map { it.value.name },
            seen = library.seen,
            onPlay = { showRoulette = false; actions.play(it) },
            onDismiss = { showRoulette = false },
        )
    }
    if (showCategories) {
        CategoriesDialog(
            categories = catalog.categories.map { it.name },
            hidden = hidden,
            onToggle = { name -> onSetCategoryHidden(name, name !in hidden) },
            onDismiss = { showCategories = false },
        )
    }
    if (showInfo) {
        AboutDialog(
            refreshing = refreshing,
            onRefresh = onRefresh,
            onDismiss = { showInfo = false },
        )
    }
}

/** Cabecera al estilo del overlay: logo, "entrada" de bienvenida y panel coral con una recomendación. */
@Composable
private fun Header(
    videoCount: Int,
    categoryCount: Int,
    status: String,
    pick: Entry?,
    onPlay: (Video) -> Unit,
    onInfo: () -> Unit,
    onCategories: () -> Unit,
    onRoulette: () -> Unit,
    onSearch: () -> Unit,
    live: Boolean?,
    onLive: () -> Unit,
    rouletteFocus: FocusRequester,
) {
    Row(
        Modifier.fillMaxWidth().height(180.dp).background(Paper),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CineLogo(Modifier.padding(start = Gutter, end = 20.dp))

        Column(
            Modifier
                .weight(1f)
                .ticketBorder()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                "TE DOY LA BIENVENIDA A MI CINE",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
                color = Coral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "$videoCount VÍDEOS · $categoryCount CATEGORÍAS · $status",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp).height(2.dp).background(Ink))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CineButton(
                    "Ruleta",
                    onClick = onRoulette,
                    filled = true,
                    compact = true,
                    modifier = Modifier.focusRequester(rouletteFocus),
                )
                CineButton("Buscar", onClick = onSearch, compact = true, maxLines = 1)
                // Solo si el Cine está emitiendo (o si no se ha podido comprobar, para no perder el botón).
                if (live != false) {
                    CineButton(if (live == true) "● En directo" else "● Directo", onClick = onLive, compact = true, maxLines = 1)
                }
            }
        }

        // Panel coral con el lado izquierdo en diagonal, como el "on air" del directo:
        // muestra el vídeo del día. (No hay forma pública de saber qué se emite ahora.)
        Box(
            Modifier
                .width(380.dp)
                .fillMaxHeight()
                .drawBehind {
                    val slant = 64.dp.toPx()
                    drawPath(
                        Path().apply {
                            moveTo(slant, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        },
                        Coral,
                    )
                    drawLine(Ink, Offset(slant, 0f), Offset(0f, size.height), 3.dp.toPx())
                }
                .padding(start = 84.dp, end = Gutter, top = 12.dp, bottom = 12.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "hoy toca",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = Script, fontSize = 24.sp),
                        color = Color.White,
                        softWrap = false,
                        modifier = Modifier.weight(1f),
                    )
                    RoundButton("⚙", onClick = onCategories)
                    Spacer(Modifier.width(8.dp))
                    RoundButton("i", onClick = onInfo)
                }
                if (pick != null) {
                    val shape = RoundedCornerShape(8.dp)
                    val interaction = remember { MutableInteractionSource() }
                    val focused by interaction.collectIsFocusedAsState()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            onClick = { onPlay(pick.video) },
                            interactionSource = interaction,
                            modifier = Modifier.width(124.dp).aspectRatio(16f / 9f),
                            scale = CardDefaults.scale(focusedScale = 1.08f),
                            border = CardDefaults.border(
                                border = Border(BorderStroke(2.dp, Ink), shape = shape),
                                focusedBorder = Border(BorderStroke(4.dp, Paper), shape = shape),
                            ),
                            shape = CardDefaults.shape(shape),
                        ) {
                            AsyncImage(
                                model = pick.video.thumbnailUrl,
                                contentDescription = pick.video.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Pill(pick.video.durationLabel)
                            Text(
                                pick.category,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        pick.video.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .then(if (focused) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier),
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoRow(
    title: String,
    videos: List<Video>,
    library: LibraryState,
    actions: VideoActions,
    symbol: Char? = null,
) {
    // Al entrar en una fila se vuelve al último vídeo enfocado en ella; si es la primera vez
    // (o ese vídeo ya no está en pantalla) se empieza siempre por el primero, en vez de por el
    // que geométricamente quede debajo, que podía ser el 1 o el 2 según la fila de arriba.
    val first = remember { FocusRequester() }
    Column {
        SectionTitle(title, symbol, Modifier.padding(start = Gutter, bottom = 14.dp))
        LazyRow(
            modifier = Modifier.focusRestorer { first },
            contentPadding = PaddingValues(horizontal = Gutter, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            itemsIndexed(videos, key = { _, v -> v.url }) { i, video ->
                VideoCard(
                    video = video,
                    marks = library.marks(video),
                    onClick = { actions.play(video) },
                    onLongClick = { actions.toggleFavorite(video) },
                    focusRequester = first.takeIf { i == 0 },
                )
            }
        }
    }
}
