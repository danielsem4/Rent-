package org.example.rent.core.data.networking

import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.rent.core.data.networking.dto.RefreshTokenRequest
import org.example.rent.core.data.networking.dto.RefreshTokenResponse
import org.example.rent.core.domain.auth.SessionStorage
import org.example.rent.core.domain.util.Result

/**
 * Builds the shared Ktor [HttpClient]. Engine is injected so this stays platform-agnostic
 * (OkHttp on Android, Darwin on iOS). Reads/writes tokens through [SessionStorage] and attaches
 * `Authorization: Bearer` to authed calls via the [Auth] plugin.
 */
class HttpClientFactory(
    private val sessionStorage: SessionStorage,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        // Omit null request fields (e.g. don't send "qrToken": null on the phone-verify body, which
        // a strict backend schema can reject).
        explicitNulls = false
    }

    fun create(engine: HttpClientEngine): HttpClient {
        // Bare client used ONLY for the token-refresh call. It intentionally has no `Auth` plugin so
        // it never attaches a stale/mid-refresh Authorization header. Reuses the same engine.
        val refreshClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000L
                socketTimeoutMillis = 20_000L
            }
            defaultRequest { contentType(ContentType.Application.Json) }
        }

        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000L
                socketTimeoutMillis = 20_000L
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        KermitLogger.withTag("HttpClient").d { message }
                    }
                }
                // DEV: log everything — request/response line, headers AND bodies — so failing auth
                // calls are fully visible. NOTE: this logs tokens; dial back to INFO before release.
                level = LogLevel.ALL
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = sessionStorage.getAccessToken()
                        val refreshToken = sessionStorage.getRefreshToken()
                        if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }
                    refreshTokens {
                        val refreshToken = sessionStorage.getRefreshToken()
                        if (refreshToken.isNullOrBlank()) return@refreshTokens null
                        val response = refreshClient.post<RefreshTokenRequest, RefreshTokenResponse>(
                            route = UrlConstants.REFRESH_ENDPOINT,
                            body = RefreshTokenRequest(refreshToken = refreshToken),
                        )
                        when (response) {
                            is Result.Success -> {
                                val newAccess = response.data.accessToken
                                val newRefresh = response.data.refreshToken ?: refreshToken
                                if (newAccess.isNullOrBlank()) {
                                    sessionStorage.clear()
                                    null
                                } else {
                                    sessionStorage.set(newAccess, newRefresh)
                                    BearerTokens(newAccess, newRefresh)
                                }
                            }
                            is Result.Failure -> {
                                sessionStorage.clear()
                                null
                            }
                        }
                    }
                }
            }
            defaultRequest { contentType(ContentType.Application.Json) }
        }
    }
}
