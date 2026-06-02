package eu.homeanthill.repository

import eu.homeanthill.api.model.ProfileNotification
import eu.homeanthill.api.requests.NotificationsServices
import java.io.IOException

class NotificationsRepository(private val notificationsService: NotificationsServices) {
  suspend fun repoGetNotifications(): List<ProfileNotification> {
    val result = notificationsService.getNotifications()
    if (result.isSuccessful) {
      return result.body()!!.notifications.sortedByDescending { it.sentAt }
    } else {
      throw IOException("Error repoGetNotifications")
    }
  }
}
