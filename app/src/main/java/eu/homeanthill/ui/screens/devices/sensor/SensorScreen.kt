package eu.homeanthill.ui.screens.devices.sensor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.FeatureValue
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room

@Composable
fun SensorScreen(
    sensorUiState: SensorViewModel.SensorUiState,
    sensorViewModel: SensorViewModel,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    // inputs
    val device: Device? =
        navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
    val home: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
    val room: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

    if (device != null) {
        sensorViewModel.initDeviceValues(device)
    }

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Sensor",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (home != null && room != null) {
                    Text(
                        text = "${home.name} ${home.location} - ${room.name} ${room.floor}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (device != null) {
                    Text(
                        text = device.mac,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${device.manufacturer} - ${device.model}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                when (sensorUiState) {
                    is SensorViewModel.SensorUiState.Error -> {
                        Text(
                            text = sensorUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is SensorViewModel.SensorUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is SensorViewModel.SensorUiState.Idle -> {
                        sensorUiState.deviceValue?.featureValues?.forEach { featureValue ->
                            FeatureValueCard(
                                sensorViewModel = sensorViewModel,
                                featureValue = featureValue,
                            )
                        }
                    }
                }
            }
        },
    )
}


@Composable
fun FeatureValueCard(
    sensorViewModel: SensorViewModel,
    featureValue: FeatureValue
) {
    Card(
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                when (featureValue.feature.name) {
                    "temperature" ->
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Temperature",
                        )
                    "humidity" ->
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Humidity",
                        )
                    "light" ->
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Light",
                        )
                    "motion" ->
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Motion",
                        )
                    "airquality" ->
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "AirQuality",
                        )
                    "airpressure" ->
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "AirPressure",
                        )
                    "poweroutage" ->
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "PowerOutage",
                        )
                    else ->
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Unsupported feature",
                        )
                }
                Text(
                    text = featureValue.feature.name.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${featureValue.value} ${featureValue.feature.unit}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = sensorViewModel.getPrettyDateFromUnixEpoch(featureValue.modifiedAt),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}