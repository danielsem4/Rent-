package org.example.rent.auth.presentation.welcome

import org.example.rent.core.presentation.util.UiText

/** The stages of the inline login flow, swapped in place in the Welcome screen's action area. */
enum class WelcomeStep {
    /** The login options (scan QR / continue with phone). */
    OPTIONS,

    /** Phone-number entry. */
    PHONE,

    /** Verification-code entry. */
    CODE,
}

data class WelcomeState(
    val step: WelcomeStep = WelcomeStep.OPTIONS,
    val phoneNumber: String = "",
    val code: String = "",
    /**
     * Non-null when the CODE step was reached via QR scan: the scanned token is verified together
     * with the entered code (instead of the phone number).
     */
    val qrToken: String? = null,
    val isLoading: Boolean = false,
    /** A resend request is in flight (kept separate from [isLoading] so the code field stays usable). */
    val isResending: Boolean = false,
    val error: UiText? = null,
)
