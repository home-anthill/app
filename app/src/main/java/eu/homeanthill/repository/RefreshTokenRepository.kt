package eu.homeanthill.repository

import eu.homeanthill.api.model.RefreshTokenRequest
import eu.homeanthill.api.model.TokenResponse
import eu.homeanthill.api.requests.RefreshTokenServices

/**
 * Handles mobile token refresh by calling POST /api/oauth/app/refresh synchronously.
 * The server rotates the mobile refresh token in the JSON response; persist it so future
 * refreshes continue to work after the app restarts.
 */
class RefreshTokenRepository(
  private val refreshTokenService: RefreshTokenServices,
  private val loginRepository: LoginRepository,
) {
  fun repoRefreshToken(): TokenResponse? {
    val refreshToken = loginRepository.getRefreshToken() ?: return null
    val response = refreshTokenService.refreshToken(RefreshTokenRequest(refreshToken)).execute()
    if (!response.isSuccessful) return null

    val body = response.body() ?: return null
    loginRepository.setRefreshToken(body.refreshToken)

    return body
  }
}
