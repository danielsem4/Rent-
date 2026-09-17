package org.example.rent.home.domain

import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.Result

interface WorkerRepository {
    /**
     * Fetches the authenticated worker's profile from `worker-portal/me`. The request is
     * authenticated with the Bearer token persisted at login (attached automatically by the shared
     * HTTP client).
     */
    suspend fun getMe(): Result<Worker, DataError.Remote>
}
