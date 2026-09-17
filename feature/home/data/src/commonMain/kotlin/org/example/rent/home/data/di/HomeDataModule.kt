package org.example.rent.home.data.di

import org.example.rent.home.data.home.KtorWorkerService
import org.example.rent.home.domain.WorkerRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val homeDataModule = module {
    singleOf(::KtorWorkerService) bind WorkerRepository::class
}
