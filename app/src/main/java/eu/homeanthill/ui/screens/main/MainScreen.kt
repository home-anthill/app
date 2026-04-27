package eu.homeanthill.ui.screens.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

import eu.homeanthill.R
import eu.homeanthill.ui.navigation.Destinations

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
  mainUiState: MainViewModel.MainUiState,
  navController: NavController,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val notificationPermission = rememberPermissionState(
    permission = Manifest.permission.POST_NOTIFICATIONS
  )
  val showRationalDialog = remember { mutableStateOf(false) }
  if (showRationalDialog.value) {
    AlertDialog(
      onDismissRequest = {
        showRationalDialog.value = false
      },
      title = {
        Text(
          text = stringResource(R.string.home_permission),
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      },
      text = {
        Text(
          text = stringResource(R.string.home_permission_test),
          fontSize = 16.sp
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            showRationalDialog.value = false
            val intent = Intent(
              Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent, null)

          }) {
          Text(
            text = stringResource(R.string.ok),
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showRationalDialog.value = false
          }) {
          Text(
            text = stringResource(R.string.cancel),
          )
        }
      },
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF121212),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
    ) {
      Column(
        modifier = Modifier
          .padding(vertical = 64.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        if (notificationPermission.status.isGranted) {
          when (mainUiState) {
            is MainViewModel.MainUiState.Error -> {
              Icon(
                imageVector = ImageVector.vectorResource(R.drawable.cloud_off_24dp),
                contentDescription = "Connection error",
                modifier = Modifier.size(150.dp),
                tint = MaterialTheme.colorScheme.error,
              )
              Text(
                text = mainUiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
              )
            }

            is MainViewModel.MainUiState.Loading -> {
              CircularProgressIndicator()
            }

            is MainViewModel.MainUiState.Idle -> {
              navController.navigate(Destinations.DEVICES) {
                launchSingleTop = true
                restoreState = true
              }
            }
          }
        } else {
          Button(
            onClick = {
              if (notificationPermission.status.shouldShowRationale) {
                // Show a rationale if needed (optional)
                showRationalDialog.value = true
              } else {
                // Request the permission
                notificationPermission.launchPermissionRequest()

              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFBD5700),
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = stringResource(id = R.string.home_ask_permission),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
            text = if (notificationPermission.status.isGranted) {
              stringResource(R.string.home_permission_granted)
            } else if (notificationPermission.status.shouldShowRationale) {
              // If the user has denied the permission but the rationale can be shown,
              // then gently explain why the app requires this permission
              stringResource(R.string.home_permission_rationale)
            } else {
              // If it's the first time the user lands on this feature, or the user
              // doesn't want to be asked again for this permission, explain that the
              // permission is required
              stringResource(R.string.home_permission_required)
            },
            color = Color(0xFF9E9E9E),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      }
    }
  }
}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//  AppTheme(darkTheme = true) {
//    MainScreen(
//      mainUiState = MainViewModel.MainUiState.Idle,
//      navController = NavController(LocalContext.current)
//    )
//  }
//}
