package com.adrian.cineillojuan.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveStatusTest {

    @Test
    fun parsesDecapiUptime() {
        assertEquals(false, LiveStatus.parseUptime("illojuan is offline"))
        assertEquals(true, LiveStatus.parseUptime("32 minutes, 57 seconds"))
        assertEquals(true, LiveStatus.parseUptime("1 day, 2 hours"))
        // Errores u otras respuestas: no se sabe.
        assertEquals(null, LiveStatus.parseUptime("User not found: illojuan"))
    }
}
