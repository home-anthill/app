package eu.homeanthill.ui.screens.devices.featurevalues

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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.screens.devices.featurevalues.controllerValues.ControllerValuesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.controllerValues.ControllerValuesScreen
import eu.homeanthill.ui.screens.devices.featurevalues.sensorValues.SensorValuesScreen
import eu.homeanthill.ui.screens.devices.featurevalues.sensorValues.SensorValuesViewModel

@Composable
fun FeaturesScreen(
  featureValuesUiState: FeaturesViewModel.FeatureValuesUiState,
  featureValuesViewModel: FeaturesViewModel,
  navController: NavController,
) {
  // inputs
  val device: Device? =
    navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
  val home: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
  val room: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    if (device != null) {
      featureValuesViewModel.initDeviceValues(device)
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
          text = "Device",
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
        when (featureValuesUiState) {
          is FeaturesViewModel.FeatureValuesUiState.Error -> {
            Text(
              text = featureValuesUiState.errorMessage,
              color = MaterialTheme.colorScheme.error,
            )
          }

          is FeaturesViewModel.FeatureValuesUiState.Loading -> {
            CircularProgressIndicator()
          }

          is FeaturesViewModel.FeatureValuesUiState.Idle -> {
            if (featureValuesUiState.deviceValue?.sensorFeatureValues?.isEmpty() == false) {
              val sensorValuesViewModel = koinViewModel<SensorValuesViewModel>()
              SensorValuesScreen(
                featureValues = featureValuesUiState.deviceValue.sensorFeatureValues,
                sensorValuesViewModel = sensorValuesViewModel,
              )
            }

            val controllerValuesViewModel = koinViewModel<ControllerValuesViewModel>()
            val getValueUiState by controllerValuesViewModel.getValueUiState.collectAsStateWithLifecycle()

            if (featureValuesUiState.deviceValue?.controllerFeatureValues?.isEmpty() == false) {
              ControllerValuesScreen(
                device = device,
                getValueUiState = getValueUiState,
                controllerValuesViewModel = controllerValuesViewModel,
                onSendResult = { result ->
                  coroutineScope.launch {
                    if (result.isError) {
                      snackbarHostState
                        .showSnackbar(
                          message = "Cannot update device state!",
                          duration = SnackbarDuration.Long
                        )
                    } else {
                      snackbarHostState
                        .showSnackbar(
                          message = "Device state update successfully!",
                          duration = SnackbarDuration.Short
                        )
                    }
                  }
                }
              )
            }
          }
        }
      }
    },
  )
}