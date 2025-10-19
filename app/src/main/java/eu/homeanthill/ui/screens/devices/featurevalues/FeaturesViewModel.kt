package eu.homeanthill.ui.screens.devices.featurevalues

import android.util.Log
import java.io.IOException
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

class FeaturesViewModel(
  private val devicesRepository: DevicesRepository
) : ViewModel() {
  companion object {
    private const val TAG = "FeaturesViewModel"
  }

  sealed class FeatureValuesUiState {
    data class Idle(val deviceValue: DeviceValue?) : FeatureValuesUiState()
    data object Loading : FeatureValuesUiState()
    data class Error(val errorMessage: String) : FeatureValuesUiState()
  }

  sealed class SendUiState {
    data class Idle(val result: String?) : SendUiState()
    data class Error(val errorMessage: String) : SendUiState()
  }

  private val _featureValuesUiState =
    MutableStateFlow<FeatureValuesUiState>(FeatureValuesUiState.Idle(null))
  val featureValuesUiState: StateFlow<FeatureValuesUiState> = _featureValuesUiState

  fun initDeviceValues(device: Device) {
    viewModelScope.launch {
      _featureValuesUiState.emit(FeatureValuesUiState.Loading)
      delay(500)
      try {
        val values: List<DeviceFeatureValueResponse> =
          devicesRepository.repoGetDeviceValues(device.id)
        val deviceValue = DeviceValue(
          device = device,
          sensorFeatureValues = getFeatureValues(device, values, "sensor"),
          controllerFeatureValues = getFeatureValues(device, values, "controller"),
        )
        _featureValuesUiState.emit(FeatureValuesUiState.Idle(deviceValue))
      } catch (err: IOException) {
        _featureValuesUiState.emit(FeatureValuesUiState.Error(err.message.toString()))
        Log.e(TAG, "initDeviceValues - err = $err")
      }
    }
  }

  private fun getFeatureValues(
    device: Device,
    values: List<DeviceFeatureValueResponse>,
    type: String,
  ): List<FeatureValue> {
    // order by 'order'
    val sortedFeatures = device.features.sortedBy { it: Feature -> it.order.toString() }
    val featureValues = sortedFeatures
      .filter { feature -> feature.type == type }
      .map { feature ->
        val sensorValue: DeviceFeatureValueResponse? =
          values.find { value -> value.featureUuid == feature.uuid }
        val featureValue = FeatureValue(
          feature = feature,
          value = sensorValue?.value ?: -999,
          createdAt = sensorValue?.createdAt ?: 0,
          modifiedAt = sensorValue?.modifiedAt ?: 0,
        )
        return@map featureValue
      }
    return featureValues
  }
}