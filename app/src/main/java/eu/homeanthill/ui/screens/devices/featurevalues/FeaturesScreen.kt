package eu.homeanthill.ui.screens.devices.featurevalues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import eu.homeanthill.R

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.components.MaterialSpinner
import eu.homeanthill.ui.components.SpinnerItemObj
import eu.homeanthill.ui.screens.devices.DevicesRoute
import eu.homeanthill.ui.screens.devices.featurevalues.controllerValues.ControllerFeatureValuesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.controllerValues.ControllerValuesScreen
import eu.homeanthill.ui.screens.devices.featurevalues.onlineValues.OnlineFeatureValuesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.onlineValues.OnlineFeatureValues
import eu.homeanthill.ui.screens.devices.featurevalues.sensorValues.SensorFeatureValues
import eu.homeanthill.ui.screens.devices.featurevalues.sensorValues.SensorFeatureValuesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(
  featureValuesUiState: FeaturesViewModel.FeatureValuesUiState,
  featureValuesViewModel: FeaturesViewModel,
  navController: NavController,
) {
  // inputs
  val device: Device? =
    navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
  val initialHome: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
  val initialRoom: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  var isRefreshing by remember { mutableStateOf(false) }
  var refreshTrigger by remember { mutableIntStateOf(0) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }

  val deviceStateUpdatedMsg = stringResource(R.string.device_state_updated)
  val deviceStateErrorMsg = stringResource(R.string.device_state_error)

  LaunchedEffect(Unit) {
    if (device != null) {
      featureValuesViewModel.initDeviceValues(device)
    }
  }

  LaunchedEffect(featureValuesUiState) {
    if (featureValuesUiState !is FeaturesViewModel.FeatureValuesUiState.Loading) {
      isRefreshing = false
    }
  }

  if (showDeleteDialog && device != null) {
    DeleteDeviceDialog(
      device = device,
      onDismissRequest = { showDeleteDialog = false },
      onConfirm = {
        featureValuesViewModel.deleteDevice(device.id) {
          navController.popBackStack()
        }
        showDeleteDialog = false
      }
    )
  }

  if (showSettingsDialog && device != null && featureValuesUiState is FeaturesViewModel.FeatureValuesUiState.Idle) {
    DeviceSettingsDialog(
      device = device,
      homes = featureValuesUiState.homes,
      initialHome = initialHome,
      initialRoom = initialRoom,
      onDismissRequest = { showSettingsDialog = false },
      onSave = { name, homeId, roomId ->
        featureValuesViewModel.updateDeviceSettings(device.id, name, homeId, roomId)
        showSettingsDialog = false
      }
    )
  }

  Scaffold(
    containerColor = Color.Black,
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    content = { padding ->
      PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
          if (device != null) {
            isRefreshing = true
            refreshTrigger++
            featureValuesViewModel.initDeviceValues(device)
          }
        },
        state = rememberPullToRefreshState(),
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          // Back Navigation
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White
              )
            }
            Text(
              text = stringResource(R.string.devices_back),
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          }

          if (device != null) {
            val currentDevice = if (featureValuesUiState is FeaturesViewModel.FeatureValuesUiState.Idle && featureValuesUiState.deviceValue != null) {
              featureValuesUiState.deviceValue.device
            } else {
              device
            }
            // Device Header Card
            DeviceHeaderCard(
              device = currentDevice,
              home = initialHome,
              room = initialRoom,
              onSettingsClick = {
                showSettingsDialog = true
              }
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          when (featureValuesUiState) {
            is FeaturesViewModel.FeatureValuesUiState.Error -> {
              Text(
                text = featureValuesUiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
              )
            }

            is FeaturesViewModel.FeatureValuesUiState.Loading -> {
              if (!isRefreshing) {
                CircularProgressIndicator(color = Color(0xFFBD5700))
              }
            }

            is FeaturesViewModel.FeatureValuesUiState.Idle -> {
              val hasOnline = device?.features?.any { feature ->
                feature.type == "sensor" && feature.name == "online"
              } == true

              val sensorFeatures = featureValuesUiState.deviceValue?.sensorFeatureValues?.filter {
                it.feature.name.lowercase() != "online"
              } ?: emptyList()

              // 1. Connectivity/Sensors Section
              if (hasOnline || sensorFeatures.isNotEmpty()) {
                SectionHeader(title = stringResource(R.string.sensors), icon = Icons.Default.MonitorHeart)

                if (hasOnline) {
                  val onlineFeatureValuesViewModel = koinViewModel<OnlineFeatureValuesViewModel>()
                  val onlineValuesUiState by onlineFeatureValuesViewModel.onlineValuesUiState.collectAsStateWithLifecycle()
                  OnlineFeatureValues(
                    device = device,
                    onlineValuesUiState = onlineValuesUiState,
                    onlineFeatureValuesViewModel = onlineFeatureValuesViewModel,
                    refreshTrigger = refreshTrigger,
                  )
                }

                if (sensorFeatures.isNotEmpty()) {
                  val sensorFeatureValuesViewModel = koinViewModel<SensorFeatureValuesViewModel>()
                  SensorFeatureValues(
                    featureValues = featureValuesUiState.deviceValue?.sensorFeatureValues,
                    sensorFeatureValuesViewModel = sensorFeatureValuesViewModel,
                  )
                }
              }

              // 3. Controls Section
              if (featureValuesUiState.deviceValue?.controllerFeatureValues?.isNotEmpty() == true) {
                SectionHeader(title = stringResource(R.string.controls), icon = Icons.Default.Tune)
                val controllerFeatureValuesViewModel = koinViewModel<ControllerFeatureValuesViewModel>()
                val getValueUiState by controllerFeatureValuesViewModel.getValueUiState.collectAsStateWithLifecycle()
                ControllerValuesScreen(
                  device = device,
                  getValueUiState = getValueUiState,
                  controllerFeatureValuesViewModel = controllerFeatureValuesViewModel,
                  refreshTrigger = refreshTrigger,
                  onSendResult = { result ->
                    coroutineScope.launch {
                      if (!result.isError && device != null) {
                        // Refresh device details after successful command
                        refreshTrigger++
                        featureValuesViewModel.initDeviceValues(device)
                      }
                      snackbarHostState.showSnackbar(
                        message = if (result.isError) deviceStateErrorMsg else deviceStateUpdatedMsg,
                        duration = if (result.isError) SnackbarDuration.Long else SnackbarDuration.Short
                      )
                    }
                  }
                )
              }

              Spacer(modifier = Modifier.height(32.dp))
              HorizontalDivider(color = Color(0xFF1E1E1E), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
              Spacer(modifier = Modifier.height(32.dp))

              // Delete Device Button
              Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                  .padding(horizontal = 16.dp, vertical = 24.dp)
                  .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFB71C1C),
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(stringResource(R.string.delete_device), fontWeight = FontWeight.Bold)
                }
              }
              Spacer(modifier = Modifier.height(24.dp))
            }
          }
        }
      }
    },
  )
}

