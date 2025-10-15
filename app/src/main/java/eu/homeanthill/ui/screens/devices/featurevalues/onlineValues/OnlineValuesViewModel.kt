package eu.homeanthill.ui.screens.devices.featurevalues.onlineValues

import android.util.Log
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.OnlineValue
import eu.homeanthill.repository.OnlineRepository

class OnlineValuesViewModel(
  private val onlineRepository: OnlineRepository
) : ViewModel() {
  companion object {
    private const val TAG = "OnlineValuesViewModel"
  }

  sealed class OnlineValuesUiState {
    data class Idle(val onlineValue: OnlineValue?) : OnlineValuesUiState()
    data object Loading : OnlineValuesUiState()
    data class Error(val errorMessage: String) : OnlineValuesUiState()
  }

  private val _onlineValuesUiState =
    MutableStateFlow<OnlineValuesUiState>(OnlineValuesUiState.Idle(null))
  val onlineValuesUiState: StateFlow<OnlineValuesUiState> = _onlineValuesUiState

  fun getPrettyDateFromUnixEpoch(isoDate: String): String {
    val unixEpoch = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)
      .toInstant(ZoneOffset.ofTotalSeconds(0))
      .toEpochMilli()
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
    val netDate = Date(unixEpoch)
    return sdf.format(netDate)
  }

  fun isOffline(modifiedAtISO: String, currentTimeISO: String): Boolean {
    val modDate = LocalDateTime.parse(modifiedAtISO, DateTimeFormatter.ISO_DATE_TIME)
    val currentDate = LocalDateTime.parse(currentTimeISO, DateTimeFormatter.ISO_DATE_TIME)
    return modDate.toInstant(ZoneOffset.ofTotalSeconds(0))
      .toEpochMilli() < (currentDate.toInstant(ZoneOffset.ofTotalSeconds(0))
      .toEpochMilli() - (60 * 1000))
  }

  fun initDeviceValues(device: Device) {
    viewModelScope.launch {
      _onlineValuesUiState.emit(OnlineValuesUiState.Loading)
      delay(500)

      try {
        val value: OnlineValue = onlineRepository.repoGetOnlineValues(device.id)
        Log.d(TAG, "initDeviceValues - value = $value")
        _onlineValuesUiState.emit(OnlineValuesUiState.Idle(value))
      } catch (err: IOException) {
        _onlineValuesUiState.emit(OnlineValuesUiState.Error(err.message.toString()))
      }
    }
  }
}