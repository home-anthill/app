package eu.homeanthill.api.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Path

import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.NewHome
import eu.homeanthill.api.model.Room
import eu.homeanthill.api.model.RoomRequest
import eu.homeanthill.api.model.UpdateHome
import retrofit2.http.PUT

interface HomesServices {
  @Headers("Accept: application/json")
  @GET("homes")
  suspend fun getHomes(): Response<List<Home>>

  @Headers("Accept: application/json")
  @POST("homes")
  suspend fun postHome(
    @Body body: NewHome
  ): Response<Home>

  @Headers("Accept: application/json")
  @PUT("homes/{id}")
  suspend fun putHome(
    @Path("id") id: String,
    @Body body: UpdateHome
  ): Response<GenericMessageResponse>

  @Headers("Accept: application/json")
  @DELETE("homes/{id}")
  suspend fun deleteHome(
    @Path("id") id: String
  ): Response<GenericMessageResponse>


  @Headers("Accept: application/json")
  @GET("homes/{id}/rooms")
  suspend fun getRooms(
    @Path("id") id: String
  ): Response<List<Room>>

  @Headers("Accept: application/json")
  @POST("homes/{id}/rooms")
  suspend fun postRoom(
    @Path("id") id: String,
    @Body body: RoomRequest
  ): Response<Room>

  @Headers("Accept: application/json")
  @PUT("homes/{id}/rooms/{rid}")
  suspend fun putRoom(
    @Path("id") id: String,
    @Path("rid") rid: String,
    @Body body: RoomRequest
  ): Response<GenericMessageResponse>

  @Headers("Accept: application/json")
  @DELETE("homes/{id}/rooms/{rid}")
  suspend fun deleteRoom(
    @Path("id") id: String,
    @Path("rid") rid: String,
  ): Response<GenericMessageResponse>

}