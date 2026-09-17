package org.example.rent.core.data.networking.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)
