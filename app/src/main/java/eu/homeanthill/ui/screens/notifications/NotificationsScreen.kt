package eu.homeanthill.ui.screens.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.homeanthill.R
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.ProfileNotification
import eu.homeanthill.ui.theme.AppTheme

@Composable
fun NotificationsScreen(
  notificationsUiState: NotificationsViewModel.NotificationsUiState,
  notificationsViewModel: NotificationsViewModel,
) {
  var isRefreshing by remember { mutableStateOf(false) }

  LaunchedEffect(notificationsUiState) {
    if (notificationsUiState !is NotificationsViewModel.NotificationsUiState.Loading) {
      isRefreshing = false
    }
  }

  NotificationsContent(
    notificationsUiState = notificationsUiState,
    isRefreshing = isRefreshing,
    onRefresh = {
      isRefreshing = true
      notificationsViewModel.loadNotifications()
    },
    formatDate = notificationsViewModel::getPrettyDateFromUnixEpoch,
  )
}

@Composable
fun NotificationsContent(
  notificationsUiState: NotificationsViewModel.NotificationsUiState,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  formatDate: (Long) -> String,
) {
  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    state = rememberPullToRefreshState(),
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = stringResource(R.string.notifications),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.tertiary,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.notifications_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f),
      )
      Spacer(modifier = Modifier.height(20.dp))

      when (notificationsUiState) {
        is NotificationsViewModel.NotificationsUiState.Error -> {
          Text(
            text = notificationsUiState.errorMessage,
            color = MaterialTheme.colorScheme.error,
          )
        }

        is NotificationsViewModel.NotificationsUiState.Loading -> {
          if (!isRefreshing) {
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
          }
        }

        is NotificationsViewModel.NotificationsUiState.Idle -> {
          if (notificationsUiState.notifications.isEmpty()) {
            Text(
              text = stringResource(R.string.notifications_empty),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            )
          } else {
            notificationsUiState.notifications.forEachIndexed { index, notification ->
              NotificationItemCard(
                number = index + 1,
                notification = notification,
                formattedDate = formatDate(notification.sentAt),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun NotificationItemCard(
  number: Int,
  notification: ProfileNotification,
  formattedDate: String,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = number.toString(),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f),
        modifier = Modifier.width(24.dp),
      )
      Box(
        modifier = Modifier
          .size(36.dp)
          .background(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            RoundedCornerShape(10.dp)
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(20.dp),
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Text(
            text = notification.body,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = deviceNames(notification.devices),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

private fun deviceNames(devices: List<Device>): String {
  if (devices.isEmpty()) {
    return "No matching devices"
  }
  return devices.joinToString(", ") { device ->
    device.name?.takeIf { it.isNotBlank() } ?: device.model.ifBlank { device.uuid }
  }
}

@Preview(showBackground = true)
@Composable
fun NotificationItemCardPreview() {
  AppTheme {
    NotificationItemCard(
      number = 1,
      notification = ProfileNotification(
        id = "notification-1",
        sentAt = 1713000000000L,
        title = "home anthill",
        body = "Device is offline",
        deviceCount = 1,
        devices = listOf(
          Device(
            id = "device-1",
            uuid = "uuid-1",
            mac = "AA:BB:CC:DD:EE:FF",
            name = "Kitchen sensor",
            manufacturer = "Acme",
            model = "Sensor-X",
            features = listOf(
              Feature(uuid = "feature-1", type = "sensor", name = "online", enable = true, order = 1, unit = "-")
            ),
            createdAt = "2024-01-01T00:00:00Z",
            modifiedAt = "2024-01-01T00:00:00Z",
          )
        ),
        provider = "fcm",
        providerMessageId = "message-1",
      ),
      formattedDate = "12:34:21 13/04/2026",
    )
  }
}
