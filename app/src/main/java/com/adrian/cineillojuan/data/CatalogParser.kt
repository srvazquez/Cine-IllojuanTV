package com.adrian.cineillojuan.data

import kotlinx.serialization.json.Json

/**
 * illojuan.tv no tiene API: el catálogo va embebido en el bundle de la web como
 * `JSON.parse(`[{"name":"CLIPS ALEATORIOS","videos":[...]}, ...]`)`.
 * Este parser localiza el bundle desde el HTML y saca ese JSON.
 */
object CatalogParser {

    val json = Json { ignoreUnknownKeys = true }

    private val bundleRegex = Regex("""/assets/index-[A-Za-z0-9_\-]+\.js""")

    fun findBundlePath(html: String): String? = bundleRegex.find(html)?.value

    fun parseBundle(js: String): List<Category> {
        var from = 0
        while (true) {
            val start = js.indexOf("JSON.parse(`", from)
            if (start < 0) break
            val (literal, end) = readTemplateLiteral(js, start + "JSON.parse(`".length)
            from = end
            if (!literal.trimStart().startsWith("[{\"name\"") || "\"videos\"" !in literal) continue
            val categories = runCatching { json.decodeFromString<List<Category>>(literal) }.getOrNull()
            if (!categories.isNullOrEmpty()) return categories
        }
        throw IllegalStateException("No se encontró el catálogo en el bundle")
    }

    fun parseJson(raw: String): List<Category> = json.decodeFromString(raw)

    /** Lee un template literal de JS desde [begin] (justo tras la comilla ` de apertura) y lo desescapa. */
    private fun readTemplateLiteral(js: String, begin: Int): Pair<String, Int> {
        val sb = StringBuilder()
        var i = begin
        while (i < js.length) {
            val c = js[i]
            when {
                c == '\\' && i + 1 < js.length -> {
                    when (val n = js[i + 1]) {
                        // En el literal, \\ es una barra real (p. ej. el \" de una cadena JSON).
                        '\\', '`', '$' -> sb.append(n)
                        'n' -> sb.append('\n')
                        't' -> sb.append('\t')
                        else -> sb.append('\\').append(n)
                    }
                    i += 2
                }
                c == '`' -> return sb.toString() to i + 1
                else -> {
                    sb.append(c); i++
                }
            }
        }
        return sb.toString() to i
    }
}
