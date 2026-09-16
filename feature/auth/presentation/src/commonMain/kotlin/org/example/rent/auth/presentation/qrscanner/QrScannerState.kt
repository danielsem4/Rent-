package org.example.rent.auth.presentation.qrscanner

import dev.icerock.moko.permissions.PermissionState
import org.example.rent.core.presentation.util.UiText

data class QrScannerState(
    val permission: PermissionState = PermissionState.NotDetermined,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    /** Guards against QRKit firing `onCompletion` repeatedly for the same code while we authenticate. */
    val hasScanned: Boolean = false,
)
