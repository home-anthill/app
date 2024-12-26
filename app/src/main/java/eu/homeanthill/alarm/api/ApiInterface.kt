package eu.homeanthill.alarm.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterface {
    @POST("fcmtoken")
    fun postFCMToken(@Body fcmTokenRequest: Map<String, String>): Call<FCMTokenResponse>
}