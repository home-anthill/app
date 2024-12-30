package eu.homeanthill.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.homeanthill.R
import eu.homeanthill.ui.components.TopAppBar
import eu.homeanthill.ui.navigation.MainRoute

@Composable
fun LoginScreen(
    loginUiState: LoginViewModel.LoginUiState,
    loginViewModel: LoginViewModel,
    navController: NavController,
) {
    var apiToken by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                appbarTitle = stringResource(id = R.string.login)
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (loginUiState) {
                    is LoginViewModel.LoginUiState.Error -> {
                        Text(
                            text = loginUiState.errorMessage,
                            color = Color.Red
                        )
                    }

                    is LoginViewModel.LoginUiState.IsLogged -> {
                        val isLoggedIn = loginUiState.loggedIn
                        if (isLoggedIn) {
                            navController.navigate(route = MainRoute.Home.name)
                        } else {
                            TextField(
                                value = apiToken,
                                onValueChange = { apiToken = it },
                            )
                            Button(
                                onClick = {
                                    loginViewModel.login(apiToken)
                                    navController.navigate(route = MainRoute.Home.name)
                                },
                                enabled = true,
                            ) {
                                Text(text = "Initialize")
                            }
                        }
                    }
                }
            }
        }
    )
}