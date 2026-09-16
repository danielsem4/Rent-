package org.example.rent.auth.data

import kotlinx.coroutines.delay
import org.example.rent.auth.domain.AuthRepository
import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.EmptyResult
import org.example.rent.core.domain.util.Result

/**
 * Temporary in-memory stand-in for a real (Ktor-backed) auth data source. Succeeds after a short
 * delay so the login loading + success flows can be exercised end-to-end. Blank inputs fail so the
 * error paths are demoable. Replace with a real implementation calling an `AuthService` once the
 * backend exists.
 */
class FakeAuthRepository : AuthRepository {
    override suspend fun loginWithQr(token: String): EmptyResult<DataError.Remote> {
        delay(1_000)
        if (token.isBlank()) return Result.Failure(DataError.Remote.UNKNOWN)
        return Result.Success(Unit)
    }

    override suspend fun requestCode(phoneNumber: String): EmptyResult<DataError.Remote> {
        delay(1_000)
        if (phoneNumber.isBlank()) return Result.Failure(DataError.Remote.BAD_REQUEST)
        return Result.Success(Unit)
    }

    override suspend fun verifyCode(phoneNumber: String, code: String): EmptyResult<DataError.Remote> {
        delay(1_000)
        if (code.isBlank()) return Result.Failure(DataError.Remote.BAD_REQUEST)
        // Pretend "000000" is always the wrong code so the failure path is demoable.
        if (code == "000000") return Result.Failure(DataError.Remote.UNAUTHORIZED)
        return Result.Success(Unit)
    }
}
