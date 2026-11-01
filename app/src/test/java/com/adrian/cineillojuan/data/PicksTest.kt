package com.adrian.cineillojuan.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PicksTest {

    private val entries = listOf(
        Entry("KARMALAND", Video(name = "MI CASICA 🏠 - KARMALVND #2", url = "u1")),
        Entry("JUEGOS DE TERROR CON ANDRÉS", Video(name = "La casa de los demonios", url = "u2")),
        Entry("GRAND THEFT AUTO", Video(name = "Marbella Vice: el regreso", url = "u3")),
    )

    @Test
    fun searchIgnoresCaseAndAccents() {
        assertEquals(listOf("u2"), search(entries, "andres terror").map { it.video.url })
        assertEquals(listOf("u1"), search(entries, "CASICA").map { it.video.url })
        assertEquals(listOf("u2"), search(entries, "casa").map { it.video.url })
        // Las palabras pueden estar repartidas entre el título y la categoría.
        assertEquals(listOf("u3"), search(entries, "marbella grand").map { it.video.url })
        assertTrue(search(entries, "   ").isEmpty())
    }

    @Test
    fun dailyPickIsStableForTheDayAndPrefersUnseen() {
        val day = LocalDate.of(2026, 11, 1)
        assertEquals(dailyPick(entries, emptySet(), day), dailyPick(entries.reversed(), emptySet(), day))
        assertEquals("u3", dailyPick(entries, setOf("u1", "u2"), day)?.video?.url)
        // Si ya está todo visto, sigue recomendando algo.
        assertTrue(dailyPick(entries, setOf("u1", "u2", "u3"), day) != null)
    }
}
