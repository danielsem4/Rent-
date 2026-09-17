package org.example.rent.home.data.mapper

import org.example.rent.home.data.dto.WorkerDto
import org.example.rent.home.domain.Worker

fun WorkerDto.toDomain(): Worker = Worker(
    nameHe = nameHe.orEmpty(),
    nameEn = nameEn.orEmpty(),
    nationality = nationality.orEmpty(),
    phone = phone.orEmpty(),
    entryDate = entryDate.orEmpty(),
    preferredLanguage = preferredLanguage.orEmpty(),
    passportExpiry = passportExpiry.orEmpty(),
    visaType = visaType.orEmpty(),
    visaExpiry = visaExpiry.orEmpty(),
    insuranceProvider = insuranceProvider.orEmpty(),
    insuranceCoverageType = insuranceCoverageType.orEmpty(),
    insuranceExpiry = insuranceExpiry.orEmpty(),
    employer = employer.orEmpty(),
    propertyId = propertyId.orEmpty(),
)
