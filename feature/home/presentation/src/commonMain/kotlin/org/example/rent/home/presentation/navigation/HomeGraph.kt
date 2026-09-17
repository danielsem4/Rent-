package org.example.rent.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import org.example.rent.home.presentation.home.HomeRoot

/** Registers the home feature's navigation subgraph (the post-login landing). */
fun NavGraphBuilder.homeGraph() {
    navigation<HomeGraphRoutes.Graph>(startDestination = HomeGraphRoutes.Home) {
        composable<HomeGraphRoutes.Home> {
            HomeRoot()
        }
    }
}
