package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.SendValueResult
import eu.homeanthill.ui.components.MaterialSpinner
import eu.homeanthill.ui.components.SwitchWithLabel

@Composable
fun ControllerValuesScreen(
  device: Device?,
  getValueUiState: ControllerFeatureValuesViewModel.ValuesUiState,
  controllerFeatureValuesViewModel: ControllerFeatureValuesViewModel,
  onSendResult: (SendValueResult) -> Unit,
  refreshTrigger: Int = 0,
) {
  var featureValues: List<DeviceFeatureValueResponse> by remember { mutableStateOf(listOf()) }

  // Trigger a values load whenever refreshTrigger changes (initial load + pull-to-refresh).
  LaunchedEffect(refreshTrigger) {
    if (device != null) {
      controllerFeatureValuesViewModel.loadValues(device.id)
    }
  }

  // Initialise local featureValues from ViewModel state whenever a new load completes.
  LaunchedEffect(getValueUiState) {
    if (getValueUiState is ControllerFeatureValuesViewModel.ValuesUiState.Idle) {
      featureValues = getValueUiState.values ?: listOf()
    }
  }

  // Collect send results and forward them to the parent screen for snackbar display.
  LaunchedEffect(Unit) {
    controllerFeatureValuesViewModel.sendValueResult.collect { result ->
      onSendResult(result)
    }
  }

  when (getValueUiState) {
    is ControllerFeatureValuesViewModel.ValuesUiState.Error -> {
      Spacer(modifier = Modifier.height(100.dp))
      Text(
        text = "Can't load current values",
        style = MaterialTheme.typography.bodyLarge
      )
    }

    is ControllerFeatureValuesViewModel.ValuesUiState.Loading -> {
      CircularProgressIndicator()
    }

    is ControllerFeatureValuesViewModel.ValuesUiState.Idle -> {
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
            .forEach { feature ->
              when (feature.name) {
                "on" -> {
                  SwitchWithLabel(
                    label = "On/Off",
                    state = featureValues.find { it.featureUuid == feature.uuid }?.value?.toInt() == 1,
                    onStateChange = { value ->
                      featureValues = featureValues.map { featureValue ->
                        if (featureValue.featureUuid == feature.uuid)
                          featureValue.copy(value = if (value) 1.0 else 0.0)
                        else featureValue
                      }
                    }
                  )
                }

                "setpoint" -> {
                  MaterialSpinner(
                    title = "Setpoint",
                    options = controllerFeatureValuesViewModel.getSetpoints(),
                    onSelect = { option ->
                      featureValues = featureValues.map { featureValue ->
                        if (featureValue.featureUuid == feature.uuid)
                          featureValue.copy(value = controllerFeatureValuesViewModel.getSetpointValue(option.value).toDouble())
                        else featureValue
                      }
                    },
                    modifier = Modifier.padding(10.dp),
                    selectedOption = controllerFeatureValuesViewModel.getSetpointByFeatureUuid(
                      featureValues,
                      feature.uuid
                    ),
                  )
                }

                "mode" -> {
                  MaterialSpinner(
                    title = "Mode",
                    options = controllerFeatureValuesViewModel.getModes(),
                    onSelect = { option ->
                      featureValues = featureValues.map { featureValue ->
                        if (featureValue.featureUuid == feature.uuid)
                          featureValue.copy(value = controllerFeatureValuesViewModel.getModeValue(option.value).toDouble())
                        else featureValue
                      }
                    },
                    modifier = Modifier.padding(10.dp),
                    selectedOption = controllerFeatureValuesViewModel.getModeByFeatureUuid(
                      featureValues,
                      feature.uuid
                    )
                  )
                }

                "fanSpeed" -> {
                  MaterialSpinner(
                    title = "Fan speed",
                    options = controllerFeatureValuesViewModel.getFanSpeeds(),
                    onSelect = { option ->
                      featureValues = featureValues.map { featureValue ->
                        if (featureValue.featureUuid == feature.uuid)
                          featureValue.copy(value = controllerFeatureValuesViewModel.getFanSpeedValue(option.value).toDouble())
                        else featureValue
                      }
                    },
                    modifier = Modifier.padding(10.dp),
                    selectedOption = controllerFeatureValuesViewModel.getFanSpeedByFeatureUuid(
                      featureValues,
                      feature.uuid
                    )
                  )
                }

                "tolerance" -> {
                  MaterialSpinner(
                    title = "Tolerance",
                    options = controllerFeatureValuesViewModel.getTolerances(),
                    onSelect = { option ->
                      featureValues = featureValues.map { featureValue ->
                        if (featureValue.featureUuid == feature.uuid)
                          featureValue.copy(value = controllerFeatureValuesViewModel.getToleranceValue(option.value).toDouble())
                        else featureValue
                      }
                    },
                    modifier = Modifier.padding(10.dp),
                    selectedOption = controllerFeatureValuesViewModel.getToleranceByFeatureUuid(
                      featureValues,
                      feature.uuid
                    )
                  )
                }
              }
            }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = controllerFeatureValuesViewModel.getPrettyDateFromUnixEpoch(device.modifiedAt),
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(modifier = Modifier.height(16.dp))
          TextButton(
            onClick = {
              controllerFeatureValuesViewModel.sendCommands(
                device = device,
                controllerFeatureValues = featureValues
              )
            },
          ) {
            Text(text = "Send")
          }
        }
      }
    }
  }
}