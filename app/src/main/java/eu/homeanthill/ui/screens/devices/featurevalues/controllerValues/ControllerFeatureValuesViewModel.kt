package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

import android.util.Log
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

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
  }

  sealed class ValuesUiState {
    data class Idle(val values: List<DeviceFeatureValueResponse>?) : ValuesUiState()
    data object Loading : ValuesUiState()
    data class Error(val errorMessage: String) : ValuesUiState()
  }

  private val _getValuesUiState = MutableStateFlow<ValuesUiState>(ValuesUiState.Idle(null))
  val getValueUiState: StateFlow<ValuesUiState> = _getValuesUiState

  private val setpoints = IntRange(17, 30).step(1).toList().toIntArray()
  private val modes = arrayOf("Cool", "Auto", "Heat", "Fan", "Dry")
  private val fanSpeeds = arrayOf("Min", "Med", "Max", "Auto", "Auto0")
  private val tolerances = IntRange(0, 10).step(1).toList().toIntArray()

  fun getSetpointByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it -> it.featureUuid == uuid }
    if (v == null || v.value.toInt() == -999) {
      return SpinnerItemObj(setpoints[0].toString(), setpoints[0].toString())
    }
    val res = setpoints[v.value.toInt() - setpoints[0]]
    return SpinnerItemObj(res.toString(), res.toString())
  }

  fun getSetpoints(): List<SpinnerItemObj> {
    return setpoints.map { t -> SpinnerItemObj(t.toString(), t.toString()) }
  }

  fun getSetpointValue(name: String): Int {
    return setpoints.indexOfFirst { temp -> temp == name.toInt() } + setpoints[0]
  }

  fun getToleranceByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it -> it.featureUuid == uuid }
    if (v == null || v.value.toInt() == -999) {
      return SpinnerItemObj(tolerances[0].toString(), tolerances[0].toString())
    }
    val res = tolerances[v.value.toInt()]
    return SpinnerItemObj(res.toString(), res.toString())
  }

  fun getTolerances(): List<SpinnerItemObj> {
    return tolerances.map { t -> SpinnerItemObj(t.toString(), t.toString()) }
  }

  fun getToleranceValue(name: String): Int {
    return tolerances.indexOfFirst { temp -> temp == name.toInt() }
  }

  fun getModeByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it -> it.featureUuid == uuid }
    if (v == null || v.value.toInt() == -999) {
      return SpinnerItemObj(modes[0], modes[0])
    }
    val res = modes[v.value.toInt() - 1]
    return SpinnerItemObj(res, res)
  }

  fun getModes(): List<SpinnerItemObj> {
    return modes.map { mode -> SpinnerItemObj(mode, mode) }
  }

  fun getModeValue(name: String): Int {
    return modes.indexOfFirst { mode -> mode == name } + 1
  }

  fun getFanSpeedByFeatureUuid(
    featureValues: List<DeviceFeatureValueResponse>, uuid: String
  ): SpinnerItemObj {
    val v: DeviceFeatureValueResponse? = featureValues.find { it -> it.featureUuid == uuid }
    if (v == null || v.value.toInt() == -999) {
      return SpinnerItemObj(fanSpeeds[0], fanSpeeds[0])
    }
    val res = fanSpeeds[v.value.toInt() - 1]
    return SpinnerItemObj(res, res)
  }

  fun getFanSpeeds(): List<SpinnerItemObj> {
    return fanSpeeds.map { fanSpeed -> SpinnerItemObj(fanSpeed, fanSpeed) }
  }

  fun getFanSpeedValue(name: String): Int {
    return fanSpeeds.indexOfFirst { fanSpeed -> fanSpeed == name } + 1
  }

  fun getPrettyDateFromUnixEpoch(unixEpoch: String?): String {
    if (unixEpoch == null) {
      return ""
    }
    return ZonedDateTime.parse(unixEpoch).format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"))
  }

  suspend fun getValues(id: String): List<DeviceFeatureValueResponse> {
    _getValuesUiState.emit(ValuesUiState.Loading)
    delay(200)
    try {
      val values: List<DeviceFeatureValueResponse> = devicesRepository.repoGetDeviceValues(id)
      _getValuesUiState.emit(ValuesUiState.Idle(values))
      return values
    } catch (err: IOException) {
      Log.e(TAG, "getValues - error = $err")
      _getValuesUiState.emit(ValuesUiState.Error(err.message.toString()))
    }
    return listOf()
  }

  suspend fun sendCommands(
    device: Device, controllerFeatureValues: List<DeviceFeatureValueResponse>
  ): SendValueResult {
    try {
      val listToSend: List<PostSetFeatureDeviceValue> =
        controllerFeatureValues.filter { it -> it.type == "controller" }.map { it ->
          PostSetFeatureDeviceValue(
            featureUuid = it.featureUuid,
            type = it.type,
            name = it.name,
            value = it.value,
          )
        }
      val sendResponse: GenericMessageResponse =
        devicesRepository.repoPostSetValues(device.id, listToSend)
      return SendValueResult(sendResponse.message, false)
    } catch (err: IOException) {
      Log.e(TAG, "sendCommands - err = $err")
      return SendValueResult(err.message.toString(), true)
    }
  }
}