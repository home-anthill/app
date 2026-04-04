package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

import android.util.Log
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.api.model.DeviceFeatureValueResponse
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

  private val setpoints = (17..30).toList()
  private val modes = arrayOf("Cool", "Auto", "Heat", "Fan", "Dry")
  private val fanSpeeds = arrayOf("Min", "Med", "Max", "Auto", "Auto0")
  private val tolerances = (0..10).toList()

  // Pre-computed option lists — source arrays are constants so these never need to change.
  private val setpointOptions: List<SpinnerItemObj> =
    setpoints.map { t -> SpinnerItemObj(t.toString(), t.toString()) }
  private val modeOptions: List<SpinnerItemObj> =
    modes.map { mode -> SpinnerItemObj(mode, mode) }
  private val fanSpeedOptions: List<SpinnerItemObj> =
    fanSpeeds.map { fanSpeed -> SpinnerItemObj(fanSpeed, fanSpeed) }
  private val toleranceOptions: List<SpinnerItemObj> =
    tolerances.map { t -> SpinnerItemObj(t.toString(), t.toString()) }

  fun getSetpointByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it.featureUuid == uuid }
    val index = (v?.value?.toInt() ?: -999) - setpoints[0]
    if (v == null || v.value.toInt() == -999 || index < 0 || index >= setpoints.size) {
      return SpinnerItemObj(setpoints[0].toString(), setpoints[0].toString())
    }
    val res = setpoints[index]
    return SpinnerItemObj(res.toString(), res.toString())
  }

  fun getSetpoints(): List<SpinnerItemObj> = setpointOptions

  fun getSetpointValue(name: String): Int {
    return setpoints.indexOfFirst { temp -> temp == name.toInt() } + setpoints[0]
  }

  fun getToleranceByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it.featureUuid == uuid }
    val index = v?.value?.toInt() ?: -999
    if (v == null || index == -999 || index < 0 || index >= tolerances.size) {
      return SpinnerItemObj(tolerances[0].toString(), tolerances[0].toString())
    }
    val res = tolerances[index]
    return SpinnerItemObj(res.toString(), res.toString())
  }

  fun getTolerances(): List<SpinnerItemObj> = toleranceOptions

  fun getToleranceValue(name: String): Int {
    return tolerances.indexOfFirst { temp -> temp == name.toInt() }
  }

  fun getModeByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it.featureUuid == uuid }
    val index = (v?.value?.toInt() ?: -999) - 1
    if (v == null || v.value.toInt() == -999 || index < 0 || index >= modes.size) {
      return SpinnerItemObj(modes[0], modes[0])
    }
    val res = modes[index]
    return SpinnerItemObj(res, res)
  }

  fun getModes(): List<SpinnerItemObj> = modeOptions

  fun getModeValue(name: String): Int {
    return modes.indexOfFirst { mode -> mode == name } + 1
  }

  fun getFanSpeedByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it.featureUuid == uuid }
    val index = (v?.value?.toInt() ?: -999) - 1
    if (v == null || v.value.toInt() == -999 || index < 0 || index >= fanSpeeds.size) {
      return SpinnerItemObj(fanSpeeds[0], fanSpeeds[0])
    }
    val res = fanSpeeds[index]
    return SpinnerItemObj(res, res)
  }

  fun getFanSpeeds(): List<SpinnerItemObj> = fanSpeedOptions

  fun getFanSpeedValue(name: String): Int {
    return fanSpeeds.indexOfFirst { fanSpeed -> fanSpeed == name } + 1
  }

  fun getPrettyDateFromUnixEpoch(unixEpoch: String?): String {
    if (unixEpoch == null) {
      return ""
    }
    return ZonedDateTime.parse(unixEpoch).format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"))
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
              value = it.value,
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
}