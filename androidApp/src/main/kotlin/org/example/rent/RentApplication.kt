package org.example.rent

import android.app.Application
import org.example.rent.di.initKoin
import org.koin.android.ext.koin.androidContext

/**
 * Application entry point. Starts Koin before any Activity so the global container (and its
 * Keystore-backed session storage, which needs a [android.content.Context]) is ready for `App()`.
 */
class RentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@RentApplication)
        }
    }
}
