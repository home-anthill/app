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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import eu.homeanthill.api.model.OnlineStatus

@Composable
fun OnlineFeatureValues(
  device: Device?,
  onlineValuesUiState: OnlineFeatureValuesViewModel.OnlineValuesUiState,
  onlineFeatureValuesViewModel: OnlineFeatureValuesViewModel,
  refreshTrigger: Int = 0,
  onNotificationUpdated: (notificationSilenced: Boolean) -> Unit = {},
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
      if (onlineValuesUiState.onlineStatus != null) {
        val status = onlineValuesUiState.onlineStatus.status
        val onlineFeature = device?.features?.firstOrNull { feature ->
          feature.enable && feature.type == "sensor" && feature.name == "online"
        }
        var notificationSilenced by remember(device?.id, onlineFeature?.uuid, onlineFeature?.notificationSilenced) {
          mutableStateOf(onlineFeature?.notificationSilenced ?: false)
        }
        var notificationUpdating by remember(device?.id, onlineFeature?.uuid) { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
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
                IconButton(
                  onClick = notification@{
                    val currentDevice = device ?: return@notification
                    val feature = onlineFeature ?: return@notification
                    val nextNotificationSilenced = !notificationSilenced

                    notificationSilenced = nextNotificationSilenced
                    notificationUpdating = true
                    onlineFeatureValuesViewModel.setFeatureNotificationSilenced(
                      device = currentDevice,
                      featureUuid = feature.uuid,
                      notificationSilenced = nextNotificationSilenced,
                      onSuccess = {
                        notificationUpdating = false
                        onNotificationUpdated(nextNotificationSilenced)
                      },
                      onError = {
                        notificationSilenced = !nextNotificationSilenced
                        notificationUpdating = false
                      },
                    )
                  },
                  enabled = device != null && onlineFeature != null && !notificationUpdating,
                  modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                  val notificationIcon = if (notificationSilenced) {
                    Icons.Default.NotificationsOff
                  } else {
                    Icons.Default.Notifications
                  }
                  Icon(
                    imageVector = notificationIcon,
                    contentDescription = if (notificationSilenced) {
                      stringResource(R.string.enable_notifications)
                    } else {
                      stringResource(R.string.silence_notifications)
                    },
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(24.dp))

              // value
              Text(
                text = when (status) {
                  OnlineStatus.ONLINE -> stringResource(R.string.online_label)
                  OnlineStatus.OFFLINE -> stringResource(R.string.offline_label)
                  OnlineStatus.UNKNOWN -> stringResource(R.string.unknown_label)
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = when (status) {
                  OnlineStatus.ONLINE -> Color(0xFF388E3C)
                  OnlineStatus.OFFLINE -> MaterialTheme.colorScheme.error
                  OnlineStatus.UNKNOWN -> MaterialTheme.colorScheme.outlineVariant
                }
              )

              Spacer(modifier = Modifier.height(16.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
              Spacer(modifier = Modifier.height(16.dp))

              // date
              onlineValuesUiState.onlineStatus.modifiedAt?.let { modifiedAt ->
                Text(
                  text = stringResource(
                    R.string.updated_at,
                    onlineFeatureValuesViewModel.getPrettyDateFromUnixEpoch(modifiedAt)
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
}
