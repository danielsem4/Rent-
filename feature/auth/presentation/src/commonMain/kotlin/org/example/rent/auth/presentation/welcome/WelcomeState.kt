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
    val isLoading: Boolean = false,
    val error: UiText? = null,
)
