package org.example.rent.core.data.networking

import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.Result
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext

private fun logCallFailure(e: Throwable) {
    KermitLogger.withTag("HttpClient").e(e) { "call failed: ${e::class.simpleName}: ${e.message}" }
}

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Remote>,
): Result<T, DataError.Remote> {
    return try {
        val response = execute()
        handleResponse(response)
    } catch (e: UnknownHostException) {
        logCallFailure(e)
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: UnresolvedAddressException) {
        logCallFailure(e)
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: ConnectException) {
        logCallFailure(e)
        Result.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: SocketTimeoutException) {
        logCallFailure(e)
        Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: HttpRequestTimeoutException) {
        logCallFailure(e)
        Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: SerializationException) {
        logCallFailure(e)
        Result.Failure(DataError.Remote.SERIALIZATION_ERROR)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        logCallFailure(e)
        Result.Failure(DataError.Remote.UNKNOWN)
    }
}
