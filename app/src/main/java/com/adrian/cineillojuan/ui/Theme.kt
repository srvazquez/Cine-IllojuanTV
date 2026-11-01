@file:OptIn(ExperimentalTextApi::class)

package com.adrian.cineillojuan.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Typography
import androidx.tv.material3.lightColorScheme
import com.adrian.cineillojuan.R

// Paleta del overlay del Cine en el canal: crema, rojo coral y negro.
val Cream = Color(0xFFFBF4EA)
val Paper = Color(0xFFFFFDF9)
val Coral = Color(0xFFEE5C55)
val Ink = Color(0xFF161616)
val Muted = Color(0xFF6E655C)

private fun mona(weight: Int, width: Float = 100f) = Font(
    R.font.mona_sans,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.width(width)),
)

/** Mona Sans, la misma tipografía que usa illojuan.tv. */
val MonaSans = FontFamily(mona(400), mona(500), mona(700), mona(800), mona(900))

/** Versión estrecha y muy gruesa para titulares en mayúsculas, como los del overlay. */
val Headline = FontFamily(mona(800, 82f), mona(900, 82f))

/** Letra manuscrita para el "Cine" del logo y el "hoy toca" de la cabecera. */
val Script = FontFamily(Font(R.font.pacifico))

private fun style(family: FontFamily, weight: Int, size: Int) =
    TextStyle(fontFamily = family, fontWeight = FontWeight(weight), fontSize = size.sp)

private val CineTypography = Typography(
    displaySmall = style(Headline, 900, 44),
    headlineSmall = style(Headline, 900, 30),
    titleLarge = style(Headline, 900, 26),
    titleMedium = style(Headline, 800, 20),
    bodyLarge = style(MonaSans, 500, 18),
    bodyMedium = style(MonaSans, 500, 15),
    labelLarge = style(MonaSans, 800, 16),
    labelMedium = style(MonaSans, 700, 13),
)

@Composable
fun CineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Coral,
            onPrimary = Color.White,
            background = Cream,
            onBackground = Ink,
            surface = Cream,
            onSurface = Ink,
            surfaceVariant = Paper,
            onSurfaceVariant = Ink,
        ),
        typography = CineTypography,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            colors = SurfaceDefaults.colors(containerColor = Cream, contentColor = Ink),
            content = { content() },
        )
    }
}
