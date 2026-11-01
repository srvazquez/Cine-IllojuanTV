package com.adrian.cineillojuan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/** "Mis categorías": qué categorías salen en la portada, la ruleta y el "hoy toca". */
@Composable
fun CategoriesDialog(
    categories: List<String>,
    hidden: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .width(920.dp)
                .background(Paper, RoundedCornerShape(16.dp))
                .border(3.dp, Ink, RoundedCornerShape(16.dp)),
        ) {
            FilmStrip(Modifier.clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)))
            Column(Modifier.padding(horizontal = 28.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("MIS CATEGORÍAS", style = MaterialTheme.typography.headlineSmall, color = Coral)
                        Text(
                            "Desmarca las que no te interesen: no saldrán en la portada, la ruleta ni el \"hoy toca\". " +
                                "La búsqueda sigue mostrándolo todo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                        )
                    }
                    CineButton("Listo", onClick = onDismiss, filled = true)
                }
                Box(Modifier.padding(vertical = 12.dp).height(2.dp).fillMaxWidth().background(Ink))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 330.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(categories) { i, name ->
                        val shown = name !in hidden
                        val letter = ('A' + i).takeIf { it <= 'Z' }?.let { "$it · " } ?: ""
                        CineButton(
                            "${if (shown) "✓" else "✕"} $letter$name",
                            onClick = { onToggle(name) },
                            filled = shown,
                            compact = true,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (i == 0) Modifier.focusRequester(firstFocus) else Modifier),
                        )
                    }
                }
                Text(
                    "${categories.size - hidden.count { it in categories }} de ${categories.size} categorías visibles",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}
