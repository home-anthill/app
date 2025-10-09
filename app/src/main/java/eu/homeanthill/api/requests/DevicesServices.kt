package eu.homeanthill.api.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PutDevice
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.PostSetFeatureDeviceValue

interface DevicesServices {
  @Headers("Accept: application/json")
  @GET("devices")
  suspend fun getDevices(): Response<List<Device>>

  @Headers("Accept: application/json")
  @PUT("devices/{id}")
  suspend fun putAssignDeviceToHomeRoom(
    @Path("id") id: String,
    @Body body: PutDevice,
  ): Response<GenericMessageResponse>

  @Headers("Accept: application/json")
  @DELETE("devices/{id}")
  suspend fun deleteDevice(
    @Path("id") id: String
  ): Response<GenericMessageResponse>

  @Headers("Accept: application/json")
  @GET("devices/{id}/values")
  suspend fun getDeviceValues(
    @Path("id") id: String
  ): Response<List<DeviceFeatureValueResponse>>

  @Headers("Accept: application/json")
  @POST("devices/{id}/values")
  suspend fun postSetValues(
    @Path("id") id: String,
    @Body body: List<PostSetFeatureDeviceValue>
  ): Response<GenericMessageResponse>
}