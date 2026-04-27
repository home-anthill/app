package eu.homeanthill.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.RefreshTokenRepository
import eu.homeanthill.cookieName

class AppAuthenticator(
  private val loginRepository: LoginRepository,
  private val refreshTokenRepository: RefreshTokenRepository,
) : Authenticator {

  override fun authenticate(route: Route?, response: Response): Request? {
    if (!response.isUnauthorized()) return null

    // Avoid infinite retry loops: if the prior response was also a 401 the refresh token has
    // already been tried (or the new JWT was rejected) → logout immediately.
    // Using priorResponse?.code == 401 rather than priorResponse != null so that a 401
    // following an HTTP redirect (which also sets priorResponse) still attempts a refresh.
    if (response.priorResponse?.code == 401) {
      loginRepository.logoutAndRedirect()
      return null
    }

    val tokenResponse = refreshTokenRepository.repoRefreshToken()
    if (tokenResponse == null) {
      loginRepository.logoutAndRedirect()
      return null
    }

    loginRepository.setJWT(tokenResponse.token)
    val retryRequest = response.request.newBuilder()
      .header("Authorization", "Bearer ${tokenResponse.token}")

    loginRepository.getSessionCookie()?.let { sessionCookie ->
      retryRequest.header("Cookie", "$cookieName=$sessionCookie")
    }

    return retryRequest.build()
  }

  private fun Response.isUnauthorized() = this.code == 401
}
