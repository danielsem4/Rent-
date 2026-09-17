package org.example.rent.home.data.dto

import kotlinx.serialization.Serializable

/** `GET /api/worker-portal/me` returns `{ worker: { … } }`. */
@Serializable
data class MeResponse(
    val worker: WorkerDto = WorkerDto(),
)

@Serializable
data class WorkerDto(
    val nameHe: String? = null,
    val nameEn: String? = null,
    val nationality: String? = null,
    val phone: String? = null,
    val entryDate: String? = null,
    val preferredLanguage: String? = null,
    val passportExpiry: String? = null,
    val visaType: String? = null,
    val visaExpiry: String? = null,
    val insuranceProvider: String? = null,
    val insuranceCoverageType: String? = null,
    val insuranceExpiry: String? = null,
    // `employer`/`propertyId` are treated as plain strings for now; adjust to nested DTOs if the
    // server returns objects.
    val employer: String? = null,
    val propertyId: String? = null,
)
