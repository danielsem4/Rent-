package org.example.rent.home.presentation.home

import org.example.rent.home.domain.Worker

/** A single labelled field rendered as a row on the Home screen. */
data class WorkerInfoRow(
    val label: String,
    val value: String,
)

/** Presentation model for the Home screen: a display name plus the populated info rows. */
data class WorkerUi(
    val displayName: String,
    val rows: List<WorkerInfoRow>,
)

fun Worker.toWorkerUi(): WorkerUi {
    val displayName = when {
        nameEn.isNotBlank() -> nameEn
        nameHe.isNotBlank() -> nameHe
        else -> "Worker"
    }

    // Only surface fields the server actually populated.
    val rows = buildList {
        add("Name (Hebrew)" to nameHe)
        add("Name (English)" to nameEn)
        add("Nationality" to nationality)
        add("Phone" to phone)
        add("Entry date" to entryDate)
        add("Preferred language" to preferredLanguage)
        add("Passport expiry" to passportExpiry)
        add("Visa type" to visaType)
        add("Visa expiry" to visaExpiry)
        add("Insurance provider" to insuranceProvider)
        add("Insurance coverage" to insuranceCoverageType)
        add("Insurance expiry" to insuranceExpiry)
        add("Employer" to employer)
        add("Property" to propertyId)
    }.filter { it.second.isNotBlank() }
        .map { WorkerInfoRow(label = it.first, value = it.second) }

    return WorkerUi(displayName = displayName, rows = rows)
}
