package eu.homeanthill.repository

import java.io.IOException

import eu.homeanthill.api.model.RefreshTokenRequest
import eu.homeanthill.api.requests.LogoutServices

class LogoutRepository(
  private val logoutService: LogoutServices,
  private val loginRepository: LoginRepository,
) {
  suspend fun logoutWithServerAndRedirect() {
    val refreshToken = loginRepository.getRefreshToken()
      ?: throw IOException("Refresh token not found")
    val response = logoutService.logout(RefreshTokenRequest(refreshToken))
    if (!response.isSuccessful) {
      throw IOException("Cannot logout")
    }
    loginRepository.logoutAndRedirect()
  }
}
