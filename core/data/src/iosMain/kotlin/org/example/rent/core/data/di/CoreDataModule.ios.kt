package org.example.rent.core.data.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.example.rent.core.data.auth.KeychainSessionStorage
import org.example.rent.core.domain.auth.SessionStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformCoreDataModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<SessionStorage> { KeychainSessionStorage() }
}
