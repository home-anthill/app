package eu.homeanthill.ui.screens.devices.sensor

import android.util.Log
import java.io.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Value
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.DeviceValue
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.FeatureValue
import java.text.SimpleDateFormat
import java.util.Date

class SensorViewModel(
    private val devicesRepository: DevicesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "SensorViewModel"
    }

    sealed class SensorUiState {
        data class Idle(val deviceValue: DeviceValue?) : SensorUiState()
        data object Loading : SensorUiState()
        data class Error(val errorMessage: String) : SensorUiState()
    }

    private val _sensorUiState =
        MutableStateFlow<SensorUiState>(SensorUiState.Idle(null))
    val sensorUiState: StateFlow<SensorUiState> = _sensorUiState

    fun getPrettyDateFromUnixEpoch(unixEpoch: String): String {
        val sdf = SimpleDateFormat("HH:MM:SS dd/MM/yyyy")
        val netDate = Date(unixEpoch.toLong())
        return sdf.format(netDate)
    }

    fun initDeviceValues(device: Device) {
        viewModelScope.launch {
            _sensorUiState.emit(SensorUiState.Loading)
            delay(500)

            try {
                val values: List<Value> = devicesRepository.repoGetValues(device.id)
                Log.d(TAG, "initDeviceValues - values = $values")

                val deviceValue = DeviceValue(
                    device = device,
                    featureValues = getFeatureValues(device, values),
                )
                Log.d(TAG, "initDeviceValues - deviceValue = $deviceValue")
                _sensorUiState.emit(SensorUiState.Idle(deviceValue))
            } catch (err: IOException) {
                _sensorUiState.emit(SensorUiState.Error(err.message.toString()))
            }
        }
    }

    private fun getFeatureValues(device: Device, values: List<Value>): List<FeatureValue> {
        // order by 'order'
        val sortedFeatures = device.features.sortedBy { it: Feature -> it.order.toString() }
        val featureValues = sortedFeatures.map { feature ->
                val sensorValue: Value? = values.find{ value -> value.uuid == feature.uuid }
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