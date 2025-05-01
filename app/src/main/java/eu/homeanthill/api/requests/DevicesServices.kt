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
import eu.homeanthill.api.model.PostSetDeviceValue
import eu.homeanthill.api.model.PutDevice
import eu.homeanthill.api.model.SensorValue
import eu.homeanthill.api.model.ControllerValue

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
    suspend fun getSensorValues(
        @Path("id") id: String
    ): Response<List<SensorValue>>

    // FIXME this api should be removed to reuse the getSensorValues after migrating devices to a list of features
    @Headers("Accept: application/json")
    @GET("devices/{id}/values")
    suspend fun getControllerValues(
        @Path("id") id: String
    ): Response<ControllerValue>

    @Headers("Accept: application/json")
    @POST("devices/{id}/values")
    suspend fun postSetValues(
        @Path("id") id: String,
        @Body body: PostSetDeviceValue
    ): Response<GenericMessageResponse>
}