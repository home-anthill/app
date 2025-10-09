package eu.homeanthill.ui.screens.devices.deviceslist

import java.io.IOException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.HomeWithDevices
import eu.homeanthill.api.model.MyDevicesList
import eu.homeanthill.api.model.Room
import eu.homeanthill.api.model.RoomSplitDevices
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.HomesRepository

class DevicesListViewModel(
  private val devicesRepository: DevicesRepository,
  private val homesRepository: HomesRepository
) : ViewModel() {
  companion object {
    private const val TAG = "DevicesListViewModel"
  }

  sealed class DevicesUiState {
    data class Idle(val deviceList: MyDevicesList?) : DevicesUiState()
    data object Loading : DevicesUiState()
    data class Error(val errorMessage: String) : DevicesUiState()
  }

  private val _deviceUiState = MutableStateFlow<DevicesUiState>(DevicesUiState.Idle(null))
  val devicesUiState: StateFlow<DevicesUiState> = _deviceUiState

  init {
    init()
  }

  private fun getUnassignedDevices(homes: List<Home>, devices: List<Device>): List<Device> {
    val rooms: List<Room> = homes
      .filter { !it.rooms.isNullOrEmpty() }
      .map { it.rooms!! }
      .flatten()
    val devicesIds: List<String> = rooms
      .filter { !it.devices.isNullOrEmpty() }
      .map { it.devices!! }
      .flatten();
    return devices
      .filter { !devicesIds.contains(it.id) }
  }

  private fun getControllers(devices: List<Device>): List<Device> {
    // if a device has a controller feature, it's a controller and it cannot have any sensor feature!
    return devices.filter { device ->
      val controller = device.features.find { feature -> feature.type == "controller" }
      return@filter controller != null
    }
  }

  private fun getSensors(devices: List<Device>): List<Device> {
    // is a device has only sensor feature, it's a sensor
    return devices.filter { device ->
      val controller = device.features.find { feature -> feature.type == "controller" }
      return@filter controller == null
    }
  }

  private fun getHomeDevices(
    homes: List<Home>,
    devices: List<Device>
  ): MutableList<HomeWithDevices> {
    val homeDevices: MutableList<HomeWithDevices> = mutableListOf()
    homes.forEach { home ->
      val roomsObjs: MutableList<RoomSplitDevices> = mutableListOf()
      if (home.rooms != null) {
        home.rooms?.forEach { room ->
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
          val homeObj = HomeWithDevices(home = home, rooms = listOf())
          homeObj.rooms = roomsObjs;
          homeDevices.add(homeObj);
        }
      }
    }
    return homeDevices
  }


  private fun init() {
    viewModelScope.launch {
      _deviceUiState.emit(DevicesUiState.Loading)
      delay(500)

      try {
        val devices: List<Device> = devicesRepository.repoGetDevices()
        val homes: List<Home> = homesRepository.repoGetHomes()
        val result = MyDevicesList(
          // 1) add unassigned devices to `result.unassignedDevices`
          unassignedDevices = getUnassignedDevices(homes, devices),
          // 2) add assigned devices with homes and rooms to `result.homeDevices`
          homeDevices = getHomeDevices(homes, devices),
        )
        Log.d(TAG, "init - ############################# result = $result")

        _deviceUiState.emit(DevicesUiState.Idle(result))
      } catch (err: IOException) {
        _deviceUiState.emit(DevicesUiState.Error(err.message.toString()))
      }
    }
  }
}