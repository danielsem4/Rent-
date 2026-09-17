package org.example.rent.auth.presentation.welcome

import org.example.rent.auth.domain.AuthRepository
import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.EmptyResult
import org.example.rent.core.domain.util.Result

/** Records the last resend call so tests can assert which identifier the ViewModel sent. */
class FakeAuthRepository(
    private val resendResult: EmptyResult<DataError.Remote> = Result.Success(Unit),
) : AuthRepository {

    var resendCallCount = 0
        private set
    var lastResendPhone: String? = null
        private set
    var lastResendQrToken: String? = null
        private set

    override suspend fun startQrLogin(qrToken: String): EmptyResult<DataError.Remote> = Result.Success(Unit)

    override suspend fun verifyQrCode(qrToken: String, code: String): EmptyResult<DataError.Remote> =
        Result.Success(Unit)

    override suspend fun requestCode(phoneNumber: String): EmptyResult<DataError.Remote> = Result.Success(Unit)

    override suspend fun verifyCode(phoneNumber: String, code: String): EmptyResult<DataError.Remote> =
        Result.Success(Unit)

    override suspend fun resendCode(
        phoneNumber: String?,
        qrToken: String?,
    ): EmptyResult<DataError.Remote> {
        resendCallCount++
        lastResendPhone = phoneNumber
        lastResendQrToken = qrToken
        return resendResult
    }

    override suspend fun logout(): EmptyResult<DataError.Remote> = Result.Success(Unit)
}
