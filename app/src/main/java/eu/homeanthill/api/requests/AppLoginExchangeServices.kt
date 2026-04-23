package eu.homeanthill.api.requests

import eu.homeanthill.api.model.AppCodeExchangeRequest
import eu.homeanthill.api.model.AppCodeExchangeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AppLoginExchangeServices {
  @Headers("Accept: application/json")
  @POST("oauth/app/exchange-code")
  suspend fun exchangeCode(
    @Body request: AppCodeExchangeRequest,
  ): Response<AppCodeExchangeResponse>
}
