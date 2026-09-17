package org.example.rent.home.presentation.home

sealed interface HomeAction {
    /** Re-fetch the worker profile after an error. */
    data object OnRetry : HomeAction
}
