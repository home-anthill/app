package eu.homeanthill.api.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

import eu.homeanthill.api.model.OnlineDeviceStatus
import eu.homeanthill.api.model.OnlineValue

interface OnlineServices {
  @Headers("Accept: application/json")
  @GET("online")
  suspend fun getOnlineStatuses(): Response<List<OnlineDeviceStatus>>

  @Headers("Accept: application/json")
  @GET("online/{id}")
  suspend fun getOnlineValues(
    @Path("id") id: String
  ): Response<OnlineValue>
}
