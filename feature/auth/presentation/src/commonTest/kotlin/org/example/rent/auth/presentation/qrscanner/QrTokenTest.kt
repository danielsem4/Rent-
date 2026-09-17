package org.example.rent.auth.presentation.qrscanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QrTokenTest {

    private val token = "a1b2c3d4e5f60718a1b2c3d4e5f60718" // 32 hex chars

    @Test
    fun `extracts t param from the onboard deep link`() {
        val scanned = "https://app.rent.example/w/onboard?t=$token"
        assertEquals(token, extractQrToken(scanned))
    }

    @Test
    fun `extracts t param when other query params follow`() {
        val scanned = "https://app.rent.example/w/onboard?t=$token&lang=en"
        assertEquals(token, extractQrToken(scanned))
    }

    @Test
    fun `strips a trailing fragment`() {
        val scanned = "https://app.rent.example/w/onboard?t=$token#section"
        assertEquals(token, extractQrToken(scanned))
    }

    @Test
    fun `accepts a bare token`() {
        assertEquals(token, extractQrToken(token))
    }

    @Test
    fun `trims surrounding whitespace`() {
        assertEquals(token, extractQrToken("  $token  "))
    }

    @Test
    fun `rejects a non-login url`() {
        assertNull(extractQrToken("https://example.com/some/other/page"))
    }

    @Test
    fun `rejects a token that is too short`() {
        assertNull(extractQrToken("https://app.rent.example/w/onboard?t=abc123"))
    }

    @Test
    fun `rejects a non-hex token`() {
        val notHex = "z".repeat(32)
        assertNull(extractQrToken("https://app.rent.example/w/onboard?t=$notHex"))
    }

    @Test
    fun `rejects blank input`() {
        assertNull(extractQrToken("   "))
    }
}
