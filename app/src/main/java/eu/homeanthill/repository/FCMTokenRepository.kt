package eu.homeanthill.repository

import eu.homeanthill.api.model.FCMTokenResponse
import java.io.IOException

import eu.homeanthill.api.requests.FCMTokenServices

class FCMTokenRepository(private val fcmTokenService: FCMTokenServices) {
    suspend fun repoPostFCMToken(fcmTokenRequest: Map<String, String>): FCMTokenResponse {
        val result = fcmTokenService.postFCMToken(fcmTokenRequest)
        if (result.isSuccessful) {
            return result.body()!!
        } else {
            throw Exception(IOException("Error repoPostFCMToken"))
        }
    }
}