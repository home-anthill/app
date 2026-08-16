package eu.homeanthill.api.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

import eu.homeanthill.api.model.OnlineDeviceStatus

interface OnlineServices {
  @Headers("Accept: application/json")
  @GET("online")
  suspend fun getOnlineStatuses(): Response<List<OnlineDeviceStatus>>
}
