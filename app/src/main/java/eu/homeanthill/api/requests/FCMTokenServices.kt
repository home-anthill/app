package eu.homeanthill.api.requests

import eu.homeanthill.api.model.FCMTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMTokenServices {
    @Headers("Accept: application/json")
    @POST("fcmtoken")
    suspend fun postFCMToken(@Body fcmTokenRequest: Map<String, String>): Response<FCMTokenResponse>
}