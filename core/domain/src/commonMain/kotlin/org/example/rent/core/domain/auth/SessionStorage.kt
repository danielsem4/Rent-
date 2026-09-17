package org.example.rent.core.domain.auth

/**
 * Holds the authenticated session's tokens. Pure Kotlin, no platform or framework dependencies so
 * it can live in the domain layer and be read from both the networking client (to attach
 * `Authorization: Bearer`) and the auth data source (to persist tokens after verify).
 *
 * The current implementation is in-memory only — tokens are lost on process death. Swap for a
 * DataStore-backed implementation once persistence across restarts is required.
 */
interface SessionStorage {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun set(accessToken: String?, refreshToken: String?)
    suspend fun clear()
}
