package eu.homeanthill.ui.screens.devices.deviceslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import eu.homeanthill.api.model.Device
import eu.homeanthill.ui.screens.devices.DevicesRoute

@Composable
fun DevicesListScreen(
    devicesUiState: DevicesListViewModel.DevicesUiState,
    devicesViewModel: DevicesListViewModel,
    navController: NavController,
) {
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
                when (devicesUiState) {
                    is DevicesListViewModel.DevicesUiState.Error -> {
                        Text(
                            text = devicesUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is DevicesListViewModel.DevicesUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is DevicesListViewModel.DevicesUiState.Idle -> {
                        if (devicesUiState.deviceList?.unassignedDevices?.isNotEmpty() == true) {
                            Text(
                                text = "Unassigned",
                                style = MaterialTheme.typography.titleLarge
                            )
                            devicesUiState.deviceList.unassignedDevices.forEach { device ->
                                SimpleCard(
                                    device = device,
                                    onEdit = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "device",
                                            device
                                        )
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "home",
                                            null
                                        )
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "room",
                                            null
                                        )
                                        navController.navigate(route = DevicesRoute.EditDevice.name)
                                    },
                                    onDetails = {
//                                    navController.currentBackStackEntry?.savedStateHandle?.set("home", home)
//                                    navController.navigate(route = HomesRoute.EditHome.name)
                                    },
                                )
                            }
                            HorizontalDivider(
                                thickness = 2.dp, modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                        devicesUiState.deviceList?.homeDevices?.forEach { homeWithDevices ->
                            Text(
                                text = homeWithDevices.home.name + " (" + homeWithDevices.home.location + ")",
                                style = MaterialTheme.typography.titleLarge
                            )
                            homeWithDevices.rooms.forEach { roomWithDevices ->
                                Text(
                                    text = roomWithDevices.room.name + " - " + roomWithDevices.room.floor,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                roomWithDevices.controllerDevices.forEach { device ->
                                    SimpleCard(
                                        device = device,
                                        onEdit = {
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "device",
                                                device
                                            )
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "home",
                                                homeWithDevices.home
                                            )
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "room",
                                                roomWithDevices.room
                                            )
                                            navController.navigate(route = DevicesRoute.EditDevice.name)
                                        },
                                        onDetails = {
//                                    navController.currentBackStackEntry?.savedStateHandle?.set("home", home)
//                                    navController.navigate(route = HomesRoute.EditHome.name)
                                        },
                                    )
                                }
                                roomWithDevices.sensorDevices.forEach { sensor ->
                                    SimpleCard(
                                        device = sensor,
                                        onEdit = {
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "device",
                                                sensor
                                            )
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "home",
                                                homeWithDevices.home
                                            )
                                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                                "room",
                                                roomWithDevices.room
                                            )
                                            navController.navigate(route = DevicesRoute.EditDevice.name)
                                        },
                                        onDetails = {
//                                    navController.currentBackStackEntry?.savedStateHandle?.set("home", home)
//                                    navController.navigate(route = HomesRoute.EditHome.name)
                                        },
                                    )
                                }
                            }
                            HorizontalDivider(
                                thickness = 2.dp, modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun SimpleCard(
    device: Device,
    onEdit: () -> Unit,
    onDetails: () -> Unit,
) {
    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 20.dp)
        ) {
            Text(
                text = device.mac,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = device.model,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(5.dp))
            TextButton(
                onClick = { onEdit() },
                modifier = Modifier.padding(8.dp),
            ) {
                Text("Settings")
            }
            TextButton(
                onClick = { onDetails() },
                modifier = Modifier.padding(8.dp),
            ) {
                Text("Play/Values")
            }
        }
    }
}
