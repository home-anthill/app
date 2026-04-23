package eu.homeanthill.api.requests

import eu.homeanthill.api.model.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LogoutServices {
  @Headers("Accept: application/json")
  @POST("oauth/app/logout")
  suspend fun logout(
    @Body request: RefreshTokenRequest,
  ): Response<Unit>
}
