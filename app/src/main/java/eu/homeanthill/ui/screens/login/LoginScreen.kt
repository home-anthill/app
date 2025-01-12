package eu.homeanthill.ui.screens.login

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import eu.homeanthill.BuildConfig
import eu.homeanthill.R
import eu.homeanthill.ui.components.TopAppBar
import eu.homeanthill.ui.navigation.MainRoute

@Composable
fun LoginScreen(
    loginUiState: LoginViewModel.LoginUiState,
    navController: NavController,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                appbarTitle = stringResource(R.string.login),
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (loginUiState) {
                    is LoginViewModel.LoginUiState.HasJWT -> {
                        val hasJWT = loginUiState.hasJWT
                        if (hasJWT) {
                            navController.navigate(route = MainRoute.Home.name)
                        } else {
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(BuildConfig.API_BASE_URL + "login_app")
                                    )
                                    context.startActivity(intent)
                                },
                                enabled = true,
                            ) {
                                Text(text = stringResource(R.string.login_button))
                            }

                        }
                    }
                }
            }
        }
    )
}