package eu.homeanthill.repository

import java.io.IOException

import eu.homeanthill.api.model.OnlineDeviceStatus
import eu.homeanthill.api.requests.OnlineServices

class OnlineRepository(private val onlineService: OnlineServices) {
  suspend fun repoGetOnlineStatuses(): List<OnlineDeviceStatus> {
    val result = onlineService.getOnlineStatuses()
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoGetOnlineStatuses")
    }
  }

}
