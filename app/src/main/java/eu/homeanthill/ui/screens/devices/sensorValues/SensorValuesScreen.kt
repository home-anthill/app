package eu.homeanthill.ui.screens.devices.sensorValues

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.homeanthill.R

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.FeatureValue
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.ui.screens.devices.sensorValues.SensorValuesViewModel

@Composable
fun SensorValuesScreen(
    sensorValuesUiState: SensorValuesViewModel.SensorValuesUiState,
    sensorValuesViewModel: SensorValuesViewModel,
    navController: NavController,
) {
    // inputs
    val device: Device? =
        navController.previousBackStackEntry?.savedStateHandle?.get<Device>("device")
    val home: Home? = navController.previousBackStackEntry?.savedStateHandle?.get<Home>("home")
    val room: Room? = navController.previousBackStackEntry?.savedStateHandle?.get<Room>("room")

    LaunchedEffect(Unit) {
        if (device != null) {
            sensorValuesViewModel.initDeviceValues(device)
        }
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
                when (sensorValuesUiState) {
                    is SensorValuesViewModel.SensorValuesUiState.Error -> {
                        Text(
                            text = sensorValuesUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    is SensorValuesViewModel.SensorValuesUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is SensorValuesViewModel.SensorValuesUiState.Idle -> {
                        sensorValuesUiState.deviceValue?.featureValues?.forEach { featureValue ->
                            eu.homeanthill.ui.screens.devices.sensorValues.FeatureValueCard(
                                sensorValuesViewModel = sensorValuesViewModel,
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
    sensorValuesViewModel: SensorValuesViewModel,
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
                    // icons taken from https://fonts.google.com/icons?icon.size=24&icon.color=%23e3e3e3&icon.platform=android&icon.set=Material+Symbols
                    "temperature" ->
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.device_thermostat_24px),
                            contentDescription = "Temperature",
                        )

                    "humidity" ->
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.invert_colors_24px),
                            contentDescription = "Humidity",
                        )

                    "light" ->
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.light_mode_24px),
                            contentDescription = "Light",
                        )

                    "motion" ->
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.directions_run_24px),
                            contentDescription = "Motion",
                        )

                    "airquality" ->
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.cloud_24px),
                            contentDescription = "AirQuality",
                        )

                    "airpressure" ->
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.compress_24px),
                            contentDescription = "AirPressure",
                        )

                    else ->
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.warning_24px),
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
                text = sensorValuesViewModel.getValue(featureValue),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = sensorValuesViewModel.getPrettyDateFromUnixEpoch(featureValue.modifiedAt),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}