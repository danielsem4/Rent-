package org.example.rent.core.data.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.example.rent.core.data.auth.KeystoreSessionStorage
import org.example.rent.core.domain.auth.SessionStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformCoreDataModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<SessionStorage> { KeystoreSessionStorage(androidContext()) }
}
