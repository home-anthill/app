package eu.homeanthill.ui.screens.homes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel

import eu.homeanthill.ui.screens.homes.rooms.RoomsViewModel
import eu.homeanthill.ui.screens.homes.homeslist.HomesListScreen
import eu.homeanthill.ui.screens.homes.homeslist.HomesListViewModel
import eu.homeanthill.ui.screens.homes.rooms.RoomsScreen

@Composable
fun HomesScreen(
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            route = Graph.HOMES_GRAPH,
            startDestination = HomesRoute.Homes.name
        ) {
            composable(
                route = HomesRoute.Homes.name
            ) {
                val homesListViewModel = koinViewModel<HomesListViewModel>()
                val homesListUiState by homesListViewModel.homesUiState.collectAsStateWithLifecycle()
                HomesListScreen(
                    homesUiState = homesListUiState,
                    homesViewModel = homesListViewModel,
                    navController = navController,
                )
            }
            composable(
                route = HomesRoute.EditHome.name
            ) {
                val roomsViewModel = koinViewModel<RoomsViewModel>()
                val roomsUiState by roomsViewModel.roomsUiState.collectAsStateWithLifecycle()
                RoomsScreen(
                    roomsUiState = roomsUiState,
                    roomsViewModel = roomsViewModel,
                    navController = navController,
                )
            }
        }
    }
}