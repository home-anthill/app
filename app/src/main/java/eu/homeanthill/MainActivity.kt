package eu.homeanthill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.TopAppBarDefaults
import org.koin.androidx.compose.koinViewModel

import eu.homeanthill.api.model.Profile
import eu.homeanthill.ui.theme.AppTheme
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
) {
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    FCMNotificationBus.messages.collect { message ->
      val title = message.notification?.title
      val body = message.notification?.body
      val text = when {
        title != null && body != null -> "$title: $body"
        title != null -> title
        body != null -> body
        message.data.isNotEmpty() -> message.data.values.first()
        else -> return@collect
      }
      snackbarHostState.showSnackbar(message = text, duration = SnackbarDuration.Long)
    }
  }

  val mainViewModel = koinViewModel<MainViewModel>()
  val mainUiState by mainViewModel.mainUiState.collectAsStateWithLifecycle()

  val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = currentNavBackStackEntry?.destination
  val currentRoute = currentDestination?.route ?: HOME

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    bottomBar = {
      NavigationBar {
        val items = listOf(
          Triple(DEVICES, R.string.devices, Icons.Default.List),
          Triple(HOMES, R.string.homes, Icons.Default.Settings),
          Triple(PROFILE, R.string.profile, Icons.Default.Person)
        )
        items.forEach { (route, labelRes, icon) ->
          NavigationBarItem(
            icon = { Icon(icon, contentDescription = null) },
            label = { Text(stringResource(labelRes)) },
            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
            onClick = {
              navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
              }
            }
          )
        }
      }
    },
    modifier = Modifier
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = DEVICES,
      modifier = modifier.padding(innerPadding)
    ) {
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
