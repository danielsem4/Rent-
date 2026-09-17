package org.example.rent.home.domain

/**
 * The authenticated worker's basic profile, as returned by `GET /api/worker-portal/me`. Blank
 * fields mean the server had no value for them.
 */
data class Worker(
    val nameHe: String = "",
    val nameEn: String = "",
    val nationality: String = "",
    val phone: String = "",
    val entryDate: String = "",
    val preferredLanguage: String = "",
    val passportExpiry: String = "",
    val visaType: String = "",
    val visaExpiry: String = "",
    val insuranceProvider: String = "",
    val insuranceCoverageType: String = "",
    val insuranceExpiry: String = "",
    val employer: String = "",
    val propertyId: String = "",
)
