package eu.homeanthill.ui.screens.devices.sensorValues

import android.util.Log
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.DeviceValue
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.FeatureValue

class SensorValuesViewModel(
  private val devicesRepository: DevicesRepository
) : ViewModel() {
  companion object {
    private const val TAG = "SensorValuesViewModel"
  }

  sealed class ValuesUiState {
    data class Idle(val deviceValue: DeviceValue?) : ValuesUiState()
    data object Loading : ValuesUiState()
    data class Error(val errorMessage: String) : ValuesUiState()
  }

  private val _sensorValuesUiState =
    MutableStateFlow<ValuesUiState>(ValuesUiState.Idle(null))
  val sensorValuesUiState: StateFlow<ValuesUiState> = _sensorValuesUiState

  fun getPrettyDateFromUnixEpoch(unixEpoch: String): String {
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
    val netDate = Date(unixEpoch.toLong())
    return sdf.format(netDate)
  }

  private fun getMotionValue(value: Int): String {
    return if (value == 0) {
      "False"
    } else {
      "True"
    }
  }

  private fun getAirQualityValue(value: Int): String {
    return when (value) {
      0 -> "Extreme pollution"
      1 -> "High pollution"
      2 -> "Mid pollution"
      3 -> "Low pollution"
      else -> "Unknown"
    }
  }

  private fun toFixed(value: Number, precision: Int): String {
    return String.format("%.${precision}f", value.toDouble())
  }

  fun getValue(featureValue: FeatureValue): String {
    return when (featureValue.feature.name) {
      "temperature" -> "${toFixed(featureValue.value, 2)} ${featureValue.feature.unit}"
      "humidity" -> "${toFixed(featureValue.value, 2)} ${featureValue.feature.unit}"
      "light" -> "${toFixed(featureValue.value, 0)} ${featureValue.feature.unit}"
      "motion" -> getMotionValue(featureValue.value.toInt())
      "airquality" -> getAirQualityValue(featureValue.value.toInt())
      "airpressure" -> "${toFixed(featureValue.value, 0)} ${featureValue.feature.unit}"
      else -> "${featureValue.value} ${featureValue.feature.unit}"
    }
  }

  fun initDeviceValues(device: Device) {
    viewModelScope.launch {
      _sensorValuesUiState.emit(ValuesUiState.Loading)
      delay(500)

      try {
        val values: List<DeviceFeatureValueResponse> =
          devicesRepository.repoGetDeviceValues(device.id)
        Log.d(TAG, "initDeviceValues - values = $values")

        val deviceValue = DeviceValue(
          device = device,
          featureValues = getFeatureValues(device, values),
        )
        Log.d(TAG, "initDeviceValues - deviceValue = $deviceValue")
        _sensorValuesUiState.emit(ValuesUiState.Idle(deviceValue))
      } catch (err: IOException) {
        _sensorValuesUiState.emit(ValuesUiState.Error(err.message.toString()))
      }
    }
  }

  private fun getFeatureValues(
    device: Device,
    values: List<DeviceFeatureValueResponse>
  ): List<FeatureValue> {
    // order by 'order'
    val sortedFeatures = device.features.sortedBy { it: Feature -> it.order.toString() }
    val featureValues = sortedFeatures.map { feature ->
      val sensorValue: DeviceFeatureValueResponse? =
        values.find { value -> value.featureUuid == feature.uuid }
      val featureValue = FeatureValue(
        feature = feature,
        value = sensorValue?.value ?: -1,
        createdAt = sensorValue?.createdAt ?: "",
        modifiedAt = sensorValue?.modifiedAt ?: "",
      )
      return@map featureValue
    }
    return featureValues
  }
}