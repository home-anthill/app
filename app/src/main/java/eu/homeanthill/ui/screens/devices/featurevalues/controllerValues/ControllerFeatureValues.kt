package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

import eu.homeanthill.R
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.SendValueResult
import eu.homeanthill.ui.components.MaterialSpinner

@Composable
fun ControllerValuesScreen(
  device: Device?,
  getValueUiState: ControllerFeatureValuesViewModel.ValuesUiState,
  controllerFeatureValuesViewModel: ControllerFeatureValuesViewModel,
  onSendResult: (SendValueResult) -> Unit,
  refreshTrigger: Int = 0,
) {
  var featureValues: List<DeviceFeatureValueResponse> by remember { mutableStateOf(listOf()) }

  LaunchedEffect(refreshTrigger) {
    if (device != null) {
      controllerFeatureValuesViewModel.loadValues(device.id)
    }
  }

  LaunchedEffect(getValueUiState) {
    if (getValueUiState is ControllerFeatureValuesViewModel.ValuesUiState.Idle) {
      featureValues = getValueUiState.values ?: listOf()
    }
  }

  LaunchedEffect(Unit) {
    controllerFeatureValuesViewModel.sendValueResult.collect { result ->
      onSendResult(result)
    }
  }

  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    // Header with Send Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(R.string.controls),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Button(
        onClick = {
          if (device != null) {
            controllerFeatureValuesViewModel.sendCommands(device, featureValues)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.secondary,
          contentColor = MaterialTheme.colorScheme.tertiary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(40.dp)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.send_commands), fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }

    when (getValueUiState) {
      is ControllerFeatureValuesViewModel.ValuesUiState.Error -> {
        Text(
          text = stringResource(R.string.load_values_error),
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(vertical = 24.dp)
        )
      }

      is ControllerFeatureValuesViewModel.ValuesUiState.Loading -> {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
      }

      is ControllerFeatureValuesViewModel.ValuesUiState.Idle -> {
        if (device != null) {
          device.features
            .filter { it.type == "controller" }
            .forEach { feature ->
              val currentValue = featureValues.find { it.featureUuid == feature.uuid }
              ControlCard(
                title = feature.name.replaceFirstChar { it.uppercase() },
                lastUpdated = controllerFeatureValuesViewModel.getPrettyDateFromLong(currentValue?.modifiedAt)
              ) {
                when (feature.name.lowercase()) {
                  "on" -> {
                    OnControl(
                      isOn = currentValue?.value?.toInt() == 1,
                      onToggle = { isOn ->
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = if (isOn) 1.0 else 0.0) else it
                        }
                      }
                    )
                  }
                  "setpoint" -> {
                    SliderControl(
                      value = currentValue?.value?.toFloat() ?: 17f,
                      range = 17f..30f,
                      steps = 12,
                      unit = "°C",
                      onValueChange = { newValue ->
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = newValue.toDouble()) else it
                        }
                      }
                    )
                  }
                  "tolerance" -> {
                    SliderControl(
                      value = currentValue?.value?.toFloat() ?: 0f,
                      range = 0f..10f,
                      steps = 9,
                      onValueChange = { newValue ->
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = newValue.toDouble()) else it
                        }
                      }
                    )
                  }
                  "mode" -> {
                    MaterialSpinner(
                      title = "",
                      options = controllerFeatureValuesViewModel.getModes(),
                      selectedOption = controllerFeatureValuesViewModel.getModeByFeatureUuid(featureValues, feature.uuid),
                      onSelect = { option ->
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = controllerFeatureValuesViewModel.getModeValue(option.value).toDouble()) else it
                        }
                      },
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                  "fanspeed" -> {
                    MaterialSpinner(
                      title = "",
                      options = controllerFeatureValuesViewModel.getFanSpeeds(),
                      selectedOption = controllerFeatureValuesViewModel.getFanSpeedByFeatureUuid(featureValues, feature.uuid),
                      onSelect = { option ->
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = controllerFeatureValuesViewModel.getFanSpeedValue(option.value).toDouble()) else it
                        }
                      },
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }
              }
            }
        }
      }
    }
  }
}

@Composable
fun ControlCard(
  title: String,
  lastUpdated: String,
  content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary
        )
        if (lastUpdated.isNotEmpty()) {
          Text(
            text = stringResource(R.string.updated_at, lastUpdated),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      content()
    }
  }
}

@Composable
fun OnControl(isOn: Boolean, onToggle: (Boolean) -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Switch(
      checked = isOn,
      onCheckedChange = onToggle,
      colors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.tertiary,
        checkedTrackColor = MaterialTheme.colorScheme.secondary,
        uncheckedThumbColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
        uncheckedTrackColor = MaterialTheme.colorScheme.outline
      )
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = if (isOn) stringResource(R.string.on) else stringResource(R.string.off),
      color = MaterialTheme.colorScheme.tertiary,
      style = MaterialTheme.typography.bodyLarge
    )
  }
}

@Composable
fun SliderControl(
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  steps: Int = 0,
  unit: String = "",
  onValueChange: (Float) -> Unit
) {
  val start = range.start.roundToInt()
  val end = range.endInclusive.roundToInt()
  val integerValue = value.roundToInt().coerceIn(start, end)

  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Text(text = range.start.toInt().toString(), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
      Text(
        text = "$integerValue$unit",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
      Text(text = range.endInclusive.toInt().toString(), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
    }
    Slider(
      value = integerValue.toFloat(),
      onValueChange = { onValueChange(it.roundToInt().coerceIn(start, end).toFloat()) },
      valueRange = range,
      steps = steps,
      colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.tertiary,
        activeTrackColor = MaterialTheme.colorScheme.secondary,
        inactiveTrackColor = MaterialTheme.colorScheme.outline
      )
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(text = range.start.toInt().toString(), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
      Text(text = range.endInclusive.toInt().toString(), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
    }
  }
}
