package eu.homeanthill.api.requests

import eu.homeanthill.api.model.ProfileNotificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface NotificationsServices {
  @Headers("Accept: application/json")
  @GET("notifications")
  suspend fun getNotifications(): Response<ProfileNotificationResponse>
}
