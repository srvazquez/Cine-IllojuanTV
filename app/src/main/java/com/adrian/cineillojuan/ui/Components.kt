package com.adrian.cineillojuan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/** Logo "Cine IlloJuan": "Cine" manuscrito en coral sobre "IlloJuan" en negro. */
@Composable
fun CineLogo(modifier: Modifier = Modifier) {
    Box(modifier) {
        Text(
            "IlloJuan",
            style = TextStyle(fontFamily = Headline, fontSize = 52.sp, color = Ink),
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 30.dp),
        )
        Text(
            "Cine",
            style = TextStyle(fontFamily = Script, fontSize = 30.sp, color = Coral),
            modifier = Modifier.offset(x = 18.dp).rotate(-10f),
        )
    }
}

/** Botón píldora con borde negro; al enfocarlo se rellena de coral. */
@Composable
fun CineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
    /** Más pequeño, para que quepan varios en una fila. */
    compact: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
) {
    val pill = RoundedCornerShape(50)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = if (compact) PaddingValues(horizontal = 14.dp, vertical = 8.dp) else ButtonDefaults.ContentPadding,
        shape = ButtonDefaults.shape(pill),
        colors = ButtonDefaults.colors(
            containerColor = if (filled) Ink else Paper,
            contentColor = if (filled) Color.White else Ink,
            focusedContainerColor = Coral,
            focusedContentColor = Color.White,
            disabledContainerColor = Paper.copy(alpha = 0.6f),
            disabledContentColor = Muted,
        ),
        border = ButtonDefaults.border(
            border = Border(BorderStroke(2.dp, Ink), shape = pill),
            focusedBorder = Border(BorderStroke(3.dp, Ink), shape = pill),
        ),
    ) {
        Text(
            text.uppercase(),
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Botón redondo con un símbolo, como la "i" del aviso o el ⚙ de las categorías. */
@Composable
fun RoundButton(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        contentPadding = PaddingValues(0.dp),
        shape = ButtonDefaults.shape(CircleShape),
        colors = ButtonDefaults.colors(
            containerColor = Paper,
            contentColor = Ink,
            focusedContainerColor = Ink,
            focusedContentColor = Color.White,
        ),
        border = ButtonDefaults.border(
            border = Border(BorderStroke(2.dp, Ink), shape = CircleShape),
            focusedBorder = Border(BorderStroke(3.dp, Paper), shape = CircleShape),
        ),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(symbol, style = TextStyle(fontFamily = Headline, fontWeight = FontWeight.Black, fontSize = 22.sp))
        }
    }
}

/** Píldora pequeña como el contador de tiempo del overlay. */
@Composable
fun Pill(text: String, modifier: Modifier = Modifier, color: Color = Ink) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .background(Paper, RoundedCornerShape(50))
            .border(2.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 2.dp),
    )
}

/** Recuadro con borde discontinuo, como las entradas de cine del overlay. */
fun Modifier.ticketBorder(color: Color = Ink, width: Dp = 2.dp, radius: Dp = 6.dp) = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14.dp.toPx(), 7.dp.toPx())),
        ),
    )
}

/** Tira de película con perforaciones. */
@Composable
fun FilmStrip(modifier: Modifier = Modifier, height: Dp = 22.dp) {
    Canvas(modifier.fillMaxWidth().height(height)) {
        drawRect(Ink)
        val holeW = 12.dp.toPx()
        val holeH = size.height * 0.42f
        val step = 26.dp.toPx()
        var x = step / 2
        while (x < size.width) {
            drawRoundRect(
                color = Cream,
                topLeft = Offset(x, (size.height - holeH) / 2),
                size = Size(holeW, holeH),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
            x += step
        }
    }
}

/** Título de fila: letra de la categoría en círculo coral + nombre en mayúsculas. */
@Composable
fun SectionTitle(title: String, letter: Char? = null, modifier: Modifier = Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (letter != null) {
            Box(
                Modifier
                    // Las letras van en blanco sobre coral; un símbolo como ♥ (emoji rojo) sobre blanco.
                    .background(if (letter.isLetter()) Coral else Paper, RoundedCornerShape(50))
                    .border(2.dp, Ink, RoundedCornerShape(50))
                    .padding(horizontal = 11.dp, vertical = 1.dp),
            ) {
                Text(letter.toString(), style = MaterialTheme.typography.titleMedium, color = if (letter.isLetter()) Color.White else Coral)
            }
        }
        Text(title.uppercase(), style = MaterialTheme.typography.titleLarge, color = Ink)
        Text("✦", style = MaterialTheme.typography.titleMedium, color = Coral)
    }
}
