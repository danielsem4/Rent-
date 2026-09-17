package org.example.rent.auth.presentation.qrscanner

sealed interface QrScannerEvent {
    /**
     * The scanned [qrToken] was accepted and the server fired the WhatsApp OTP. The caller should
     * hand the token back to the Welcome CODE step so the user can enter the code.
     */
    data class OtpSent(val qrToken: String) : QrScannerEvent
}
