package eu.homeanthill.ui.screens.devices.featurevalues.onlineValues

import android.util.Log
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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
import eu.homeanthill.BuildConfig
import eu.homeanthill.repository.OnlineRepository

class OnlineFeatureValuesViewModel(
  private val onlineRepository: OnlineRepository
) : ViewModel() {
  companion object {
    private const val TAG = "OnlineValuesViewModel"
    private const val OFFLINE_THRESHOLD_MS = 60 * 1000L
    private const val LOAD_DELAY_MS = 500L
  }

  // DateTimeFormatter is immutable and thread-safe; no risk of concurrent access issues.
  private val dtf = DateTimeFormatter
    .ofPattern("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
    .withZone(ZoneId.systemDefault())

  sealed class OnlineValuesUiState {
    data class Idle(val onlineValue: OnlineValue?) : OnlineValuesUiState()
    data object Loading : OnlineValuesUiState()
    data class Error(val errorMessage: String) : OnlineValuesUiState()
  }

  private val _onlineValuesUiState =
    MutableStateFlow<OnlineValuesUiState>(OnlineValuesUiState.Idle(null))
  val onlineValuesUiState: StateFlow<OnlineValuesUiState> = _onlineValuesUiState

  fun getPrettyDateFromUnixEpoch(isoDate: String): String {
    if (BuildConfig.DEBUG) Log.d(TAG, "isoDate = $isoDate")
    val instant = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)
      .toInstant(ZoneOffset.UTC)
    return dtf.format(instant)
  }

  fun isOffline(modifiedAtISO: String, currentTimeISO: String): Boolean {
    val modEpoch = LocalDateTime.parse(modifiedAtISO, DateTimeFormatter.ISO_DATE_TIME)
      .toInstant(ZoneOffset.UTC).toEpochMilli()
    val currentEpoch = LocalDateTime.parse(currentTimeISO, DateTimeFormatter.ISO_DATE_TIME)
      .toInstant(ZoneOffset.UTC).toEpochMilli()
    return modEpoch < currentEpoch - OFFLINE_THRESHOLD_MS
  }

  fun initDeviceValues(device: Device) {
    viewModelScope.launch {
      _onlineValuesUiState.emit(OnlineValuesUiState.Loading)
      delay(LOAD_DELAY_MS)
      try {
        val value: OnlineValue = onlineRepository.repoGetOnlineValues(device.id)
        _onlineValuesUiState.emit(OnlineValuesUiState.Idle(value))
      } catch (err: IOException) {
        _onlineValuesUiState.emit(OnlineValuesUiState.Error(err.message.toString()))
      }
    }
  }
}