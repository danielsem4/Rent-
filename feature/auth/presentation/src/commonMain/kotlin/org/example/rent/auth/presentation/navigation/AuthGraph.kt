package org.example.rent.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import org.example.rent.auth.presentation.qrscanner.QrScannerRoot
import org.example.rent.auth.presentation.welcome.WelcomeRoot

/**
 * Registers the auth feature's navigation subgraph. [onLoginSuccess] is a cross-feature callback the
 * host (`:shared`) wires up — the auth module never imports another feature's routes directly.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(startDestination = AuthGraphRoutes.Welcome) {
        composable<AuthGraphRoutes.Welcome> {
            WelcomeRoot(
                onScanQrClick = { navController.navigate(AuthGraphRoutes.QrScanner) },
                onLoginSuccess = onLoginSuccess,
            )
        }

        composable<AuthGraphRoutes.QrScanner> {
            QrScannerRoot(
                onScanSuccess = onLoginSuccess,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
