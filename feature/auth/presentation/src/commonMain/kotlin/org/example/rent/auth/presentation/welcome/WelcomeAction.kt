package org.example.rent.auth.presentation.welcome

sealed interface WelcomeAction {
    /** Reveal the phone-number entry in place of the login options. */
    data object OnContinueWithPhone : WelcomeAction
    data class OnPhoneNumberChange(val phoneNumber: String) : WelcomeAction
    data class OnCodeChange(val code: String) : WelcomeAction
    data object OnSubmitPhoneNumber : WelcomeAction
    data object OnSubmitCode : WelcomeAction

    /** Step back one stage: CODE → PHONE, or PHONE → OPTIONS. */
    data object OnBack : WelcomeAction
}
