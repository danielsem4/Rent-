package org.example.rent.auth.presentation.qrscanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.example.rent.core.designsystem.components.buttons.AppButton
import org.example.rent.core.designsystem.components.layouts.AppBaseScreen
import org.example.rent.core.designsystem.theme.AppDimens
import org.example.rent.core.designsystem.theme.AppSpacing
import org.example.rent.core.designsystem.theme.AppTheme
import org.example.rent.core.designsystem.theme.extended
import org.example.rent.core.presentation.util.DeviceConfiguration
import org.example.rent.core.presentation.util.ObserveAsEvents
import org.example.rent.core.presentation.util.currentDeviceConfiguration
import org.koin.compose.viewmodel.koinViewModel
import qrscanner.CameraLens
import qrscanner.QrScanner

@Composable
fun QrScannerRoot(
    onOtpSent: (qrToken: String) -> Unit,
    onBack: () -> Unit,
    viewModel: QrScannerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is QrScannerEvent.OtpSent -> onOtpSent(event.qrToken)
        }
    }

    // The camera permission is driven from the composable, where the moko controller can bind to the
    // platform lifecycle. Results are reported back into the ViewModel's state.
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)
    val scope = rememberCoroutineScope()

    val requestPermission: () -> Unit = {
        scope.launch {
            val result = try {
                controller.providePermission(Permission.CAMERA)
                PermissionState.Granted
            } catch (_: DeniedAlwaysException) {
                PermissionState.DeniedAlways
            } catch (_: DeniedException) {
                PermissionState.Denied
            } catch (_: RequestCanceledException) {
                PermissionState.Denied
            }
            viewModel.onAction(QrScannerAction.OnPermissionResult(result))
        }
    }

    // Ask on first entry; if already granted this returns immediately without a prompt.
    LaunchedEffect(Unit) { requestPermission() }

    QrScannerScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onRequestPermission = requestPermission,
        onOpenSettings = controller::openAppSettings,
    )
}

@Composable
fun QrScannerScreen(
    state: QrScannerState,
    onAction: (QrScannerAction) -> Unit,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AppBaseScreen(
        titleText = "Scan QR",
        onIconClick = onBack,
    ) {
        when (state.permission) {
            PermissionState.Granted -> CameraContent(state = state, onAction = onAction)

            PermissionState.DeniedAlways -> PermissionMessage(
                message = "Camera access is blocked. Enable it in Settings to scan your login QR code.",
                buttonText = "Open settings",
                onButtonClick = onOpenSettings,
            )

            PermissionState.Denied,
            PermissionState.NotGranted,
            -> PermissionMessage(
                message = "Rent+ needs the camera to scan your login QR code.",
                buttonText = "Grant camera access",
                onButtonClick = onRequestPermission,
            )

            PermissionState.NotDetermined -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ColumnScope.CameraContent(
    state: QrScannerState,
    onAction: (QrScannerAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center,
    ) {
        QrScanner(
            modifier = Modifier.fillMaxSize(),
            flashlightOn = false,
            cameraLens = CameraLens.Back,
            openImagePicker = false,
            onCompletion = { payload -> onAction(QrScannerAction.OnQrScanned(payload)) },
            imagePickerHandler = { },
            onFailure = { },
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    state.error?.let { error ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            AppButton(
                text = "Try again",
                onClick = { onAction(QrScannerAction.OnRetry) },
            )
        }
    }
}

@Composable
private fun ColumnScope.PermissionMessage(
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
) {
    val maxWidth: Dp = when (currentDeviceConfiguration()) {
        DeviceConfiguration.MOBILE -> Dp.Unspecified
        else -> AppDimens.formMaxWidth
    }
    Column(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth()
            .widthIn(max = maxWidth)
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
        )
        AppButton(
            text = buttonText,
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun QrScannerDeniedPreview() {
    AppTheme {
        QrScannerScreen(
            state = QrScannerState(permission = PermissionState.Denied),
            onAction = {},
            onBack = {},
            onRequestPermission = {},
            onOpenSettings = {},
        )
    }
}

@Preview
@Composable
private fun QrScannerDeniedAlwaysDarkPreview() {
    AppTheme(darkTheme = true) {
        QrScannerScreen(
            state = QrScannerState(permission = PermissionState.DeniedAlways),
            onAction = {},
            onBack = {},
            onRequestPermission = {},
            onOpenSettings = {},
        )
    }
}
