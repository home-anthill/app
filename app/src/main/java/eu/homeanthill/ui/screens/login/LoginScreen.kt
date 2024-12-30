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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import eu.homeanthill.R
import eu.homeanthill.ui.components.TopAppBar
import eu.homeanthill.ui.navigation.MainRoute

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LoginScreen(
    loginUiState: LoginViewModel.LoginUiState,
    loginViewModel: LoginViewModel,
    navController: NavController,
) {
    var apiToken by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Permission request
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
                    text = "Permission",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    "The notification is important for this app. Please grant the permission.",
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
                    Text("OK", style = TextStyle(color = Color.Black))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationalDialog.value = false
                    }) {
                    Text("Cancel", style = TextStyle(color = Color.Black))
                }
            },
        )
    }

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
                if (notificationPermission.status.isGranted) {
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
                } else {
                    Button(onClick = {
                        if (notificationPermission.status.shouldShowRationale) {
                            // Show a rationale if needed (optional)
                            showRationalDialog.value = true
                        } else {
                            // Request the permission
                            notificationPermission.launchPermissionRequest()

                        }
                    }) {
                        Text(text = "Ask for permission")
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
                        text = if (notificationPermission.status.isGranted) {
                            "Permission Granted"
                        } else if (notificationPermission.status.shouldShowRationale) {
                            // If the user has denied the permission but the rationale can be shown,
                            // then gently explain why the app requires this permission
                            "The notification is important for this app. Please grant the permission."
                        } else {
                            // If it's the first time the user lands on this feature, or the user
                            // doesn't want to be asked again for this permission, explain that the
                            // permission is required
                            "The notification permission is required for some functionality."
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    )
}