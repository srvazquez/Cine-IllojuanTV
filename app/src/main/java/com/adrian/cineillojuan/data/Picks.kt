package com.adrian.cineillojuan.data

import java.text.Normalizer
import java.time.LocalDate
import kotlin.random.Random

/**
 * El "hoy toca": un vídeo al azar que cambia cada día (el mismo durante todo el día),
 * preferiblemente uno que todavía no se haya visto.
 */
fun dailyPick(entries: List<Entry>, seen: Set<String>, day: LocalDate = LocalDate.now()): Entry? {
    val pool = entries.filter { it.video.url !in seen }.ifEmpty { entries }.sortedBy { it.video.url }
    return pool.randomOrNull(Random(day.toEpochDay()))
}

/** Minúsculas y sin tildes, para buscar sin preocuparse de cómo se escriba. */
fun normalize(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()

/** Vídeos cuyo título o categoría contienen todas las palabras buscadas. */
fun search(entries: List<Entry>, query: String): List<Entry> {
    val words = normalize(query).split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return emptyList()
    return entries.filter { e ->
        val haystack = normalize(e.video.name + " " + e.category)
        words.all { it in haystack }
    }
}

/** Filtros de duración de la ruleta. */
enum class DurationFilter(val label: String, val range: LongRange) {
    ANY("Cualquiera", 0L..Long.MAX_VALUE),
    SHORT("Menos de 30 min", 0L until 30 * 60L),
    MEDIUM("30 min – 1:30 h", 30 * 60L until 90 * 60L),
    LONG("Más de 1:30 h", 90 * 60L..Long.MAX_VALUE),
}
