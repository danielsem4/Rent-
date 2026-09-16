package org.example.rent.auth.presentation.di

import org.example.rent.auth.presentation.qrscanner.QrScannerViewModel
import org.example.rent.auth.presentation.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::QrScannerViewModel)
    viewModelOf(::WelcomeViewModel)
}