@Composable
fun DeviceSettingsDialog(
  device: Device,
  homes: List<Home>,
  initialHome: Home?,
  initialRoom: Room?,
  onDismissRequest: () -> Unit,
  onSave: (String, String, String) -> Unit
) {
  var name by remember { mutableStateOf(device.name ?: "") }
  var selectedHome by remember { mutableStateOf(initialHome) }
  var selectedRoom by remember { mutableStateOf(initialRoom) }

  val homesOptions = homes.map { SpinnerItemObj(it.id, it.name) }
  val roomsOptions = selectedHome?.rooms?.map { SpinnerItemObj(it.id, it.name) } ?: emptyList()

  val isSaveEnabled = name.trim().isNotEmpty() && selectedHome != null && selectedRoom != null

  Dialog(onDismissRequest = onDismissRequest) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.device_settings),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Device Name
        Text(
          text = stringResource(R.string.device_name_label),
          style = MaterialTheme.typography.labelMedium,
          color = Color.White,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
          value = name,
          onValueChange = { name = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF2C2C2C),
            unfocusedContainerColor = Color(0xFF2C2C2C),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Home Dropdown
        Text(
          text = stringResource(R.string.home_label),
          style = MaterialTheme.typography.labelMedium,
          color = Color.White,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        MaterialSpinner(
          title = stringResource(R.string.select_home),
          options = homesOptions,
          selectedOption = selectedHome?.let { SpinnerItemObj(it.id, it.name) },
          onSelect = { option ->
            selectedHome = homes.find { it.id == option.key }
            selectedRoom = null
          },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Room Dropdown
        Text(
          text = stringResource(R.string.room_label),
          style = MaterialTheme.typography.labelMedium,
          color = Color.White,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        MaterialSpinner(
          title = stringResource(R.string.select_room),
          options = roomsOptions,
          selectedOption = selectedRoom?.let { SpinnerItemObj(it.id, it.name) },
          onSelect = { option ->
            selectedRoom = selectedHome?.rooms?.find { it.id == option.key }
          },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismissRequest) {
            Text(text = stringResource(R.string.cancel), color = Color.White)
          }
          Spacer(modifier = Modifier.width(16.dp))
          TextButton(
            onClick = {
              if (selectedHome != null && selectedRoom != null) {
                onSave(name, selectedHome!!.id, selectedRoom!!.id)
              }
            },
            enabled = isSaveEnabled
          ) {
            Text(
              text = stringResource(R.string.save),
              color = if (isSaveEnabled) Color.White else Color.Gray,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun DeleteDeviceDialog(
  device: Device,
  onDismissRequest: () -> Unit,
  onConfirm: () -> Unit
) {
  Dialog(onDismissRequest = onDismissRequest) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(
          text = stringResource(R.string.device_delete_title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = buildAnnotatedString {
            val deviceDisplayName = if (!device.name.isNullOrBlank()) device.name else device.mac
            val text = stringResource(R.string.device_delete_confirm, deviceDisplayName)
            val index = text.indexOf(deviceDisplayName)
            append(text.substring(0, index))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
              append(deviceDisplayName)
            }
            append(text.substring(index + deviceDisplayName.length))
          },
          color = Color.White,
          style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
              append(stringResource(R.string.device_delete_important))
            }
            append(stringResource(R.string.device_delete_warning))
          },
          color = Color.White,
          style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismissRequest) {
            Text(text = stringResource(R.string.cancel), color = Color.White)
          }
          Spacer(modifier = Modifier.width(16.dp))
          Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFB71C1C),
              contentColor = Color.White
            ),
          ) {
            Text(text = stringResource(R.string.delete), fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
        .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(8.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color(0xFFFD7E13),
        modifier = Modifier.size(18.dp)
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = Color(0xFFFD7E13)
    )
  }
}

@Composable
fun DeviceHeaderCard(
  device: Device,
  home: Home?,
  room: Room?,
  onSettingsClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2C))
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (!device.name.isNullOrBlank()) device.name else device.mac,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          modifier = Modifier.weight(1f)
        )
        IconButton(
          onClick = onSettingsClick,
          modifier = Modifier
            .size(40.dp)
            .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.device_settings),
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = Color(0xFF1E1E1E), thickness = 1.dp)
      Spacer(modifier = Modifier.height(24.dp))

      DeviceDetailItem(label = stringResource(R.string.device_model), value = device.model)
      Spacer(modifier = Modifier.height(16.dp))
      DeviceDetailItem(label = stringResource(R.string.device_mac), value = device.mac)
      Spacer(modifier = Modifier.height(16.dp))
      DeviceDetailItem(
        label = stringResource(R.string.device_location),
        value = if (home != null && room != null) "${home.name} - ${room.name}" else stringResource(R.string.device_unassigned)
      )
    }
  }
}

@Composable
fun DeviceDetailItem(label: String, value: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = label,
      color = Color(0xFFFD7E13),
      fontWeight = FontWeight.Bold,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.width(100.dp)
    )
    Text(
      text = value,
      color = Color.Gray,
      style = MaterialTheme.typography.bodyMedium
    )
  }
}
