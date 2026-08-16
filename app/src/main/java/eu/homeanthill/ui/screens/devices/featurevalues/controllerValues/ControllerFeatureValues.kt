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
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

import eu.homeanthill.R
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.Format
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

  LaunchedEffect(getValueUiState, device) {
    if (getValueUiState is ControllerFeatureValuesViewModel.ValuesUiState.Idle) {
      featureValues = controllerValuesWithDefaults(device, getValueUiState.values.orEmpty())
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
            .filter { it.enable && it.type == "controller" }
            .forEach { feature ->
              val currentValue = featureValues.find { it.featureUuid == feature.uuid }
              ControlCard(
                title = feature.name.replaceFirstChar { it.uppercase() },
                lastUpdated = controllerFeatureValuesViewModel.getPrettyDateFromLong(currentValue?.modifiedAt)
              ) {
                when (feature.spec.format) {
                  Format.BOOL -> {
                    OnControl(
                      isOn = currentValue?.value?.toInt() == 1,
                      onToggle = { isOn ->
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = if (isOn) 1.0 else 0.0) else it
                        }
                      }
                    )
                  }
                  Format.INT, Format.FLOAT -> {
                    val minValue = feature.spec.min ?: DEFAULT_SLIDER_MIN
                    val maxValue = feature.spec.max ?: DEFAULT_SLIDER_MAX
                    val step = feature.spec.step ?: DEFAULT_SLIDER_STEP
                    SliderControl(
                      value = currentValue?.value?.toFloat() ?: minValue,
                      range = minValue..maxValue,
                      step = step,
                      unit = feature.unit,
                      onValueChange = { newValue ->
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = newValue.toDouble()) else it
                        }
                      }
                    )
                  }
                  Format.LIST -> {
                    MaterialSpinner(
                      title = "",
                      options = controllerFeatureValuesViewModel.getOptions(feature),
                      selectedOption = controllerFeatureValuesViewModel.getSelectedOption(feature, currentValue),
                      onSelect = { option ->
                        val selectedValue = option.key.toDoubleOrNull() ?: return@MaterialSpinner
                        featureValues = featureValues.map {
                          if (it.featureUuid == feature.uuid) it.copy(value = selectedValue) else it
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
  step: Float = DEFAULT_SLIDER_STEP,
  unit: String = "",
  onValueChange: (Float) -> Unit
) {
  val start = range.start
  val end = range.endInclusive
  val snappedValue = snapToStep(value, start, end, step)
  val stepCount = sliderSteps(start, end, step)
  val displayValue = formatSliderValue(snappedValue, step)

  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Text(text = formatSliderValue(range.start, step), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
      Text(
        text = "$displayValue$unit",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
      Text(text = formatSliderValue(range.endInclusive, step), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
    }
    Slider(
      value = snappedValue,
      onValueChange = { onValueChange(snapToStep(it, start, end, step)) },
      valueRange = range,
      steps = stepCount,
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
      Text(text = formatSliderValue(range.start, step), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
      Text(text = formatSliderValue(range.endInclusive, step), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), fontSize = 12.sp)
    }
  }
}

private fun controllerValuesWithDefaults(
  device: Device?,
  values: List<DeviceFeatureValueResponse>
): List<DeviceFeatureValueResponse> {
  if (device == null) {
    return values
  }
  return device.features
    .filter { it.enable && it.type == "controller" }
    .sortedBy { it.order }
    .map { feature ->
      values.find { it.featureUuid == feature.uuid } ?: DeviceFeatureValueResponse(
        featureUuid = feature.uuid,
        type = feature.type,
        name = feature.name,
        value = defaultControllerValue(feature),
        createdAt = 0L,
        modifiedAt = 0L,
      )
    }
}

private fun defaultControllerValue(feature: Feature): Double {
  return when (feature.spec.format) {
    Format.BOOL -> 0.0
    Format.INT, Format.FLOAT -> feature.spec.min?.toDouble() ?: 0.0
    Format.LIST -> feature.spec.list?.firstOrNull()?.value?.toDouble() ?: 0.0
  }
}

private fun sliderSteps(start: Float, end: Float, step: Float): Int {
  if (step <= 0f || end <= start) {
    return 0
  }
  return max(((end - start) / step).roundToInt() - 1, 0)
}

private fun snapToStep(value: Float, start: Float, end: Float, step: Float): Float {
  val clampedValue = value.coerceIn(start, end)
  if (step <= 0f) {
    return clampedValue
  }
  val stepIndex = ((clampedValue - start) / step).roundToInt()
  return (start + stepIndex * step).coerceIn(start, end)
}

private fun formatSliderValue(value: Float, step: Float): String {
  val decimals = if (step <= 0f) {
    MAX_DECIMAL_PRECISION
  } else {
    max(0.0, ceil(-log10(step.toDouble()))).toInt()
  }
  return String.format(Locale.US, "%.${min(decimals, MAX_DECIMAL_PRECISION)}f", value)
}

private const val DEFAULT_SLIDER_MIN = 0f
private const val DEFAULT_SLIDER_MAX = 100f
private const val DEFAULT_SLIDER_STEP = 1f
private const val MAX_DECIMAL_PRECISION = 2
