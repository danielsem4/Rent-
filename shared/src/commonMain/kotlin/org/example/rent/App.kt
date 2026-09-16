package org.example.rent

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.example.rent.core.designsystem.theme.AppTheme
import org.example.rent.auth.data.di.authDataModule
import org.example.rent.auth.presentation.di.authPresentationModule
import org.example.rent.auth.presentation.navigation.AuthGraphRoutes
import org.example.rent.auth.presentation.navigation.authGraph
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(authPresentationModule, authDataModule)
    }) {
        AppTheme {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = AuthGraphRoutes.Graph,
            ) {
                authGraph(
                    navController = navController,
                    onLoginSuccess = {
                        // TODO: navigate to the home graph once it exists.
                    },
                )
            }
        }
    }
}
