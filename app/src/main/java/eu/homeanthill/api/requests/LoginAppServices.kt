package eu.homeanthill.api.requests

import eu.homeanthill.api.model.LoginResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface LoginServices {
    @Headers("Accept: application/json")
    @GET("login")
    suspend fun getLogin(): Response<LoginResponse>
}