package eu.homeanthill.repository

import android.util.Log
import java.io.IOException

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetFeatureDeviceValue
import eu.homeanthill.api.model.PutDevice
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.requests.DevicesServices

class DevicesRepository(private val devicesService: DevicesServices) {
  suspend fun repoGetDevices(): List<Device> {
    val result = devicesService.getDevices()
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoGetDevices")
    }
  }

  suspend fun repoAssignDeviceToHomeRoom(id: String, body: PutDevice): GenericMessageResponse {
    val result = devicesService.putAssignDeviceToHomeRoom(id, body)
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoAssignDeviceToHomeRoom")
    }
  }

  suspend fun repoDeleteDevice(id: String): GenericMessageResponse {
    val result = devicesService.deleteDevice(id)
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoDeleteDevice")
    }
  }

  suspend fun repoGetDeviceValues(id: String): List<DeviceFeatureValueResponse> {
    val result = devicesService.getDeviceValues(id)
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoGetDeviceValues")
    }
  }

  suspend fun repoPostSetValues(
    id: String,
    body: List<PostSetFeatureDeviceValue>
  ): GenericMessageResponse {
    val result = devicesService.postSetValues(id, body)
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoPostSetValues")
    }
  }
}