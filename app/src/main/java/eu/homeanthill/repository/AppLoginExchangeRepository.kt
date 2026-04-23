package eu.homeanthill.repository

import eu.homeanthill.api.model.AppCodeExchangeRequest
import eu.homeanthill.api.model.AppCodeExchangeResponse
import eu.homeanthill.api.requests.AppLoginExchangeServices
import eu.homeanthill.cookieName

data class AppLoginExchangeResult(
  val tokenResponse: AppCodeExchangeResponse,
  val sessionCookie: String?,
)

class AppLoginExchangeRepository(
  private val service: AppLoginExchangeServices,
) {
  suspend fun exchangeCode(code: String, codeVerifier: String): AppLoginExchangeResult? {
    val response = service.exchangeCode(AppCodeExchangeRequest(code, codeVerifier))
    if (!response.isSuccessful) return null

    val body = response.body() ?: return null
    val sessionCookie = response.headers().values("Set-Cookie")
      .firstOrNull { it.startsWith("$cookieName=") }
      ?.substringAfter("$cookieName=")
      ?.substringBefore(";")

    return AppLoginExchangeResult(
      tokenResponse = body,
      sessionCookie = sessionCookie,
    )
  }
}
