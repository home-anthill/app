package eu.homeanthill.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.homeanthill.R
import eu.homeanthill.ui.components.TopAppBar

@Composable
fun HomeScreen(
    homeUiState: HomeViewModel.HomeUiState,
    navController: NavController,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                appbarTitle = stringResource(id = R.string.home)
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
                when (homeUiState) {
                    is HomeViewModel.HomeUiState.Error -> {
                        Text(
                            text = homeUiState.errorMessage,
                            color = Color.Red
                        )
                    }

                    is HomeViewModel.HomeUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is HomeViewModel.HomeUiState.Idle -> {
                        Text(
                            text = homeUiState.apiToken,
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                        Text(
                            text = homeUiState.fcmToken,
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    )
}