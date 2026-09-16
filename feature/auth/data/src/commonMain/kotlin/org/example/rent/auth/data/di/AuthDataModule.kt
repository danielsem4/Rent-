package org.example.rent.auth.data.di

import org.example.rent.auth.data.FakeAuthRepository
import org.example.rent.auth.domain.AuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authDataModule = module {
    singleOf(::FakeAuthRepository) bind AuthRepository::class
}
