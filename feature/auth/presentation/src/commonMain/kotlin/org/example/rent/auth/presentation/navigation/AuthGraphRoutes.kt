package org.example.rent.auth.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AuthGraphRoutes {
    @Serializable
    data object Graph : AuthGraphRoutes

    @Serializable
    data object Welcome : AuthGraphRoutes

    @Serializable
    data object QrScanner : AuthGraphRoutes
}
