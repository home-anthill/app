package eu.homeanthill.ui.screens.devices.featurevalues.sensorValues

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.ViewModel

import eu.homeanthill.api.model.FeatureValue

class SensorFeatureValuesViewModel : ViewModel() {
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
      0 -> "Extreme pollution"
      1 -> "High pollution"
      2 -> "Mid pollution"
      3 -> "Low pollution"
      else -> "Unknown"
    }
  }

  private fun toFixed(value: Double, precision: Int): String {
    return String.format("%.${precision}f", value)
  }

  fun getValue(featureValue: FeatureValue): String {
    return when (featureValue.feature.name) {
      "temperature" -> "${toFixed(featureValue.value, 2)} ${featureValue.feature.unit}"
      "humidity" -> "${toFixed(featureValue.value, 2)} ${featureValue.feature.unit}"
      "light" -> "${toFixed(featureValue.value, 0)} ${featureValue.feature.unit}"
      "motion" -> getMotionValue(featureValue.value.toInt())
      "airquality" -> getAirQualityValue(featureValue.value.toInt())
      "airpressure" -> "${toFixed(featureValue.value, 0)} ${featureValue.feature.unit}"
      else -> "${featureValue.value} ${featureValue.feature.unit}"
    }
  }
}