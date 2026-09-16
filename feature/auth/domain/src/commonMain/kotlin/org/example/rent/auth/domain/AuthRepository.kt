package org.example.rent.auth.domain

import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.EmptyResult

interface AuthRepository {
    /**
     * Logs the user in from a scanned QR-code payload (an opaque token). On success the user is
     * considered authenticated with all their details.
     */
    suspend fun loginWithQr(token: String): EmptyResult<DataError.Remote>

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
}
