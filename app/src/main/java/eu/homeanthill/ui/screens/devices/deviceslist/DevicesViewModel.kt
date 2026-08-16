package eu.homeanthill.ui.screens.devices.deviceslist

import java.io.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.HomeWithDevices
import eu.homeanthill.api.model.MyDevicesList
import eu.homeanthill.api.model.OnlineStatus
import eu.homeanthill.api.model.Room
import eu.homeanthill.api.model.RoomSplitDevices
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.HomesRepository
import eu.homeanthill.repository.OnlineRepository

class DevicesListViewModel(
  private val devicesRepository: DevicesRepository,
  private val homesRepository: HomesRepository,
  private val onlineRepository: OnlineRepository? = null,
) : ViewModel() {
  data class DeviceOnlineStatus(val status: OnlineStatus)

  sealed class DevicesUiState {
    data class Idle(
      val deviceList: MyDevicesList?,
      val onlineStatuses: Map<String, DeviceOnlineStatus> = emptyMap(),
    ) : DevicesUiState()
    data object Loading : DevicesUiState()
    data class Error(val errorMessage: String) : DevicesUiState()
  }

  private val _deviceUiState = MutableStateFlow<DevicesUiState>(DevicesUiState.Idle(null))
  val devicesUiState: StateFlow<DevicesUiState> = _deviceUiState
  private var loadJob: Job? = null

  init {
    loadDevices()
  }

  private fun getUnassignedDevices(homes: List<Home>, devices: List<Device>): List<Device> {
    val rooms: List<Room> = homes
      .filter { !it.rooms.isNullOrEmpty() }
      .map { it.rooms!! }
      .flatten()
    val assignedIds: Set<String> = rooms
      .filter { !it.devices.isNullOrEmpty() }
      .flatMap { it.devices!! }
      .toSet()
    val unassignedDevices = devices.filter { it.id !in assignedIds }
    return getSensors(unassignedDevices) + getControllers(unassignedDevices)
  }

  private fun getControllers(devices: List<Device>): List<Device> {
    // if a device has a controller feature, it's a controller and it cannot have any sensor feature!
    return devices.filter { device ->
      device.features.any { feature -> feature.enable && feature.type == "controller" }
    }
  }

  private fun getSensors(devices: List<Device>): List<Device> {
    // if a device has only sensor features, it's a sensor
    return devices.filter { device ->
      device.features.none { feature -> feature.enable && feature.type == "controller" }
    }
  }

  private fun hasOnlineFeature(device: Device): Boolean {
    return device.features.any { feature ->
      feature.enable &&
        feature.type.lowercase() == "sensor" &&
        feature.name.lowercase() == "online"
    }
  }

  private suspend fun getOnlineStatuses(devices: List<Device>): Map<String, DeviceOnlineStatus> {
    val repository = onlineRepository ?: return emptyMap()
    val enabledOnlineDeviceIds = devices
      .filter { hasOnlineFeature(it) }
      .mapTo(mutableSetOf()) { it.id }
    if (enabledOnlineDeviceIds.isEmpty()) return emptyMap()

    return try {
      repository.repoGetOnlineStatuses()
        .asSequence()
        .filter { it.deviceId in enabledOnlineDeviceIds }
        .associate { onlineStatus ->
          onlineStatus.deviceId to DeviceOnlineStatus(onlineStatus.status)
        }
    } catch (err: CancellationException) {
      throw err
    } catch (_: Exception) {
      emptyMap()
    }
  }

  private fun getHomeDevices(
    homes: List<Home>,
    devices: List<Device>
  ): List<HomeWithDevices> {
    val homeDevices: MutableList<HomeWithDevices> = mutableListOf()
    homes.forEach { home ->
      val roomsObjs: MutableList<RoomSplitDevices> = mutableListOf()
      if (home.rooms != null) {
        home.rooms.forEach { room ->
          // if this room has devices, otherwise skip it
          if (!room.devices.isNullOrEmpty()) {
            val roomDevices: List<Device> = room.devices
              .distinct()
              .mapNotNull { deviceId -> devices.find { device -> device.id == deviceId } }
            // split those devices into 2 different arrays:
            // - controllers (devices able to receive commands)
            // - sensors (read-only devices)
            val roomObj = RoomSplitDevices(
              room = room,
              controllerDevices = getControllers(roomDevices),
              sensorDevices = getSensors(roomDevices),
            )
            // add this room to the list of rooms of the current home
            roomsObjs.add(roomObj)
          }
        }
        // if this home has rooms (added in the loop above), otherwise skip it
        if (roomsObjs.isNotEmpty()) {
          homeDevices.add(HomeWithDevices(home = home, rooms = roomsObjs))
        }
      }
    }
    return homeDevices
  }


  fun loadDevices() {
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
      _deviceUiState.emit(DevicesUiState.Loading)

      try {
        val (devices, homes) = coroutineScope {
          val devicesResult = async { devicesRepository.repoGetDevices() }
          val homesResult = async { homesRepository.repoGetHomes() }
          devicesResult.await() to homesResult.await()
        }
        val result = MyDevicesList(
          // 1) add unassigned devices to `result.unassignedDevices`
          unassignedDevices = getUnassignedDevices(homes, devices),
          // 2) add assigned devices with homes and rooms to `result.homeDevices`
          homeDevices = getHomeDevices(homes, devices),
        )

        // Render the list as soon as its core data is ready. Online statuses are secondary
        // information and should not keep the entire screen behind a loading indicator.
        _deviceUiState.emit(DevicesUiState.Idle(result))
        val onlineStatuses = getOnlineStatuses(devices)
        _deviceUiState.emit(DevicesUiState.Idle(result, onlineStatuses))
      } catch (err: IOException) {
        _deviceUiState.emit(DevicesUiState.Error(err.message.toString()))
      }
    }
  }
}
