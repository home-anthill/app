package eu.homeanthill.ui.screens.devices.featurevalues.onlineValues

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import eu.homeanthill.R

import eu.homeanthill.api.model.Device

@Composable
fun OnlineFeatureValues(
  device: Device?,
  onlineValuesUiState: OnlineFeatureValuesViewModel.OnlineValuesUiState,
  onlineFeatureValuesViewModel: OnlineFeatureValuesViewModel,
) {
  LaunchedEffect(Unit) {
    if (device != null) {
      onlineFeatureValuesViewModel.initDeviceValues(device)
    }
  }

  when (onlineValuesUiState) {
    is OnlineFeatureValuesViewModel.OnlineValuesUiState.Error -> {
      Text(
        text = onlineValuesUiState.errorMessage,
        color = MaterialTheme.colorScheme.error,
      )
    }

    is OnlineFeatureValuesViewModel.OnlineValuesUiState.Loading -> {
      CircularProgressIndicator()
    }

    is OnlineFeatureValuesViewModel.OnlineValuesUiState.Idle -> {
      if (onlineValuesUiState.onlineValue != null) {
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
              Icon(
                imageVector = ImageVector.vectorResource(R.drawable.bolt_24px),
                contentDescription = "PowerOutage",
                modifier = Modifier.size(45.dp)
              )
              Spacer(Modifier.weight(1f))
              Text(
                text = "ONLINE",
                style = MaterialTheme.typography.bodyLarge,
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (onlineFeatureValuesViewModel.isOffline(
                  onlineValuesUiState.onlineValue.modifiedAt,
                  onlineValuesUiState.onlineValue.currentTime
                )
              ) {
                Canvas(modifier = Modifier.size(30.dp), onDraw = {
                  drawCircle(color = Color.Red)
                })
                Text(
                  text = "Offline",
                  style = MaterialTheme.typography.headlineLarge,
                  modifier = Modifier.padding(start = 8.dp)
                )
              } else {
                Canvas(modifier = Modifier.size(30.dp), onDraw = {
                  drawCircle(color = Color.Green)
                })
                Text(
                  text = "Online",
                  style = MaterialTheme.typography.headlineLarge,
                  modifier = Modifier.padding(start = 8.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
              text = onlineFeatureValuesViewModel.getPrettyDateFromUnixEpoch(onlineValuesUiState.onlineValue.modifiedAt),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }
  }
}