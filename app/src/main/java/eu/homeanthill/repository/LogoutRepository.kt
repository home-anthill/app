package eu.homeanthill.repository

import eu.homeanthill.api.model.RefreshTokenRequest
import eu.homeanthill.api.requests.LogoutServices

class LogoutRepository(
  private val logoutService: LogoutServices,
  private val loginRepository: LoginRepository,
) {
  suspend fun logoutWithServerAndRedirect() {
    try {
      loginRepository.getRefreshToken()?.let { refreshToken ->
        logoutService.logout(RefreshTokenRequest(refreshToken))
      }
    } catch (_: Exception) {
      // Local logout must still happen if the server session is already unavailable.
    }
    loginRepository.logoutAndRedirect()
  }
}
