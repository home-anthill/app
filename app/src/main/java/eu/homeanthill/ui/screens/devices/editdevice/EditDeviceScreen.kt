package eu.homeanthill.ui.screens.devices.editdevice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.components.MaterialSpinner
import eu.homeanthill.ui.components.SpinnerItemObj
import eu.homeanthill.ui.screens.devices.DevicesRoute

@Composable
fun EditDeviceScreen(
    devicesUiState: EditDeviceViewModel.EditDeviceUiState,
    devicesViewModel: EditDeviceViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val device = navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
    val initialHome: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
    val initialRoom: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

    var homesOption: List<SpinnerItemObj> by remember { mutableStateOf(listOf()) }
    var roomsOption: List<SpinnerItemObj> by remember { mutableStateOf(listOf()) }

    var selectedHome: Home? by remember { mutableStateOf(initialHome) }
    var selectedRoom: Room? by remember { mutableStateOf(initialRoom) }

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
                if (device !== null) {
                    Text(
                        text = device.mac,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = device.model,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                when (devicesUiState) {
                    is EditDeviceViewModel.EditDeviceUiState.Error -> {
                        Text(
                            text = devicesUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is EditDeviceViewModel.EditDeviceUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is EditDeviceViewModel.EditDeviceUiState.Idle -> {
                        homesOption = devicesUiState.homes.map { home ->
                            SpinnerItemObj(home.id, home.name)
                        }
                        MaterialSpinner(
                            title = "home",
                            options = homesOption,
                            onSelect = { option ->
                                selectedHome = devicesUiState.homes.find { home -> home.id == option.key }
                                selectedRoom = null
                            },
                            modifier = Modifier.padding(10.dp),
                            selectedOption = if (selectedHome == null) { null } else { SpinnerItemObj(selectedHome!!.id, selectedHome!!.name) },
                        )
                        if (selectedHome !== null && selectedHome!!.rooms !== null && selectedHome!!.rooms!!.isNotEmpty()) {
                            roomsOption = selectedHome!!.rooms!!.map { room ->
                                SpinnerItemObj(room.id, room.name)
                            }
                            MaterialSpinner(
                                title = "room",
                                options = roomsOption,
                                onSelect = { option ->
                                    selectedRoom =
                                        selectedHome!!.rooms!!.find { room -> room.id == option.key }
                                },
                                modifier = Modifier.padding(10.dp),
                                selectedOption = if (selectedRoom == null) { null } else { SpinnerItemObj(selectedRoom!!.id, selectedRoom!!.name) },
                            )
                        } else {
                            selectedRoom = null
                        }

                        if (selectedHome != null && selectedRoom != null) {
                            TextButton(
                                onClick = {
                                    if (device !== null && selectedHome !== null && selectedRoom !== null) {
                                        coroutineScope.launch {
                                            devicesViewModel.assignDevice(
                                                id = device.id,
                                                homeId = selectedHome!!.id,
                                                roomId = selectedRoom!!.id
                                            )
                                            navController.navigate(route = DevicesRoute.Devices.name)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Assign")
                            }
                        }
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (device !== null) {
                                        devicesViewModel.deleteDevice(id = device.id)
                                        navController.navigate(route = DevicesRoute.Devices.name)
                                    }
                                }
                            },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Remove this device")
                        }
                    }
                }
            }
        },
    )
}