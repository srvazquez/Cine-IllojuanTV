package com.adrian.cineillojuan.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.adrian.cineillojuan.data.Entry
import com.adrian.cineillojuan.data.search

/** Buscador por título o categoría, escribiendo o con el micrófono del mando. */
@Composable
fun SearchScreen(entries: List<Entry>, library: LibraryState, actions: VideoActions, onClose: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(entries, query) { search(entries, query) }
    val micFocus = remember { FocusRequester() }
    var fieldFocused by remember { mutableStateOf(false) }

    val voice = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { query = it }
    }
    fun listen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "¿Qué quieres ver?")
        try {
            voice.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Esta tele no tiene búsqueda por voz", Toast.LENGTH_LONG).show()
        }
    }

    BackHandler(onBack = onClose)
    LaunchedEffect(Unit) { runCatching { micFocus.requestFocus() } }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().background(Paper)) {
            Row(
                Modifier.padding(horizontal = Gutter, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("BUSCAR", style = MaterialTheme.typography.displaySmall, color = Coral)
                CineButton("🎤 Hablar", onClick = ::listen, filled = true, modifier = Modifier.focusRequester(micFocus))
                Box(
                    Modifier
                        .weight(1f)
                        .background(Cream, RoundedCornerShape(50))
                        .border(if (fieldFocused) 3.dp else 2.dp, if (fieldFocused) Coral else Ink, RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    if (query.isEmpty()) {
                        Text("…o escribe un título, un juego, un amigo…", style = MaterialTheme.typography.bodyLarge, color = Muted)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                        cursorBrush = SolidColor(Coral),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { fieldFocused = it.isFocused },
                    )
                }
                CineButton("Volver", onClick = onClose)
            }
            FilmStrip()
        }

        Text(
            when {
                query.isBlank() -> "Pulsa 🎤 y di lo que quieres ver."
                results.isEmpty() -> "Nada con «$query». Prueba con otra palabra."
                else -> "${results.size} ${if (results.size == 1) "resultado" else "resultados"} para «$query»"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = Gutter, top = 20.dp, bottom = 8.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = Gutter, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(results, key = { it.video.url }) { entry ->
                VideoCard(
                    video = entry.video,
                    marks = library.marks(entry.video),
                    onClick = { actions.play(entry.video) },
                    onLongClick = { actions.toggleFavorite(entry.video) },
                )
            }
        }
    }
}
