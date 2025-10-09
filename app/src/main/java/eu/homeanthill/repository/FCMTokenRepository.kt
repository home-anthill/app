package eu.homeanthill.repository

import java.io.IOException

import eu.homeanthill.api.model.FCMTokenResponse

import eu.homeanthill.api.requests.FCMTokenServices

class FCMTokenRepository(private val fcmTokenService: FCMTokenServices) {
  suspend fun repoPostFCMToken(fcmTokenRequest: Map<String, String>): FCMTokenResponse {
    val result = fcmTokenService.postFCMToken(fcmTokenRequest)
    if (result.isSuccessful) {
      return result.body()!!
    } else {
      throw IOException("Error repoPostFCMToken")
    }
  }
}