package org.example.rent.auth.presentation.welcome

sealed interface WelcomeEvent {
    /** Authentication completed — the user is signed in. */
    data object LoginSuccess : WelcomeEvent

    /** A fresh verification code was sent — show a confirmation to the user. */
    data object CodeResent : WelcomeEvent
}
