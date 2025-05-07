package eu.homeanthill.ui.screens.devices.deviceValues

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.api.model.ControllerValue
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetDeviceValue
import eu.homeanthill.ui.components.SpinnerItemObj
import kotlinx.coroutines.flow.StateFlow

class DeviceValuesViewModel(
    private val devicesRepository: DevicesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "DeviceValuesViewModel"
    }

    sealed class SendUiState {
        data class Idle(val result: String?) : SendUiState()
        data object Loading : SendUiState()
        data class Error(val errorMessage: String) : SendUiState()
    }

    private val _sendUiState = MutableStateFlow<SendUiState>(SendUiState.Idle(null))
    val sendUiState: StateFlow<SendUiState> = _sendUiState


    private val temps = IntRange(17, 30).step(1).toList().toIntArray()
    private val modes = arrayOf("Cool", "Auto", "Heat", "Fan", "Dry")
    private val fanSpeeds = arrayOf("Min", "Med", "Max", "Auto", "Auto0")

    fun getPrettyDateFromUnixEpoch(unixEpoch: String): String {
        if (unixEpoch == "") {
            return ""
        }
        val sdf = SimpleDateFormat.getDateInstance()
        val netDate = Date(unixEpoch.toLong())
        return sdf.format(netDate)
    }

    fun getTemperatures(): List<SpinnerItemObj> {
        return temps.map { t -> SpinnerItemObj(t.toString(), t.toString()) }
    }

    fun getTemperatureValue(name: String): Int {
        return temps.indexOfFirst { temp -> temp == name.toInt() } + 17
    }

    fun getModes(): List<SpinnerItemObj> {
        return modes.map { mode -> SpinnerItemObj(mode, mode) }
    }

    fun getModeValue(name: String): Int {
        return modes.indexOfFirst { mode -> mode == name } + 1
    }

    fun getFanSpeeds(): List<SpinnerItemObj> {
        return fanSpeeds.map { fanSpeed -> SpinnerItemObj(fanSpeed, fanSpeed) }
    }

    fun getFanSpeedValue(name: String): Int {
        return fanSpeeds.indexOfFirst { fanSpeed -> fanSpeed == name } + 1
    }

    suspend fun send(id: String, body: PostSetDeviceValue) {
        _sendUiState.emit(SendUiState.Loading)
        delay(200)

        try {
            val sendResponse: GenericMessageResponse = devicesRepository.repoPostSetValues(id, body)
            Log.d(TAG, "send - sendResponse = $sendResponse")
            _sendUiState.emit(SendUiState.Idle(sendResponse.message))
        } catch (err: IOException) {
            Log.e(TAG, "send - sendResponse = $err")
            _sendUiState.emit(SendUiState.Error(err.message.toString()))
        }
    }

    suspend fun getValue(id: String): ControllerValue {
        val value: ControllerValue = devicesRepository.repoGetControllerValues(id)
        Log.d(TAG, "getValue - value = $value")
        return value
    }
}