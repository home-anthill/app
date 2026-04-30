package eu.homeanthill.repository

import eu.homeanthill.api.model.AppCodeExchangeRequest
import eu.homeanthill.api.model.AppCodeExchangeResponse
import eu.homeanthill.api.requests.AppLoginExchangeServices

class AppLoginExchangeRepository(
  private val service: AppLoginExchangeServices,
) {
  suspend fun exchangeCode(code: String, codeVerifier: String): AppCodeExchangeResponse? {
    val response = service.exchangeCode(AppCodeExchangeRequest(code, codeVerifier))
    if (!response.isSuccessful) return null

    return response.body()
  }
}
