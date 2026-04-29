package eu.homeanthill.ui.screens.devices.featurevalues.onlineValues

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import eu.homeanthill.R
import eu.homeanthill.api.model.Device

@Composable
fun OnlineFeatureValues(
  device: Device?,
  onlineValuesUiState: OnlineFeatureValuesViewModel.OnlineValuesUiState,
  onlineFeatureValuesViewModel: OnlineFeatureValuesViewModel,
  refreshTrigger: Int = 0,
) {
  LaunchedEffect(refreshTrigger) {
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
      CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }

    is OnlineFeatureValuesViewModel.OnlineValuesUiState.Idle -> {
      if (onlineValuesUiState.onlineValue != null) {
        val isOffline = onlineFeatureValuesViewModel.isOffline(
          onlineValuesUiState.onlineValue.modifiedAt,
          onlineValuesUiState.onlineValue.currentTime
        )

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Box {
            // Orange top border accent
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.secondary)
                .align(Alignment.TopCenter)
            )

            // sensor card
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
            ) {
              // header
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
              ) {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.bolt_24px),
                    contentDescription = "Online",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                  text = stringResource(R.string.online),
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.tertiary
                )
              }

              Spacer(modifier = Modifier.height(24.dp))

              // value
              Text(
                text = if (isOffline) stringResource(R.string.offline_label) else stringResource(R.string.online_label),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isOffline) MaterialTheme.colorScheme.error else Color(0xFF388E3C)
              )

              Spacer(modifier = Modifier.height(16.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
              Spacer(modifier = Modifier.height(16.dp))

              // date
              Text(
                text = stringResource(
                  R.string.updated_at,
                  onlineFeatureValuesViewModel.getPrettyDateFromUnixEpoch(onlineValuesUiState.onlineValue.modifiedAt)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
              )
            }
          }
        }
      }
    }
  }
}
