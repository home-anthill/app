package eu.homeanthill

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import org.koin.androidx.compose.koinViewModel
import eu.homeanthill.ui.theme.AppTheme
import eu.homeanthill.ui.navigation.Graph
import eu.homeanthill.ui.navigation.MainRoute
import eu.homeanthill.ui.screens.home.HomeScreen
import eu.homeanthill.ui.screens.home.HomeViewModel
import eu.homeanthill.ui.screens.login.LoginScreen
import eu.homeanthill.ui.screens.login.LoginViewModel
import eu.homeanthill.ui.screens.profile.ProfileScreen
import eu.homeanthill.ui.screens.profile.ProfileViewModel

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data: Uri? = intent.data
        Log.d(TAG, "onNewIntent query = ${data?.query}")

        // these 2 query params must match those on server-side
        val jwt = data?.getQueryParameter("token")
        val cookie = data?.getQueryParameter("session_cookie")

        this.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
            .edit()
            .putString("sessionCookie", cookie)
            .putString("jwt", jwt)
            .apply()

        // restart the activity
        val i = Intent(this@MainActivity, MainActivity::class.java)
        finish()
        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "secret env - API_BASE_URL = ${BuildConfig.API_BASE_URL}")
        enableEdgeToEdge()
        setContent {
            AppTheme(dynamicColor = false) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
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
                                navController = navController,

                            )
                        }
                        composable(
                            route = MainRoute.Home.name
                        ) {
                            val homeViewModel = koinViewModel<HomeViewModel>()
                            val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
                            HomeScreen(
                                homeUiState = homeUiState,
                                navController = navController,
                            )
                        }
                        composable(
                            route = MainRoute.Profile.name
                        ) {
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
                    }
                }
            }
        }
    }
}