package eu.homeanthill.ui.screens.devices

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel

import eu.homeanthill.ui.screens.devices.deviceslist.DevicesListScreen
import eu.homeanthill.ui.screens.devices.deviceslist.DevicesListViewModel
import eu.homeanthill.ui.screens.devices.editdevice.EditDeviceScreen
import eu.homeanthill.ui.screens.devices.editdevice.EditDeviceViewModel
import eu.homeanthill.ui.screens.devices.sensorValues.SensorValuesScreen
import eu.homeanthill.ui.screens.devices.sensorValues.SensorValuesViewModel
import eu.homeanthill.ui.screens.devices.onlineValues.OnlineValuesScreen
import eu.homeanthill.ui.screens.devices.onlineValues.OnlineValuesViewModel
import eu.homeanthill.ui.screens.devices.deviceValues.DeviceValuesScreen
import eu.homeanthill.ui.screens.devices.deviceValues.DeviceValuesViewModel

@Composable
fun DevicesScreen(
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            route = Graph.DEVICES_GRAPH,
            startDestination = DevicesRoute.Devices.name
        ) {
            composable(
                route = DevicesRoute.Devices.name
            ) {
                val devicesListViewModel = koinViewModel<DevicesListViewModel>()
                val devicesListUiState by devicesListViewModel.devicesUiState.collectAsStateWithLifecycle()
                DevicesListScreen(
                    devicesUiState = devicesListUiState,
                    devicesViewModel = devicesListViewModel,
                    navController = navController,
                )
            }
            composable(
                route = DevicesRoute.EditDevice.name
            ) {
                val editDeviceViewModel = koinViewModel<EditDeviceViewModel>()
                val editDeviceUiState by editDeviceViewModel.editDeviceUiState.collectAsStateWithLifecycle()
                EditDeviceScreen(
                    devicesUiState = editDeviceUiState,
                    devicesViewModel = editDeviceViewModel,
                    navController = navController,
                )
            }
            composable(
                route = DevicesRoute.SensorValues.name
            ) {
                val sensorValuesViewModel = koinViewModel<SensorValuesViewModel>()
                val sensorValuesUiState by sensorValuesViewModel.sensorValuesUiState.collectAsStateWithLifecycle()
                SensorValuesScreen(
                    sensorValuesUiState = sensorValuesUiState,
                    sensorValuesViewModel = sensorValuesViewModel,
                    navController = navController,
                )
            }
            composable(
                route = DevicesRoute.OnlineValues.name
            ) {
                val onlineValuesViewModel = koinViewModel<OnlineValuesViewModel>()
                val onlineValuesUiState by onlineValuesViewModel.onlineValuesUiState.collectAsStateWithLifecycle()
                OnlineValuesScreen(
                    onlineValuesUiState = onlineValuesUiState,
                    onlineValuesViewModel = onlineValuesViewModel,
                    navController = navController,
                )
            }
            composable(
                route = DevicesRoute.DeviceValues.name
            ) {
                val deviceValuesViewModel = koinViewModel<DeviceValuesViewModel>()
                val deviceValuesUiState by deviceValuesViewModel.deviceValuesUiState.collectAsStateWithLifecycle()
                DeviceValuesScreen(
                    deviceValuesUiState = deviceValuesUiState,
                    deviceValuesViewModel = deviceValuesViewModel,
                    navController = navController,
                )
            }
        }
    }
}