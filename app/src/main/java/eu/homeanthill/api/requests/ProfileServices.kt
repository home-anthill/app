package eu.homeanthill.api.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

import eu.homeanthill.api.model.Profile
import eu.homeanthill.api.model.ProfileAPITokenResponse
import retrofit2.http.Path

interface ProfileServices {
    @Headers("Accept: application/json")
    @GET("profile")
    suspend fun getProfile(): Response<Profile>

    @Headers("Accept: application/json")
    @POST("profile/{id}/tokens")
    suspend fun postRegenApiToken(@Path("id") id: String): Response<ProfileAPITokenResponse>
}