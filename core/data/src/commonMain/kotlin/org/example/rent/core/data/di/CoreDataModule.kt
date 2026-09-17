package org.example.rent.core.data.di

import io.ktor.client.HttpClient
import org.example.rent.core.data.networking.HttpClientFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Provides the platform HttpClientEngine (OkHttp on Android, Darwin on iOS) and the platform-backed
 * [org.example.rent.core.domain.auth.SessionStorage] (Android Keystore / iOS Keychain).
 */
expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)

    single<HttpClient> {
        HttpClientFactory(get()).create(get())
    }
}
