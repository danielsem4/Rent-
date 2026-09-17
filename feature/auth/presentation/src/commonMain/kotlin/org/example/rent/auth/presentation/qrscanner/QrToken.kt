package org.example.rent.auth.presentation.qrscanner

/**
 * Extracts the worker-auth QR token from a scanned QR payload.
 *
 * The onboarding QR encodes a deep link `https://<app-link-base>/w/onboard?t=<qrToken>`, so the token
 * is the `t` query parameter. A raw token (already just the hex string) is also accepted so the same
 * helper works whether the QR carries the full URL or the bare token.
 *
 * Returns the token only when it matches the contract's shape (16–256 hex chars), otherwise `null`
 * so the caller can reject an unrelated/invalid QR code.
 */
fun extractQrToken(scanned: String): String? {
    val raw = scanned.trim()
    val candidate = if ("t=" in raw) {
        raw.substringAfter("t=").substringBefore("&").substringBefore("#").trim()
    } else {
        raw
    }
    return candidate.takeIf { it.length in 16..256 && it.all(Char::isHexDigit) }
}

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
