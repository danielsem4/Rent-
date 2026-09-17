package org.example.rent

import androidx.compose.ui.window.ComposeUIViewController
import org.example.rent.di.initKoin
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    // Start Koin once before the first composition (idempotent across re-entry).
    if (!koinStarted) {
        koinStarted = true
        initKoin()
    }
    return ComposeUIViewController { App() }
}
