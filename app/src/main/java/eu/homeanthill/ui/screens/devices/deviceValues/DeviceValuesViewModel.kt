package eu.homeanthill.ui.screens.devices.deviceValues

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.IOException
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetFeatureDeviceValue
import eu.homeanthill.ui.components.SpinnerItemObj

class DeviceValuesViewModel(
  private val devicesRepository: DevicesRepository
) : ViewModel() {
  companion object {
    private const val TAG = "DeviceValuesViewModel"
  }

  sealed class SendUiState {
    data class Idle(val result: String?) : SendUiState()
    data class Error(val errorMessage: String) : SendUiState()
  }

  sealed class ValuesUiState {
    data class Idle(val values: List<DeviceFeatureValueResponse>?) : ValuesUiState()
    data object Loading : ValuesUiState()
    data class Error(val errorMessage: String) : ValuesUiState()
  }

  private val _sendUiState = MutableStateFlow<SendUiState>(SendUiState.Idle(null))
  val sendUiState: StateFlow<SendUiState> = _sendUiState
  private val _getValuesUiState = MutableStateFlow<ValuesUiState>(ValuesUiState.Idle(null))
  val getValueUiState: StateFlow<ValuesUiState> = _getValuesUiState

  private val setpoints = IntRange(17, 30).step(1).toList().toIntArray()
  private val modes = arrayOf("Cool", "Auto", "Heat", "Fan", "Dry")
  private val fanSpeeds = arrayOf("Min", "Med", "Max", "Auto", "Auto0")

  fun getPrettyDateFromUnixEpoch(unixEpoch: String): String {
    if (unixEpoch == "") {
      return ""
    }
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
    val netDate = Date(unixEpoch.toLong())
    return sdf.format(netDate)
  }

  fun getSetpoints(): List<SpinnerItemObj> {
    return setpoints.map { t -> SpinnerItemObj(t.toString(), t.toString()) }
  }

  fun getSetpointValue(name: String): Int {
    return setpoints.indexOfFirst { temp -> temp == name.toInt() } + 17
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

  suspend fun send(id: String, body: List<PostSetFeatureDeviceValue>) {
    try {
      val sendResponse: GenericMessageResponse = devicesRepository.repoPostSetValues(id, body)
      _sendUiState.emit(SendUiState.Idle(sendResponse.message))
    } catch (err: IOException) {
      Log.e(TAG, "send - error = $err")
      _sendUiState.emit(SendUiState.Error(err.message.toString()))
    }
  }

  suspend fun getValues(id: String): List<DeviceFeatureValueResponse> {
    _getValuesUiState.emit(ValuesUiState.Loading)
    delay(200)
    try {
      val values: List<DeviceFeatureValueResponse> = devicesRepository.repoGetDeviceValues(id)
      _getValuesUiState.emit(ValuesUiState.Idle(values))
      return values
    } catch (err: IOException) {
      Log.e(TAG, "getValue - error = $err")
      _getValuesUiState.emit(ValuesUiState.Error(err.message.toString()))
    }
    return listOf()
  }
}