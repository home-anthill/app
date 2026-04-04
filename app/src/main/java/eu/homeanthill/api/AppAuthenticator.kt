package eu.homeanthill.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.RefreshTokenRepository

class AppAuthenticator(
  private val loginRepository: LoginRepository,
  private val refreshTokenRepository: RefreshTokenRepository,
) : Authenticator {

  override fun authenticate(route: Route?, response: Response): Request? {
    if (!response.isUnauthorized()) return null

    // Avoid infinite retry loops: if this response is itself a retry (priorResponse exists),
    // the refresh already failed or the new token was rejected → logout.
    if (response.priorResponse != null) {
      loginRepository.logoutAndRedirect()
      return null
    }

    val tokenResponse = refreshTokenRepository.repoRefreshToken()
    if (tokenResponse == null) {
      loginRepository.logoutAndRedirect()
      return null
    }

    loginRepository.setJWT(tokenResponse.token)
    return response.request.newBuilder()
      .header("Authorization", "Bearer ${tokenResponse.token}")
      .build()
  }

  private fun Response.isUnauthorized() = this.code == 401
}
