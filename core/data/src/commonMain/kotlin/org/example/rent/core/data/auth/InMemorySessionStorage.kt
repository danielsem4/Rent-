package org.example.rent.core.data.auth

import org.example.rent.core.domain.auth.SessionStorage

/**
 * In-memory [SessionStorage] — tokens live only for the process lifetime. No longer the production
 * binding (that's `KeystoreSessionStorage` on Android / `KeychainSessionStorage` on iOS); kept as a
 * lightweight, dependency-free implementation for tests and previews.
 */
class InMemorySessionStorage : SessionStorage {

    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun set(accessToken: String?, refreshToken: String?) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clear() {
        accessToken = null
        refreshToken = null
    }
}
