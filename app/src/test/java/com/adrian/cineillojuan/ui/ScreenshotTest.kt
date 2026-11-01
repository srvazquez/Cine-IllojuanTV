package com.adrian.cineillojuan.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.adrian.cineillojuan.data.Catalog
import com.adrian.cineillojuan.data.CatalogParser
import com.adrian.cineillojuan.data.Origin
import com.adrian.cineillojuan.data.entries
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Renderiza las pantallas a PNG en app/build/outputs/roborazzi para revisar el diseño. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-land-xhdpi")
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val catalog = Catalog(
        CatalogParser.parseJson(File("src/main/assets/catalog.json").readText()),
        Origin.WEB,
    )

    @Test
    fun home() {
        compose.setContent {
            CineTheme {
                CatalogContent(
                    catalog,
                    library = LibraryState(),
                    actions = VideoActions(play = {}, toggleFavorite = {}),
                    onSetCategoryHidden = { _, _ -> },
                    live = true,
                    refreshing = false,
                    refreshError = null,
                    onRefresh = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/home.png")
    }

    @Test
    fun roulette() {
        val entries = catalog.categories.entries()
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CineTheme {
                RouletteDialog(entries, catalog.categories.map { it.name }, seen = emptySet(), onPlay = {}, onDismiss = {})
            }
        }
        compose.mainClock.advanceTimeBy(5_000)
        captureScreenRoboImage("build/outputs/roborazzi/roulette.png")
    }
}
