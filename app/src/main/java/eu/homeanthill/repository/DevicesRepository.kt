package eu.homeanthill.repository

import java.io.IOException

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetDeviceValue
import eu.homeanthill.api.model.PutDevice
import eu.homeanthill.api.model.SensorValue
import eu.homeanthill.api.model.ControllerValue
import eu.homeanthill.api.requests.DevicesServices

class DevicesRepository(private val devicesService: DevicesServices) {
    suspend fun repoGetDevices(): List<Device> {
        val result = devicesService.getDevices()
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoGetDevices"))
        }
    }

    suspend fun repoAssignDeviceToHomeRoom(id: String, body: PutDevice): GenericMessageResponse {
        val result = devicesService.putAssignDeviceToHomeRoom(id, body)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoAssignDeviceToHomeRoom"))
        }
    }

    suspend fun repoDeleteDevice(id: String): GenericMessageResponse {
        val result = devicesService.deleteDevice(id)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoDeleteDevice"))
        }
    }

    suspend fun repoGetSensorValues(id: String): List<SensorValue> {
        val result = devicesService.getSensorValues(id)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoGetSensorValues"))
        }
    }

    suspend fun repoGetControllerValues(id: String): ControllerValue {
        val result = devicesService.getControllerValues(id)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoGetControllerValues"))
        }
    }

    suspend fun repoPostSetValues(id: String, body: PostSetDeviceValue): GenericMessageResponse {
        val result = devicesService.postSetValues(id, body)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoPostSetValues"))
        }
    }
}