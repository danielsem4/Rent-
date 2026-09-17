package org.example.rent.auth.domain

import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.EmptyResult

interface AuthRepository {
    /**
     * Starts the QR login: the scanned [qrToken] (an opaque onboard token) is sent to the server,
     * which fires a one-time verification code to the worker's WhatsApp. On success the caller
     * collects the code and calls [verifyQrCode] with the same [qrToken].
     */
    suspend fun startQrLogin(qrToken: String): EmptyResult<DataError.Remote>

    /**
     * Verifies the WhatsApp [code] against the scanned [qrToken]. On success the user is considered
     * authenticated and the returned tokens are persisted.
     */
    suspend fun verifyQrCode(qrToken: String, code: String): EmptyResult<DataError.Remote>

    /**
     * Requests a one-time verification code to be sent to [phoneNumber]. On success the caller can
     * proceed to collect the code and call [verifyCode].
     */
    suspend fun requestCode(phoneNumber: String): EmptyResult<DataError.Remote>

    /**
     * Verifies the [code] the user received for [phoneNumber]. On success the user is considered
     * authenticated.
     */
    suspend fun verifyCode(phoneNumber: String, code: String): EmptyResult<DataError.Remote>

    /**
     * Requests a fresh one-time code for an in-progress login. Pass exactly the single identifier the
     * flow started with: [qrToken] for the QR flow, otherwise [phoneNumber] for the phone flow.
     */
    suspend fun resendCode(
        phoneNumber: String? = null,
        qrToken: String? = null,
    ): EmptyResult<DataError.Remote>

    /**
     * Ends the current session: revokes the stored refresh token server-side and clears local tokens.
     * Local tokens are cleared regardless of the server response.
     */
    suspend fun logout(): EmptyResult<DataError.Remote>
}
