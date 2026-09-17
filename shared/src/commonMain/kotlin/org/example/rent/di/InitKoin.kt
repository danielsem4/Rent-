package org.example.rent.di

import org.example.rent.auth.data.di.authDataModule
import org.example.rent.auth.presentation.di.authPresentationModule
import org.example.rent.core.data.di.coreDataModule
import org.example.rent.home.data.di.homeDataModule
import org.example.rent.home.presentation.di.homePresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Starts the global Koin container with every app module. Call once per process from the platform
 * entry point ([RentApplication] on Android, `MainViewController` on iOS); Compose then reads the
 * started container via `KoinContext` in `App()`.
 *
 * [config] lets each platform contribute platform-only wiring — Android passes `androidContext(...)`
 * so the Keystore-backed session storage can resolve a `Context`.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            authPresentationModule,
            authDataModule,
            homeDataModule,
            homePresentationModule,
        )
    }
}
