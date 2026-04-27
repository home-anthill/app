package eu.homeanthill.repository

import eu.homeanthill.api.model.RefreshTokenRequest
import eu.homeanthill.api.model.TokenResponse
import eu.homeanthill.api.requests.RefreshTokenServices
import eu.homeanthill.cookieName

/**
 * Handles mobile token refresh by calling POST /api/oauth/app/refresh synchronously.
 * The server rotates the mobile refresh token in the JSON response; persist it so future
 * refreshes continue to work after the app restarts. The mobile refresh endpoint also
 * renews the server session cookie; persist it so the retried authenticated request does
 * not fail the session/JWT identity check after a short access-token TTL.
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
    response.headers().values("Set-Cookie")
      .firstOrNull { it.startsWith("$cookieName=") }
      ?.substringAfter("$cookieName=")
      ?.substringBefore(";")
      ?.let { loginRepository.setSessionCookie(it) }

    return body
  }
}
