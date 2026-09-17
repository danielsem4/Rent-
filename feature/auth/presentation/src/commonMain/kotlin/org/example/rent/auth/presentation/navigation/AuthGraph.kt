package org.example.rent.auth.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import org.example.rent.auth.presentation.qrscanner.QrScannerRoot
import org.example.rent.auth.presentation.welcome.WelcomeRoot

/** Back-stack key used to hand the scanned QR token from the scanner back to the Welcome screen. */
private const val QR_TOKEN_KEY = "qrToken"

/**
 * Registers the auth feature's navigation subgraph. [onLoginSuccess] is a cross-feature callback the
 * host (`:shared`) wires up — the auth module never imports another feature's routes directly.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(startDestination = AuthGraphRoutes.Welcome) {
        composable<AuthGraphRoutes.Welcome> { entry ->
            val pendingQrToken by entry.savedStateHandle
                .getStateFlow<String?>(QR_TOKEN_KEY, null)
                .collectAsStateWithLifecycle()

            WelcomeRoot(
                onScanQrClick = { navController.navigate(AuthGraphRoutes.QrScanner) },
                onLoginSuccess = onLoginSuccess,
                pendingQrToken = pendingQrToken,
                onQrTokenConsumed = { entry.savedStateHandle[QR_TOKEN_KEY] = null },
            )
        }

        composable<AuthGraphRoutes.QrScanner> {
            QrScannerRoot(
                onOtpSent = { qrToken ->
                    // Hand the token back to Welcome's CODE step, then return to it.
                    navController.previousBackStackEntry?.savedStateHandle?.set(QR_TOKEN_KEY, qrToken)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
