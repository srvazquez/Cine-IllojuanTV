package com.adrian.cineillojuan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/** Créditos tal y como aparecen en illojuan.tv. */
private val credits = listOf(
    "Idea original, contenido y jefe supremo" to "IlloJuan",
    "Diseño" to "_franmolina",
    "Animaciones" to "rafacos22",
    "Soporte" to "Clober",
    "Administración y desarrollo" to "Álvaro",
)

/** Aviso de app no oficial, sin ánimo de lucro, y créditos del Cine IlloJuan. */
@Composable
fun AboutDialog(refreshing: Boolean, onRefresh: () -> Unit, onDismiss: () -> Unit) {
    val closeFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { closeFocus.requestFocus() } }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .width(920.dp)
                .background(Paper, RoundedCornerShape(16.dp))
                .border(3.dp, Ink, RoundedCornerShape(16.dp)),
        ) {
            FilmStrip(Modifier.clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)))
            Row(
                Modifier.padding(28.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Column(Modifier.weight(1.1f)) {
                    Text("APP NO OFICIAL", style = MaterialTheme.typography.headlineSmall, color = Coral)
                    Box(Modifier.padding(vertical = 8.dp).height(2.dp).width(220.dp).background(Ink))
                    Paragraph(
                        "Esta aplicación está hecha por un fan y no tiene ninguna relación con IlloJuan " +
                            "ni con el equipo de illojuan.tv, ni cuenta con su respaldo.",
                    )
                    Paragraph(
                        "No tiene ánimo de lucro: es gratuita, sin anuncios y no recoge ningún dato " +
                            "(lo que ves y tu lista se guardan solo en esta tele).",
                    )
                    Paragraph(
                        "Los vídeos, miniaturas y nombres pertenecen a sus autores. El catálogo se lee de " +
                            "illojuan.tv y los vídeos se reproducen en YouTube.",
                    )
                    Text(
                        "Consejo: mantén pulsado OK sobre un vídeo para añadirlo a Mi lista.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CineButton(
                            "Cerrar",
                            onClick = onDismiss,
                            filled = true,
                            modifier = Modifier.focusRequester(closeFocus),
                        )
                        CineButton(
                            if (refreshing) "Actualizando…" else "Actualizar catálogo",
                            onClick = onRefresh,
                            enabled = !refreshing,
                        )
                    }
                }
                Column(
                    Modifier
                        .weight(1f)
                        .ticketBorder()
                        .padding(20.dp),
                ) {
                    Text("CRÉDITOS DEL CINE", style = MaterialTheme.typography.titleLarge, color = Coral)
                    Spacer(Modifier.height(10.dp))
                    credits.forEach { (role, who) ->
                        Text(
                            buildAnnotatedString {
                                append("$role: ")
                                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(who) }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 3.dp),
                        )
                    }
                    Box(Modifier.padding(vertical = 10.dp).height(2.dp).fillMaxWidth().background(Ink))
                    Text("ESTA APP", style = MaterialTheme.typography.titleMedium, color = Coral)
                    Text(
                        buildAnnotatedString {
                            append("Desarrollo de la app para Android TV: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("srvazquez") }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 3.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tipografías Mona Sans y Pacifico (SIL Open Font License).",
                        style = MaterialTheme.typography.labelMedium,
                        color = Muted,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Paragraph(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 10.dp))
}
