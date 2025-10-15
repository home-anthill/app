package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.PostSetFeatureDeviceValue
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.components.MaterialSpinner
import eu.homeanthill.ui.components.SwitchWithLabel

@Composable
fun DeviceValuesScreen(
  sendUiState: ControllerValuesViewModel.SendUiState,
  getValueUiState: ControllerValuesViewModel.ValuesUiState,
  controllerValuesViewModel: ControllerValuesViewModel,
  navController: NavController,
) {
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // inputs
  val device: Device? =
    navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
  val home: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
  val room: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

  var featureValues: List<DeviceFeatureValueResponse> by remember { mutableStateOf(listOf()) }
  var modifiedAt: String by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    if (device != null) {
      featureValues = controllerValuesViewModel.getValues(device.id)
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
          is ControllerValuesViewModel.ValuesUiState.Error -> {
            Spacer(modifier = Modifier.height(100.dp))
            Text(
              text = "Can't load current values",
              style = MaterialTheme.typography.bodyLarge
            )
          }

          is ControllerValuesViewModel.ValuesUiState.Loading -> {
            CircularProgressIndicator()
          }

          is ControllerValuesViewModel.ValuesUiState.Idle -> {
            if (device != null) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 20.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Spacer(modifier = Modifier.height(6.dp))

                device.features
                  .filter { feature -> feature.type == "controller" }
                  .map { feature ->
                    when (feature.name) {
                      "on" -> {
                        SwitchWithLabel(
                          label = "On/Off",
                          state = featureValues.find { it.featureUuid == feature.uuid }?.value?.toInt() == 1,
                          onStateChange = { value ->
                            featureValues = featureValues.map { featureValue ->
                              val newVal = featureValue.copy()
                              if (newVal.featureUuid == feature.uuid) {
                                newVal.value = if (value) 1 else 0
                              }
                              newVal
                            }
                          }
                        )
                      }

                      "setpoint" -> {
                        MaterialSpinner(
                          title = "Setpoint",
                          options = controllerValuesViewModel.getSetpoints(),
                          onSelect = { option ->
                            featureValues = featureValues.map { featureValue ->
                              val newVal = featureValue.copy()
                              if (newVal.featureUuid == feature.uuid) {
                                newVal.value =
                                  controllerValuesViewModel.getSetpointValue(option.value)
                              }
                              newVal
                            }
                          },
                          modifier = Modifier.padding(10.dp),
                          selectedOption = controllerValuesViewModel.getSetpointByFeatureUuid(
                            featureValues,
                            feature.uuid
                          ),
                        )
                      }

                      "mode" -> {
                        MaterialSpinner(
                          title = "Mode",
                          options = controllerValuesViewModel.getModes(),
                          onSelect = { option ->
                            featureValues = featureValues.map { featureValue ->
                              val newVal = featureValue.copy()
                              if (newVal.featureUuid == feature.uuid) {
                                newVal.value =
                                  controllerValuesViewModel.getModeValue(option.value)
                              }
                              newVal
                            }
                          },
                          modifier = Modifier.padding(10.dp),
                          selectedOption = controllerValuesViewModel.getModeByFeatureUuid(
                            featureValues,
                            feature.uuid
                          )
                        )
                      }

                      "fanSpeed" -> {
                        MaterialSpinner(
                          title = "Fan speed",
                          options = controllerValuesViewModel.getFanSpeeds(),
                          onSelect = { option ->
                            featureValues = featureValues.map { featureValue ->
                              val newVal = featureValue.copy()
                              if (newVal.featureUuid == feature.uuid) {
                                newVal.value =
                                  controllerValuesViewModel.getFanSpeedValue(option.value)
                              }
                              newVal
                            }
                          },
                          modifier = Modifier.padding(10.dp),
                          selectedOption = controllerValuesViewModel.getFanSpeedByFeatureUuid(
                            featureValues,
                            feature.uuid
                          )
                        )
                      }

                      "tolerance" -> {
                        MaterialSpinner(
                          title = "Tolerance",
                          options = controllerValuesViewModel.getTolerances(),
                          onSelect = { option ->
                            featureValues = featureValues.map { featureValue ->
                              val newVal = featureValue.copy()
                              if (newVal.featureUuid == feature.uuid) {
                                newVal.value =
                                  controllerValuesViewModel.getToleranceValue(option.value)
                              }
                              newVal
                            }
                          },
                          modifier = Modifier.padding(10.dp),
                          selectedOption = controllerValuesViewModel.getToleranceByFeatureUuid(
                            featureValues,
                            feature.uuid
                          )
                        )
                      }
                    }
                  }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                  text = controllerValuesViewModel.getPrettyDateFromUnixEpoch(
                    modifiedAt
                  ),
                  style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                  onClick = {
                    coroutineScope.launch {
                      val listToSend: List<PostSetFeatureDeviceValue> =
                        featureValues
                          .filter { it -> it.type == "controller" }
                          .map { it ->
                            PostSetFeatureDeviceValue(
                              featureUuid = it.featureUuid,
                              type = it.type,
                              name = it.name,
                              value = it.value,
                            )
                          }
                      controllerValuesViewModel.send(
                        id = device.id,
                        body = listToSend
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
        is ControllerValuesViewModel.SendUiState.Error -> {
          LaunchedEffect(snackbarHostState) {
            snackbarHostState
              .showSnackbar(
                message = "Cannot update device state!",
                duration = SnackbarDuration.Long
              )
          }
        }

        is ControllerValuesViewModel.SendUiState.Idle -> {
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