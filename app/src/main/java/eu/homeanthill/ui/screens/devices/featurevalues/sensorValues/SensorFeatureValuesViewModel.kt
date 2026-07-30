package eu.homeanthill.ui.screens.devices.featurevalues.sensorValues

import android.util.Log
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.FeatureValue
import eu.homeanthill.api.model.Format
import eu.homeanthill.repository.DevicesRepository

class SensorFeatureValuesViewModel(
  private val devicesRepository: DevicesRepository,
) : ViewModel() {
  enum class ThermostatMode(val sensorValue: Float) {
    ERROR(-1.0f),
    SLEEP(0.0f),
    COLD(1.0f),
    HEAT(2.0f),
  }

  // DateTimeFormatter is immutable and thread-safe; no risk of concurrent access issues.
  private val dtf = DateTimeFormatter
    .ofPattern("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
    .withZone(ZoneId.systemDefault())

  fun getPrettyDateFromUnixEpoch(isoDate: Long): String {
    return dtf.format(Instant.ofEpochMilli(isoDate))
  }

  private fun getMotionValue(value: Int): String {
    return if (value == 0) {
      "False"
    } else {
      "True"
    }
  }

  private fun getAirQualityValue(value: Int): String {
    return when (value) {
      0 -> "Poor"
      1 -> "Low"
      2 -> "Good"
      3 -> "Excellent"
      else -> "Unknown"
    }
  }

  private fun formatByStep(value: Double, step: Float?): String {
    val decimals = if (step == null || step <= 0f) {
      DEFAULT_DECIMAL_PRECISION
    } else {
      max(0.0, ceil(-log10(step.toDouble()))).toInt()
    }
    return String.format(Locale.US, "%.${min(decimals, MAX_DECIMAL_PRECISION)}f", value)
  }

  fun getValue(featureValue: FeatureValue): String {
    return when (featureValue.feature.name) {
      "motion" -> getMotionValue(featureValue.value.toInt())
      "airquality" -> getAirQualityValue(featureValue.value.toInt())
      else -> "${formatByStep(featureValue.value, featureValue.feature.spec.step)} ${featureValue.feature.unit}"
    }
  }

  fun getThermostatMode(featureValue: FeatureValue): ThermostatMode? {
    if (
      featureValue.feature.name != "mode" ||
      featureValue.feature.spec.format != Format.FLOAT
    ) return null

    return ThermostatMode.entries.firstOrNull { mode ->
      featureValue.value == mode.sensorValue.toDouble()
    }
  }

  fun supportsNotifications(featureValue: FeatureValue): Boolean {
    return featureValue.feature.name in NOTIFICATION_FEATURE_NAMES
  }

  fun setFeatureNotificationSilenced(
    device: Device,
    featureUuid: String,
    notificationSilenced: Boolean,
    onSuccess: () -> Unit = {},
    onError: () -> Unit = {},
  ) {
    viewModelScope.launch {
      try {
        devicesRepository.repoSetFeatureNotification(device.id, featureUuid, notificationSilenced)
        onSuccess()
      } catch (err: IOException) {
        Log.e(TAG, "setFeatureNotificationSilenced - err = $err")
        onError()
      }
    }
  }

  companion object {
    private const val TAG = "SensorValuesViewModel"
    private const val DEFAULT_DECIMAL_PRECISION = 2
    private const val MAX_DECIMAL_PRECISION = 2
    private val NOTIFICATION_FEATURE_NAMES = setOf("motion", "mode")
  }
}
