package org.example.rent.auth.presentation.welcome

sealed interface WelcomeAction {
    /** Reveal the phone-number entry in place of the login options. */
    data object OnContinueWithPhone : WelcomeAction
    data class OnPhoneNumberChange(val phoneNumber: String) : WelcomeAction
    data class OnCodeChange(val code: String) : WelcomeAction
    data object OnSubmitPhoneNumber : WelcomeAction
    data object OnSubmitCode : WelcomeAction

    /** Request a fresh OTP for the in-progress login (phone or QR). */
    data object OnResendCode : WelcomeAction

    /** The QR scanner fired the OTP and handed back the scanned token; jump to the CODE step. */
    data class OnQrTokenReceived(val qrToken: String) : WelcomeAction

    /** Step back one stage: CODE → PHONE, or PHONE → OPTIONS. */
    data object OnBack : WelcomeAction
}
