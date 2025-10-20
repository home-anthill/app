package eu.homeanthill.ui.screens.devices.featurevalues.sensorValues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

import eu.homeanthill.R
import eu.homeanthill.api.model.FeatureValue

@Composable
fun SensorFeatureValues(
  featureValues: List<FeatureValue>?,
  sensorFeatureValuesViewModel: SensorFeatureValuesViewModel,
) {

  featureValues
    ?.filter { featureValue -> featureValue.feature.name != "online" }
    ?.forEach { featureValue ->
      Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp, horizontal = 20.dp)
          .clip(RoundedCornerShape(16.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            when (featureValue.feature.name) {
              // icons taken from https://fonts.google.com/icons?icon.size=24&icon.color=%23e3e3e3&icon.platform=android&icon.set=Material+Symbols
              "temperature" ->
                Icon(
                  imageVector = ImageVector.vectorResource(R.drawable.device_thermostat_24px),
                  contentDescription = "Temperature",
                  modifier = Modifier.size(45.dp)
                )

              "humidity" ->
                Icon(
                  imageVector = ImageVector.vectorResource(R.drawable.invert_colors_24px),
                  contentDescription = "Humidity",
                  modifier = Modifier.size(45.dp)
                )

              "light" ->
                Icon(
                  imageVector = ImageVector.vectorResource(R.drawable.light_mode_24px),
                  contentDescription = "Light",
                  modifier = Modifier.size(45.dp)
                )

              "motion" ->
                Icon(
                  imageVector = ImageVector.vectorResource(R.drawable.directions_run_24px),
                  contentDescription = "Motion",
                  modifier = Modifier.size(45.dp)
                )

              "airquality" ->
                Icon(
                  imageVector = ImageVector.vectorResource(R.drawable.cloud_24px),
                  contentDescription = "AirQuality",
                  modifier = Modifier.size(45.dp)
                )

              "airpressure" ->
                Icon(
                  imageVector = ImageVector.vectorResource(R.drawable.compress_24px),
                  contentDescription = "AirPressure",
                  modifier = Modifier.size(45.dp)
                )

              else ->
                Icon(
                  imageVector = ImageVector.vectorResource(R.drawable.warning_24px),
                  contentDescription = "Unsupported feature",
                  modifier = Modifier.size(45.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
              text = featureValue.feature.name.uppercase(),
              style = MaterialTheme.typography.bodyLarge,
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = sensorFeatureValuesViewModel.getValue(featureValue),
            style = MaterialTheme.typography.headlineLarge,
          )
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            text = sensorFeatureValuesViewModel.getPrettyDateFromUnixEpoch(featureValue.modifiedAt),
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
}