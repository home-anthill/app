package eu.homeanthill.ui.screens.devices.editdevice

import android.util.Log
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
import com.google.gson.annotations.SerializedName
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.api.model.RoomRequest
import eu.homeanthill.ui.components.MaterialSpinner
import eu.homeanthill.ui.components.SpinnerItemObj
import eu.homeanthill.ui.screens.devices.DevicesRoute
import kotlinx.coroutines.launch

@Composable
fun EditDeviceScreen(
    devicesUiState: EditDeviceViewModel.EditDeviceUiState,
    devicesViewModel: EditDeviceViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val device = navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")

    var selectedHome: Home? by remember { mutableStateOf(null) }
    var selectedRoom: Room? by remember { mutableStateOf(null) }

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
                        MaterialSpinner(
                            title = "home",
                            options = devicesUiState.homes.map {
                                SpinnerItemObj(it.id, it.name)
                            },
                            onSelect = { option ->
                                selectedHome = devicesUiState.homes.find { it.id === option.key }
                                Log.d("*********", "$selectedHome")
                            },
                            modifier = Modifier.padding(10.dp)
                        )
                        if (selectedHome !== null && selectedHome!!.rooms !== null && selectedHome!!.rooms!!.isNotEmpty()) {
                            MaterialSpinner(
                                title = "room",
                                options = selectedHome!!.rooms!!.map {
                                    SpinnerItemObj(it.id, it.name)
                                },
                                onSelect = { option ->
                                    selectedRoom =
                                        selectedHome!!.rooms!!.find { it.id === option.key }
                                    Log.d("*********", "$selectedRoom")
                                },
                                modifier = Modifier.padding(10.dp)
                            )
                        } else {
                            selectedRoom = null
                            Log.d("*********", "selectedRoom reset to null")
                        }

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