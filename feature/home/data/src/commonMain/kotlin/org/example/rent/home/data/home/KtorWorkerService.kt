package org.example.rent.home.data.home

import io.ktor.client.HttpClient
import org.example.rent.core.data.networking.get
import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.Result
import org.example.rent.core.domain.util.map
import org.example.rent.home.data.dto.MeResponse
import org.example.rent.home.data.mapper.toDomain
import org.example.rent.home.domain.Worker
import org.example.rent.home.domain.WorkerRepository

/**
 * Fetches the authenticated worker's profile. The `Authorization: Bearer` header is attached
 * automatically by the shared [HttpClient]'s Auth plugin from the tokens saved at login.
 */
class KtorWorkerService(
    private val httpClient: HttpClient,
) : WorkerRepository {

    override suspend fun getMe(): Result<Worker, DataError.Remote> {
        return httpClient.get<MeResponse>(route = "worker-portal/me")
            .map { it.worker.toDomain() }
    }
}
