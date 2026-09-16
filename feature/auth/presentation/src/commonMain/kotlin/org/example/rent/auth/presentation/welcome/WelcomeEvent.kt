package org.example.rent.auth.presentation.welcome

sealed interface WelcomeEvent {
    /** Authentication completed — the user is signed in. */
    data object LoginSuccess : WelcomeEvent
}
