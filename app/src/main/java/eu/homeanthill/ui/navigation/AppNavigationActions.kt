package eu.homeanthill.ui.navigation

import androidx.navigation.NavHostController

class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToHome() {
        navController.navigate(Destinations.HOME) {
            popUpTo(Destinations.HOME)
        }
    }

    fun navigateToProfile() {
        navController.navigate(Destinations.PROFILE) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToHomes() {
        navController.navigate(Destinations.HOMES) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToDevices() {
        navController.navigate(Destinations.DEVICES) {
            launchSingleTop = true
            restoreState = true
        }
    }
}