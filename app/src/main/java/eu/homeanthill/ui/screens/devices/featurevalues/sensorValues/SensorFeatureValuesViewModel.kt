package eu.homeanthill.ui.screens.devices.featurevalues.sensorValues

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModel

import eu.homeanthill.api.model.FeatureValue

class SensorFeatureValuesViewModel() : ViewModel() {

  fun getPrettyDateFromUnixEpoch(isoDate: Number): String {
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.ITALY)
    val netDate = Date(isoDate.toLong())
    return sdf.format(netDate)
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

  private fun toFixed(value: Number, precision: Int): String {
    return String.format("%.${precision}f", value.toDouble())
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