package eu.homeanthill.ui.screens.devices.onlineValues

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.homeanthill.R

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.OnlineValue
import eu.homeanthill.api.model.Room

@Composable
fun OnlineValuesScreen(
    onlineValuesUiState: OnlineValuesViewModel.OnlineValuesUiState,
    onlineValuesViewModel: OnlineValuesViewModel,
    navController: NavController,
) {
    // inputs
    val device: Device? =
        navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
    val home: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
    val room: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

    LaunchedEffect(Unit) {
        if (device != null) {
            onlineValuesViewModel.initDeviceValues(device)
        }
    }

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "PowerOutage",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (home != null && room != null) {
                    Text(
                        text = "${home.name} ${home.location} - ${room.name} ${room.floor}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (device != null) {
                    Text(
                        text = device.mac,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${device.manufacturer} - ${device.model}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                when (onlineValuesUiState) {
                    is OnlineValuesViewModel.OnlineValuesUiState.Error -> {
                        Text(
                            text = onlineValuesUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is OnlineValuesViewModel.OnlineValuesUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is OnlineValuesViewModel.OnlineValuesUiState.Idle -> {
                        if (onlineValuesUiState.onlineValue != null) {
                            OnlineValueCard(
                                onlineValuesViewModel = onlineValuesViewModel,
                                onlineValue = onlineValuesUiState.onlineValue,
                            )
                        }
                    }
                }
            }
        },
    )
}


@Composable
fun OnlineValueCard(
    onlineValuesViewModel: OnlineValuesViewModel,
    onlineValue: OnlineValue
) {
    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.bolt_24px),
                    contentDescription = "PowerOutage",
                    modifier = Modifier.size(45.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "POWEROUTAGE",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onlineValuesViewModel.isOffline(
                        onlineValue.modifiedAt,
                        onlineValue.currentTime
                    )
                ) {
                    Canvas(modifier = Modifier.size(30.dp), onDraw = {
                        drawCircle(color = Color.Red)
                    })
                    Text(
                        text = "Offline",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Canvas(modifier = Modifier.size(30.dp), onDraw = {
                        drawCircle(color = Color.Green)
                    })
                    Text(
                        text = "Online",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = onlineValuesViewModel.getPrettyDateFromUnixEpoch(onlineValue.modifiedAt),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}