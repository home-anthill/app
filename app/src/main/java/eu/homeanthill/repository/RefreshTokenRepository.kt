package eu.homeanthill.repository

import eu.homeanthill.api.model.TokenResponse
import eu.homeanthill.api.requests.RefreshTokenServices
import eu.homeanthill.refreshTokenCookieName

/**
 * Handles token refresh by calling POST /api/token/refresh synchronously.
 * After a successful refresh the server rotates the refresh token via Set-Cookie; this
 * repository reads that header and persists the new value to SharedPreferences so future
 * refreshes continue to work after the app restarts.
 */
class RefreshTokenRepository(
  private val refreshTokenService: RefreshTokenServices,
  private val loginRepository: LoginRepository,
) {
  fun repoRefreshToken(): TokenResponse? {
    val response = refreshTokenService.refreshToken().execute()
    if (!response.isSuccessful) return null

    // Persist the rotated refresh token returned by the server in the Set-Cookie header.
    val newRefreshToken = response.headers().values("Set-Cookie")
      .firstOrNull { it.startsWith("$refreshTokenCookieName=") }
      ?.substringAfter("$refreshTokenCookieName=")
      ?.substringBefore(";")
    if (!newRefreshToken.isNullOrEmpty()) {
      loginRepository.setRefreshToken(newRefreshToken)
    }

    return response.body()
  }
}
