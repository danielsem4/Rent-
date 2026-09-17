package org.example.rent.auth.data.auth

import io.ktor.client.HttpClient
import org.example.rent.auth.data.dto.LogoutRequest
import org.example.rent.auth.data.dto.OkResponse
import org.example.rent.auth.data.dto.PhoneStartRequest
import org.example.rent.auth.data.dto.QrStartRequest
import org.example.rent.auth.data.dto.ResendRequest
import org.example.rent.auth.data.dto.VerifyRequest
import org.example.rent.auth.data.dto.VerifyResponse
import org.example.rent.auth.domain.AuthRepository
import org.example.rent.core.data.networking.post
import org.example.rent.core.domain.auth.SessionStorage
import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.EmptyResult
import org.example.rent.core.domain.util.Result
import org.example.rent.core.domain.util.asEmptyResult
import org.example.rent.core.domain.util.onSuccess

/**
 * Real Ktor-backed auth data source hitting the foreign-worker auth endpoints under `/api`. On a
 * successful phone/QR verify it persists the returned tokens to [SessionStorage] so subsequent
 * authed calls carry `Authorization: Bearer`.
 */
class KtorAuthService(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
) : AuthRepository {

    override suspend fun startQrLogin(qrToken: String): EmptyResult<DataError.Remote> {
        return httpClient.post<QrStartRequest, OkResponse>(
            route = "worker-auth/qr/start",
            body = QrStartRequest(qrToken = qrToken),
        ).asEmptyResult()
    }

    override suspend fun verifyQrCode(qrToken: String, code: String): EmptyResult<DataError.Remote> {
        return httpClient.post<VerifyRequest, VerifyResponse>(
            route = "worker-auth/verify",
            body = VerifyRequest(qrToken = qrToken, code = code),
        ).onSuccess { response ->
            sessionStorage.set(response.accessToken, response.refreshToken)
        }.asEmptyResult()
    }

    override suspend fun requestCode(phoneNumber: String): EmptyResult<DataError.Remote> {
        return httpClient.post<PhoneStartRequest, OkResponse>(
            route = "worker-auth/phone/start",
            body = PhoneStartRequest(phone = phoneNumber),
        ).asEmptyResult()
    }

    override suspend fun verifyCode(phoneNumber: String, code: String): EmptyResult<DataError.Remote> {
        return httpClient.post<VerifyRequest, VerifyResponse>(
            route = "worker-auth/verify",
            body = VerifyRequest(phone = phoneNumber, code = code),
        ).onSuccess { response ->
            sessionStorage.set(response.accessToken, response.refreshToken)
        }.asEmptyResult()
    }

    override suspend fun resendCode(
        phoneNumber: String?,
        qrToken: String?,
    ): EmptyResult<DataError.Remote> {
        return httpClient.post<ResendRequest, OkResponse>(
            route = "worker-auth/otp/resend",
            body = ResendRequest(phone = phoneNumber, qrToken = qrToken),
        ).asEmptyResult()
    }

    override suspend fun logout(): EmptyResult<DataError.Remote> {
        val refreshToken = sessionStorage.getRefreshToken()
        return try {
            // Only hit the endpoint when there's a token to revoke; either way we clear locally below.
            if (refreshToken.isNullOrBlank()) {
                Result.Success(Unit)
            } else {
                httpClient.post<LogoutRequest, OkResponse>(
                    route = "worker-auth/logout",
                    body = LogoutRequest(refreshToken = refreshToken),
                ).asEmptyResult()
            }
        } finally {
            // Contract: clear local tokens regardless of the server response.
            sessionStorage.clear()
        }
    }
}
