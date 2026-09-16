package org.example.rent.auth.presentation.qrscanner

sealed interface QrScannerEvent {
    data object ScanSuccess : QrScannerEvent
}
