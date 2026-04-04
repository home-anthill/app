package eu.homeanthill.api.requests

import eu.homeanthill.api.model.TokenResponse
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.POST

interface RefreshTokenServices {
  // Uses Call<T> (not suspend) so AppAuthenticator can call execute() synchronously.
  @Headers("Accept: application/json")
  @POST("token/refresh")
  fun refreshToken(): Call<TokenResponse>
}
