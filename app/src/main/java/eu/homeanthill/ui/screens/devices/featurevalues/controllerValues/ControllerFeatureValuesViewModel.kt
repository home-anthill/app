package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

import android.util.Log
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import eu.homeanthill.api.model.Device
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.Format
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetFeatureDeviceValue
import eu.homeanthill.api.model.SendValueResult
import eu.homeanthill.ui.components.SpinnerItemObj


class ControllerFeatureValuesViewModel(
  private val devicesRepository: DevicesRepository
) : ViewModel() {
  companion object {
    private const val TAG = "ControllerValuesViewModel"
    private const val LOAD_DELAY_MS = 200L
  }

  sealed class ValuesUiState {
    data class Idle(val values: List<DeviceFeatureValueResponse>?) : ValuesUiState()
    data object Loading : ValuesUiState()
    data class Error(val errorMessage: String) : ValuesUiState()
  }

  private val _getValuesUiState = MutableStateFlow<ValuesUiState>(ValuesUiState.Idle(null))
  val getValueUiState: StateFlow<ValuesUiState> = _getValuesUiState

  private val _sendValueResult = MutableSharedFlow<SendValueResult>(extraBufferCapacity = 1)
  val sendValueResult: SharedFlow<SendValueResult> = _sendValueResult

  fun getOptions(feature: Feature): List<SpinnerItemObj> {
    return feature.spec.list.orEmpty().map { item ->
      SpinnerItemObj(key = item.value.toString(), value = item.text)
    }
  }

  fun getSelectedOption(
    feature: Feature,
    currentValue: DeviceFeatureValueResponse?
  ): SpinnerItemObj? {
    val currentIntValue = currentValue?.value?.roundToInt()
    return getOptions(feature).firstOrNull { it.key.toIntOrNull() == currentIntValue }
  }

  fun getPrettyDateFromUnixEpoch(unixEpoch: String?): String {
    if (unixEpoch.isNullOrBlank()) {
      return ""
    }
    return try {
      ZonedDateTime.parse(unixEpoch).format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"))
    } catch (e: Exception) {
      ""
    }
  }

  fun getPrettyDateFromLong(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) {
      return ""
    }
    val dtf = DateTimeFormatter
      .ofPattern("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
      .withZone(ZoneId.systemDefault())
    return dtf.format(Instant.ofEpochMilli(timestamp))
  }

  fun loadValues(id: String) {
    viewModelScope.launch {
      _getValuesUiState.emit(ValuesUiState.Loading)
      delay(LOAD_DELAY_MS)
      try {
        val values: List<DeviceFeatureValueResponse> = devicesRepository.repoGetDeviceValues(id)
        _getValuesUiState.emit(ValuesUiState.Idle(values))
      } catch (err: IOException) {
        Log.e(TAG, "loadValues - error = $err")
        _getValuesUiState.emit(ValuesUiState.Error(err.message.toString()))
      }
    }
  }

  fun sendCommands(
    device: Device, controllerFeatureValues: List<DeviceFeatureValueResponse>
  ) {
    viewModelScope.launch {
      try {
        val listToSend: List<PostSetFeatureDeviceValue> =
          controllerFeatureValues.filter { it.type == "controller" }.map {
            PostSetFeatureDeviceValue(
              featureUuid = it.featureUuid,
              type = it.type,
              name = it.name,
              value = normalizeCommandValue(device, it),
            )
          }
        val sendResponse: GenericMessageResponse =
          devicesRepository.repoPostSetValues(device.id, listToSend)
        _sendValueResult.emit(SendValueResult(sendResponse.message, false))
      } catch (err: IOException) {
        Log.e(TAG, "sendCommands - err = $err")
        _sendValueResult.emit(SendValueResult(err.message.toString(), true))
      }
    }
  }

  private fun normalizeCommandValue(device: Device, value: DeviceFeatureValueResponse): Double {
    val feature = device.features.find { it.uuid == value.featureUuid } ?: return value.value
    val min = feature.spec.min?.toDouble()
    val max = feature.spec.max?.toDouble()
    val clamped = when {
      min != null && max != null -> value.value.coerceIn(min, max)
      min != null -> value.value.coerceAtLeast(min)
      max != null -> value.value.coerceAtMost(max)
      else -> value.value
    }

    return when (feature.spec.format) {
      Format.BOOL -> if (clamped >= 1.0) 1.0 else 0.0
      Format.INT -> clamped.roundToInt().toDouble()
      Format.LIST -> {
        val allowedValues = feature.spec.list.orEmpty().map { it.value }
        val roundedValue = clamped.roundToInt()
        if (allowedValues.isEmpty() || roundedValue in allowedValues) {
          roundedValue.toDouble()
        } else {
          allowedValues.first().toDouble()
        }
      }
      Format.FLOAT -> clamped
    }
  }
}
