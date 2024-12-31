package eu.homeanthill

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.homeanthill.ui.navigation.Graph
import eu.homeanthill.ui.navigation.MainRoute
import eu.homeanthill.ui.screens.home.HomeScreen
import eu.homeanthill.ui.screens.home.HomeViewModel
import eu.homeanthill.ui.screens.login.LoginScreen
import eu.homeanthill.ui.screens.login.LoginViewModel
import eu.homeanthill.ui.theme.HomeAnthillTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "secret env - API_BASE_URL = ${BuildConfig.API_BASE_URL}")
        enableEdgeToEdge()
        setContent {
            HomeAnthillTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        route = Graph.MAIN,
                        startDestination = MainRoute.Login.name
                    ) {
                        composable(
                            route = MainRoute.Login.name
                        ) {
                            val loginViewModel = koinViewModel<LoginViewModel>()
                            val loginUiState by loginViewModel.loginUiState.collectAsStateWithLifecycle()

                            LoginScreen(
                                loginUiState = loginUiState,
                                loginViewModel = loginViewModel,
                                navController = navController
                            )
                        }
                        composable(
                            route = MainRoute.Home.name
                        ) {
                            val homeViewModel = koinViewModel<HomeViewModel>()
                            val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()

                            HomeScreen(
                                homeUiState = homeUiState,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}