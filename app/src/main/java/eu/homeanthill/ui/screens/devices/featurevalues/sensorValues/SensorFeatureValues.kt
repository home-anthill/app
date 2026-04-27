package eu.homeanthill.ui.screens.devices.featurevalues.sensorValues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import eu.homeanthill.R
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.FeatureValue
import eu.homeanthill.ui.theme.AppTheme

@Composable
fun SensorFeatureValues(
  featureValues: List<FeatureValue>?,
  sensorFeatureValuesViewModel: SensorFeatureValuesViewModel,
) {
  val filteredValues = featureValues?.filter { it.feature.name.lowercase() != "online" }

  if (!filteredValues.isNullOrEmpty()) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      filteredValues.forEach { featureValue ->
        SensorCard(
          featureValue = featureValue,
          displayValue = sensorFeatureValuesViewModel.getValue(featureValue),
          lastUpdated = sensorFeatureValuesViewModel.getPrettyDateFromUnixEpoch(featureValue.modifiedAt)
        )
      }
    }
  }
}

@Composable
fun SensorCard(
  featureValue: FeatureValue,
  displayValue: String,
  lastUpdated: String
) {
  val type = featureValue.feature.type.lowercase()
  val name = featureValue.feature.name.lowercase()

  val iconRes = when {
    type.contains("temperature") || name.contains("temperature") -> R.drawable.device_thermostat_24px
    type.contains("humidity") || name.contains("humidity") || name.contains("humidty") -> R.drawable.invert_colors_24px
    type.contains("light") || name.contains("light") -> R.drawable.light_mode_24px
    type.contains("pressure") || name.contains("pressure") -> R.drawable.compress_24px
    type.contains("air quality") || name.contains("air quality") || type.contains("eco") || name.contains("eco") || name.contains("airquality") -> R.drawable.eco_24px
    type.contains("motion") || name.contains("motion") || type.contains("pir") || name.contains("pir") -> R.drawable.directions_run_24px
    else -> R.drawable.question_mark_24px
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
  ) {
    Box {
      // Orange top border accent
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .background(Color(0xFFBD5700).copy(alpha = 0.5f))
          .align(Alignment.TopCenter)
      )

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Start
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
              .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = ImageVector.vectorResource(iconRes),
              contentDescription = featureValue.feature.name,
              tint = Color(0xFFFD7E13),
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(16.dp))
          Text(
            text = featureValue.feature.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = displayValue,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFD7E13)
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF1E1E1E), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = stringResource(R.string.updated_at, lastUpdated),
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun SensorCardPreview() {
  AppTheme(darkTheme = true) {
    SensorCard(
      featureValue = FeatureValue(
        feature = Feature("1", "sensor", "temperature", true, 1, "C"),
        value = 24.5,
        createdAt = 0L,
        modifiedAt = 1713000000L
      ),
      displayValue = "24.5 °C",
      lastUpdated = "12:34:21 13/04/2026"
    )
  }
}
