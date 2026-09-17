package org.example.rent

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.example.rent.core.designsystem.theme.AppTheme
import org.example.rent.auth.presentation.navigation.AuthGraphRoutes
import org.example.rent.auth.presentation.navigation.authGraph
import org.example.rent.home.presentation.navigation.HomeGraphRoutes
import org.example.rent.home.presentation.navigation.homeGraph
import androidx.compose.ui.tooling.preview.Preview

/**
 * Root UI. Koin is started once by the platform entry point (`initKoin`); with Koin 4.x the Compose
 * integration reads that global container directly, so `koinViewModel()` in screens just works.
 */
@Composable
@Preview
fun App() {
    AppTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = AuthGraphRoutes.Graph,
        ) {
            authGraph(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(HomeGraphRoutes.Graph) {
                        popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                    }
                },
            )
            homeGraph()
        }
    }
}
