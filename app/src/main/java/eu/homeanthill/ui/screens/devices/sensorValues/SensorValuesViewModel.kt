package eu.homeanthill.ui.screens.devices.sensorValues

import android.util.Log
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.SensorValue
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

    sealed class SensorValuesUiState {
        data class Idle(val deviceValue: DeviceValue?) : SensorValuesUiState()
        data object Loading : SensorValuesUiState()
        data class Error(val errorMessage: String) : SensorValuesUiState()
    }

    private val _sensorValuesUiState =
        MutableStateFlow<SensorValuesUiState>(SensorValuesUiState.Idle(null))
    val sensorValuesUiState: StateFlow<SensorValuesUiState> = _sensorValuesUiState

    fun getPrettyDateFromUnixEpoch(unixEpoch: String): String {
        val sdf = SimpleDateFormat.getDateInstance()
        val netDate = Date(unixEpoch.toLong())
        return sdf.format(netDate)
    }

    fun initDeviceValues(device: Device) {
        viewModelScope.launch {
            _sensorValuesUiState.emit(SensorValuesUiState.Loading)
            delay(500)

            try {
                val values: List<SensorValue> = devicesRepository.repoGetSensorValues(device.id)
                Log.d(TAG, "initDeviceValues - values = $values")

                val deviceValue = DeviceValue(
                    device = device,
                    featureValues = getFeatureValues(device, values),
                )
                Log.d(TAG, "initDeviceValues - deviceValue = $deviceValue")
                _sensorValuesUiState.emit(SensorValuesUiState.Idle(deviceValue))
            } catch (err: IOException) {
                _sensorValuesUiState.emit(SensorValuesUiState.Error(err.message.toString()))
            }
        }
    }

    private fun getFeatureValues(device: Device, values: List<SensorValue>): List<FeatureValue> {
        // order by 'order'
        val sortedFeatures = device.features.sortedBy { it: Feature -> it.order.toString() }
        val featureValues = sortedFeatures.map { feature ->
            val sensorValue: SensorValue? = values.find { value -> value.uuid == feature.uuid }
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