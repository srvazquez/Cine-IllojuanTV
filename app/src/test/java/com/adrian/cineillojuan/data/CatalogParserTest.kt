package com.adrian.cineillojuan.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogParserTest {

    @Test
    fun findsBundleInHtml() {
        val html = """<script type="module" crossorigin src="/assets/index-D9yAe-Ov.js"></script>"""
        assertEquals("/assets/index-D9yAe-Ov.js", CatalogParser.findBundlePath(html))
        assertNull(CatalogParser.findBundlePath("<html></html>"))
    }

    @Test
    fun extractsCatalogFromBundle() {
        // Un JSON.parse anterior que no es el catálogo, y escapes de template literal en el bueno.
        val js = """var a=JSON.parse(`{"x":1}`);const Hu=JSON.parse(`[{"name":"KARMALAND","videos":[""" +
            """{"name":"Dice \\"hola\\" y cobra 5\$","duration":3725,"url":"https://www.youtube.com/watch?v=abc",""" +
            """"imageName":"KARMA_1","played":"2025-11-21T19:03:08+01:00","nuevo":true},""" +
            """{"name":"Sin emitir","duration":60,"url":"https://www.youtube.com/watch?v=def"}]}]`);foo()"""

        val cats = CatalogParser.parseBundle(js)

        assertEquals(1, cats.size)
        assertEquals("KARMALAND", cats[0].name)
        assertEquals("""Dice "hola" y cobra 5$""", cats[0].videos[0].name)
        assertEquals(3725L, cats[0].videos[0].duration)
        assertEquals("1:02:05", cats[0].videos[0].durationLabel)
        assertNull(cats[0].videos[1].played)
    }
}
