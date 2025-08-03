package eu.homeanthill.ui.screens.devices.deviceValues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.PostSetDeviceValue
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.components.MaterialSpinner
import eu.homeanthill.ui.components.SwitchWithLabel

@Composable
fun DeviceValuesScreen(
    sendUiState: DeviceValuesViewModel.SendUiState,
    getValueUiState: DeviceValuesViewModel.GetValueUiState,
    deviceValuesViewModel: DeviceValuesViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // inputs
    val device: Device? =
        navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
    val home: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
    val room: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

    var on: Boolean by remember { mutableStateOf(false) }
    var temperature: Int by remember { mutableIntStateOf(17) }
    var mode: Int by remember { mutableIntStateOf(1) }
    var fanSpeed: Int by remember { mutableIntStateOf(1) }
    var modifiedAt: String by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (device != null) {
            val value = deviceValuesViewModel.getValue(device.id)
            if (value != null) {
                on = value.on
                temperature = value.temperature
                mode = value.mode
                fanSpeed = value.fanSpeed
                modifiedAt = value.modifiedAt
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
                    text = "Controller",
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
                when (getValueUiState) {
                    is DeviceValuesViewModel.GetValueUiState.Error -> {
                        Spacer(modifier = Modifier.height(100.dp))
                        Text(
                            text = "Can't load current values",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    is DeviceValuesViewModel.GetValueUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is DeviceValuesViewModel.GetValueUiState.Idle -> {
                        if (device != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(6.dp))
                                SwitchWithLabel(
                                    label = "On/Off",
                                    state = on,
                                    onStateChange = {
                                        on = it
                                    }
                                )
                                MaterialSpinner(
                                    title = "Temperature",
                                    options = deviceValuesViewModel.getTemperatures(),
                                    onSelect = { option ->
                                        temperature =
                                            deviceValuesViewModel.getTemperatureValue(option.value)
                                    },
                                    modifier = Modifier.padding(10.dp),
                                    selectedOption = deviceValuesViewModel.getTemperatures()[temperature - 17],
                                )
                                MaterialSpinner(
                                    title = "Mode",
                                    options = deviceValuesViewModel.getModes(),
                                    onSelect = { option ->
                                        mode = deviceValuesViewModel.getModeValue(option.value)
                                    },
                                    modifier = Modifier.padding(10.dp),
                                    selectedOption = deviceValuesViewModel.getModes()[mode - 1]
                                )
                                MaterialSpinner(
                                    title = "Fan speed",
                                    options = deviceValuesViewModel.getFanSpeeds(),
                                    onSelect = { option ->
                                        fanSpeed =
                                            deviceValuesViewModel.getFanSpeedValue(option.value)
                                    },
                                    modifier = Modifier.padding(10.dp),
                                    selectedOption = deviceValuesViewModel.getFanSpeeds()[fanSpeed - 1]
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = deviceValuesViewModel.getPrettyDateFromUnixEpoch(
                                        modifiedAt
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            deviceValuesViewModel.send(
                                                id = device.id,
                                                PostSetDeviceValue(
                                                    on = on,
                                                    temperature = temperature,
                                                    mode = mode,
                                                    fanSpeed = fanSpeed
                                                )
                                            )
                                        }
                                    },
                                ) {
                                    Text(text = "Send")
                                }
                            }
                        }
                    }
                }
            }
            when (sendUiState) {
                is DeviceValuesViewModel.SendUiState.Error -> {
                    LaunchedEffect(snackbarHostState) {
                        snackbarHostState
                            .showSnackbar(
                                message = "Cannot update device state!",
                                duration = SnackbarDuration.Long
                            )
                    }
                }

                is DeviceValuesViewModel.SendUiState.Idle -> {
                    if (sendUiState.result != null) {
                        LaunchedEffect(snackbarHostState) {
                            snackbarHostState
                                .showSnackbar(
                                    message = "Device state update successfully!",
                                    duration = SnackbarDuration.Short
                                )
                        }
                    }
                }
            }
        },
    )
}