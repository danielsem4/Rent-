package org.example.rent.auth.data.di

import org.example.rent.auth.data.auth.KtorAuthService
import org.example.rent.auth.domain.AuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authDataModule = module {
    singleOf(::KtorAuthService) bind AuthRepository::class
}
