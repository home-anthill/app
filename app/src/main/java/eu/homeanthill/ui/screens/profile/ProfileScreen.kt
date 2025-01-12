package eu.homeanthill.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import eu.homeanthill.R
import eu.homeanthill.ui.components.TopAppBar

@Composable
fun ProfileScreen(
    profileUiState: ProfileViewModel.ProfileUiState,
    navController: NavController,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                appbarTitle = stringResource(R.string.profile),
                onBackPressed = { navController.popBackStack() }
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
                when (profileUiState) {
                    is ProfileViewModel.ProfileUiState.Error -> {
                        Text(
                            text = profileUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is ProfileViewModel.ProfileUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is ProfileViewModel.ProfileUiState.Idle -> {
                        Text(
                            text = profileUiState.profile.toString(),
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    )
}