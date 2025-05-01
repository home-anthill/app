package eu.homeanthill.ui.screens.devices.deviceValues

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

import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.ControllerValue
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetDeviceValue
import eu.homeanthill.ui.components.SpinnerItemObj

class DeviceValuesViewModel(
    private val devicesRepository: DevicesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "DeviceValuesViewModel"
    }

    sealed class DeviceValuesUiState {
        data class Idle(val deviceValue: ControllerValue?) : DeviceValuesUiState()
        data object Loading : DeviceValuesUiState()
        data class Error(val errorMessage: String) : DeviceValuesUiState()
    }

    private val _deviceValuesUiState =
        MutableStateFlow<DeviceValuesUiState>(DeviceValuesUiState.Idle(null))
    val deviceValuesUiState: StateFlow<DeviceValuesUiState> = _deviceValuesUiState

    private val temps = IntRange(17, 30).step(1).toList().toIntArray()
    private val modes = arrayOf("Cool", "Auto", "Heat", "Fan", "Dry")
    private val fanSpeeds = arrayOf("Min", "Med", "Max", "Auto", "Auto0")

    fun getPrettyDateFromUnixEpoch(unixEpoch: String): String {
        val sdf = SimpleDateFormat.getDateInstance()
        val netDate = Date(unixEpoch.toLong())
        return sdf.format(netDate)
    }

    fun getTemperatures(): List<SpinnerItemObj> {
        return temps.map { t -> SpinnerItemObj(t.toString(), t.toString()) }
    }
    fun getTemperatureSpinnerObj(temperature: Int): SpinnerItemObj {
        return SpinnerItemObj(
            temperature.toString(),
            temperature.toString()
        )
    }
    fun getModes(): List<SpinnerItemObj> {
        val mds = modes.mapIndexed { i, t -> SpinnerItemObj(t, t) }
        Log.d(TAG, "getModes - mds = $mds")
        return mds
    }
    fun getModeValue(name: String): Int {
        return modes.indexOfFirst { mode -> mode == name } + 1
    }
    fun getModeSpinnerObj(mode: Int): SpinnerItemObj {
        val obj = SpinnerItemObj(
            modes[mode-1],
            modes[mode-1]
        )
        Log.d(TAG, "getModeSpinnerObj - obj = $obj")
        return obj
    }
    fun getFanSpeeds(): List<SpinnerItemObj> {
        return fanSpeeds.map { t -> SpinnerItemObj(t, t) }
    }
    fun getFanSpeedValue(name: String): Int {
        return fanSpeeds.indexOfFirst { fanSpeed -> fanSpeed == name } + 1
    }
    fun getFanSpeedSpinnerObj(fanSpeed: Int): SpinnerItemObj {
        return SpinnerItemObj(
            fanSpeeds[fanSpeed-1],
            fanSpeed.toString()
        )
    }

    suspend fun send(id: String, body: PostSetDeviceValue) {
        val sendResponse: GenericMessageResponse = devicesRepository.repoPostSetValues(id, body)
        Log.d(TAG, "initDeviceValues - sendResponse = $sendResponse")
    }

    fun initDeviceValues(device: Device) {
        viewModelScope.launch {
            _deviceValuesUiState.emit(DeviceValuesUiState.Loading)
            delay(500)

            try {
                val value: ControllerValue = devicesRepository.repoGetControllerValues(device.id)
                Log.d(TAG, "initDeviceValues - value = $value")
                _deviceValuesUiState.emit(DeviceValuesUiState.Idle(value))
            } catch (err: IOException) {
                _deviceValuesUiState.emit(DeviceValuesUiState.Error(err.message.toString()))
            }
        }
    }
}