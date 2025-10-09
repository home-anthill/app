package eu.homeanthill.ui.screens.profile

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import eu.homeanthill.LoginActivity
import eu.homeanthill.R
import eu.homeanthill.ui.components.CircleAsyncImage

@Composable
fun ProfileScreen(
  profileUiState: ProfileViewModel.ProfileUiState,
  apiTokenUiState: ProfileViewModel.ApiTokenUiState,
  profileViewModel: ProfileViewModel,
  navController: NavController,
) {
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  Scaffold(
    content = { padding ->
      Column(
        modifier = Modifier
          .padding(padding)
          .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
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
            profileUiState.profile?.github?.avatarURL?.let { CircleAsyncImage(it, 150.dp) }
            Spacer(modifier = Modifier.height(30.dp))
            profileUiState.profile?.github?.login?.let {
              Text(
                text = it,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
            }
            profileUiState.profile?.github?.name?.let {
              Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
            profileUiState.profile?.github?.email?.let {
              Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
              )
            }
            HorizontalDivider(
              thickness = 2.dp, modifier = Modifier.padding(vertical = 20.dp)
            )
            when (apiTokenUiState) {
              is ProfileViewModel.ApiTokenUiState.Error -> {
                Text(
                  text = "********-****-****-****-************",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
              }

              is ProfileViewModel.ApiTokenUiState.Loading -> {
                CircularProgressIndicator()
              }

              is ProfileViewModel.ApiTokenUiState.Idle -> {
                Text(
                  text = apiTokenUiState.apiToken,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
            Button(
              onClick = {
                coroutineScope.launch {
                  Log.d("ProfileScreen", "id = $profileUiState.profile?.id")
                  profileViewModel.regenApiToken(profileUiState.profile?.id)
                }
              }, enabled = true, modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            ) {
              Text(text = stringResource(R.string.profile_regen_apitoken))
            }
            HorizontalDivider(
              thickness = 2.dp, modifier = Modifier.padding(vertical = 20.dp)
            )
            Button(
              onClick = {
                profileViewModel.logout()
              },
              enabled = true,
            ) {
              Text(text = stringResource(R.string.profile_logout))
            }
          }
        }
      }
    })
}