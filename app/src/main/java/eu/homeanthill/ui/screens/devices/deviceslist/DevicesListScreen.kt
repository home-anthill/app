package eu.homeanthill.ui.screens.devices.deviceslist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import eu.homeanthill.R
import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.ui.screens.devices.DevicesRoute
import eu.homeanthill.ui.theme.AppTheme

@Composable
fun DevicesListScreen(
  devicesUiState: DevicesListViewModel.DevicesUiState,
  devicesViewModel: DevicesListViewModel,
  navController: NavController,
) {
  var isRefreshing by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    devicesViewModel.loadDevices()
  }

  LaunchedEffect(devicesUiState) {
    if (devicesUiState !is DevicesListViewModel.DevicesUiState.Loading) {
      isRefreshing = false
    }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    content = { padding ->
      PullToRefreshBox(
        isRefreshing = isRefreshing, onRefresh = {
          isRefreshing = true
          devicesViewModel.loadDevices()
        }, state = rememberPullToRefreshState(), modifier = Modifier
          .fillMaxSize()
          .padding(padding)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          when (devicesUiState) {
            is DevicesListViewModel.DevicesUiState.Error -> {
              Text(
                text = devicesUiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
              )
            }

            is DevicesListViewModel.DevicesUiState.Loading -> {
              if (!isRefreshing) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
              }
            }

            is DevicesListViewModel.DevicesUiState.Idle -> {
              val deviceList = devicesUiState.deviceList

              if (deviceList?.unassignedDevices?.isNotEmpty() == true) {
                Text(
                  text = stringResource(R.string.devices_unassigned),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                )
                deviceList.unassignedDevices.forEach { device ->
                  DeviceCard(
                    device = device, onClick = {
                      navController.currentBackStackEntry?.savedStateHandle?.set("device", device)
                      navController.currentBackStackEntry?.savedStateHandle?.set("home", null)
                      navController.currentBackStackEntry?.savedStateHandle?.set("room", null)
                      navController.navigate(route = DevicesRoute.FeatureValues.name)
                    })
                }
                HorizontalDivider(
                  thickness = 1.dp,
                  color = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
                )
              }

              // homes
              deviceList?.homeDevices?.forEach { homeWithDevices ->
                Row(
                  modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(40.dp),
                    contentAlignment = Alignment.CenterStart,
                  ) {
                    Icon(
                      imageVector = Icons.Default.Business,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "${homeWithDevices.home.name} (${homeWithDevices.home.location})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                // home rooms
                homeWithDevices.rooms.forEach { roomWithDevices ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.MeetingRoom,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.tertiary,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                      text = "${roomWithDevices.room.name} (${roomWithDevices.controllerDevices.size + roomWithDevices.sensorDevices.size})",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.tertiary
                    )
                  }

                  roomWithDevices.controllerDevices.forEach { device ->
                    DeviceCard(
                      device = device, onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("device", device)
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                          "home", homeWithDevices.home
                        )
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                          "room", roomWithDevices.room
                        )
                        navController.navigate(route = DevicesRoute.FeatureValues.name)
                      })
                  }
                  roomWithDevices.sensorDevices.forEach { sensor ->
                    DeviceCard(
                      device = sensor, onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("device", sensor)
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                          "home", homeWithDevices.home
                        )
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                          "room", roomWithDevices.room
                        )
                        navController.navigate(route = DevicesRoute.FeatureValues.name)
                      })
                  }
                }
                HorizontalDivider(
                  thickness = 1.dp,
                  color = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
                )
              }
            }
          }
        }
      }
    },
  )
}

@Composable
fun DeviceCard(
  device: Device,
  onClick: () -> Unit,
) {
  val hasController = device.features.any { it.type.lowercase().contains("controller") }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (!device.name.isNullOrBlank()) device.name else device.mac,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
          )
          Text(
            text = device.mac, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
          )
        }
        if (hasController) {
          CtrlBadge()
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(36.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        device.features.filter { it.enable && it.type.lowercase() == "sensor" }.forEach { feature ->
          FeatureIcon(feature)
          Spacer(modifier = Modifier.width(8.dp))
        }
      }
    }
  }
}

@Composable
fun CtrlBadge() {
  Surface(
    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
    color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Tune,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = stringResource(R.string.controller_badge),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun FeatureIcon(feature: Feature) {
  val iconRes = when (feature.name) {
    "temperature" -> R.drawable.device_thermostat_24px
    "humidity" -> R.drawable.invert_colors_24px
    "light" -> R.drawable.light_mode_24px
    "airpressure" -> R.drawable.compress_24px
    "airquality" -> R.drawable.eco_24px
    "motion" -> R.drawable.directions_run_24px
    "online" -> R.drawable.bolt_24px
    else -> R.drawable.question_mark_24px
  }

  Box(
    modifier = Modifier
      .size(36.dp)
      .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = ImageVector.vectorResource(iconRes),
      contentDescription = feature.name,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DeviceCardPreview() {
  AppTheme {
    DeviceCard(
      device = Device(
        id = "1",
        uuid = "uuid",
        mac = "1C:DB:D4:41:38:B4",
        name = "My Device name",
        manufacturer = "man",
        model = "model info",
        features = listOf(
          Feature("1", "sensor", "temperature", true, 1, "C"),
          Feature("2", "sensor", "humidity", true, 2, "%"),
          Feature("3", "sensor", "light", true, 3, "lux"),
          Feature("4", "sensor", "airquality", true, 4, ""),
          Feature("5", "controller", "setpoint", true, 5, "")
        ),
        createdAt = "",
        modifiedAt = ""
      ), onClick = {})
  }
}
