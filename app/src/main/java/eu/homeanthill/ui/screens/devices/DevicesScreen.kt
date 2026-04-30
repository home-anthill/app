package eu.homeanthill.ui.screens.devices

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel

import eu.homeanthill.ui.screens.devices.deviceslist.DevicesListScreen
import eu.homeanthill.ui.screens.devices.deviceslist.DevicesListViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.FeaturesScreen
import eu.homeanthill.ui.screens.devices.featurevalues.FeaturesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.controllerValues.ControllerFeatureValuesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.onlineValues.OnlineFeatureValuesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.sensorValues.SensorFeatureValuesViewModel

@Composable
fun DevicesScreen(
) {
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background,
  ) {
    val navController = rememberNavController()
    NavHost(
      navController = navController,
      route = Graph.DEVICES_GRAPH,
      startDestination = DevicesRoute.Devices.name
    ) {
      composable(
        route = DevicesRoute.Devices.name
      ) {
        val devicesListViewModel = koinViewModel<DevicesListViewModel>()
        val devicesListUiState by devicesListViewModel.devicesUiState.collectAsStateWithLifecycle()
        DevicesListScreen(
          devicesUiState = devicesListUiState,
          devicesViewModel = devicesListViewModel,
          navController = navController,
        )
      }
      composable(
        route = DevicesRoute.FeatureValues.name
      ) {
        val featureValuesViewModel = koinViewModel<FeaturesViewModel>()
        val featureValuesUiState by featureValuesViewModel.featureValuesUiState.collectAsStateWithLifecycle()
        val onlineFeatureValuesViewModel = koinViewModel<OnlineFeatureValuesViewModel>()
        val onlineValuesUiState by onlineFeatureValuesViewModel.onlineValuesUiState.collectAsStateWithLifecycle()
        val sensorFeatureValuesViewModel = koinViewModel<SensorFeatureValuesViewModel>()
        val controllerFeatureValuesViewModel = koinViewModel<ControllerFeatureValuesViewModel>()
        val controllerValuesUiState by controllerFeatureValuesViewModel.getValueUiState.collectAsStateWithLifecycle()
        FeaturesScreen(
          featureValuesUiState = featureValuesUiState,
          featureValuesViewModel = featureValuesViewModel,
          onlineValuesUiState = onlineValuesUiState,
          onlineFeatureValuesViewModel = onlineFeatureValuesViewModel,
          sensorFeatureValuesViewModel = sensorFeatureValuesViewModel,
          controllerValuesUiState = controllerValuesUiState,
          controllerFeatureValuesViewModel = controllerFeatureValuesViewModel,
          navController = navController,
        )
      }
    }
  }
}
