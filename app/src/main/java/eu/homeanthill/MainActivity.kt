package eu.homeanthill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.TopAppBarDefaults
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import eu.homeanthill.ui.theme.AppTheme
import eu.homeanthill.ui.navigation.AppDrawer
import eu.homeanthill.ui.navigation.AppNavigationActions
import eu.homeanthill.ui.navigation.Destinations.DEVICES
import eu.homeanthill.ui.navigation.Destinations.HOME
import eu.homeanthill.ui.navigation.Destinations.HOMES
import eu.homeanthill.ui.navigation.Destinations.PROFILE
import eu.homeanthill.ui.screens.devices.DevicesScreen
import eu.homeanthill.ui.screens.main.MainScreen
import eu.homeanthill.ui.screens.main.MainViewModel
import eu.homeanthill.ui.screens.homes.HomesScreen
import eu.homeanthill.ui.screens.profile.ProfileScreen
import eu.homeanthill.ui.screens.profile.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme(dynamicColor = false) {
                AppNavGraph()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: HOME
    val navigationActions = remember(navController) {
        AppNavigationActions(navController)
    }
    ModalNavigationDrawer(
        drawerContent = {
            AppDrawer(
                route = currentRoute,
                navigateToHome = { navigationActions.navigateToHome() },
                navigateToProfile = { navigationActions.navigateToProfile() },
                navigateToHomes = { navigationActions.navigateToHomes() },
                navigateToDevices = { navigationActions.navigateToDevices() },
                closeDrawer = { coroutineScope.launch { drawerState.close() } },
                modifier = Modifier
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = currentRoute) },
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            content = {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            modifier = Modifier
        ) {
            NavHost(
                navController = navController,
                startDestination = HOME,
                modifier = modifier.padding(it)
            ) {
                composable(HOME) {
                    val homeViewModel = koinViewModel<MainViewModel>()
                    val homeUiState by homeViewModel.mainUiState.collectAsStateWithLifecycle()
                    MainScreen(
                        mainUiState = homeUiState,
                        navController = navController,
                    )
                }
                composable(PROFILE) {
                    val profileViewModel = koinViewModel<ProfileViewModel>()
                    val profileUiState by profileViewModel.profileUiState.collectAsStateWithLifecycle()
                    val apiTokenUiState by profileViewModel.apiTokenUiState.collectAsStateWithLifecycle()
                    ProfileScreen(
                        profileUiState = profileUiState,
                        apiTokenUiState = apiTokenUiState,
                        profileViewModel = profileViewModel,
                        navController = navController
                    )
                }
                composable(HOMES) {
                    HomesScreen()
                }
                composable(DEVICES) {
                    DevicesScreen()
                }
            }
        }
    }
}