package org.example.rent.auth.presentation.qrscanner

import dev.icerock.moko.permissions.PermissionState

sealed interface QrScannerAction {
    /** A QR code was decoded; [payload] is the opaque login token. */
    data class OnQrScanned(val payload: String) : QrScannerAction

    /** The camera permission flow (owned by the composable) resolved to [state]. */
    data class OnPermissionResult(val state: PermissionState) : QrScannerAction

    /** Clears the last error so the user can scan again. */
    data object OnRetry : QrScannerAction
}
