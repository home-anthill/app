package eu.homeanthill.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

import eu.homeanthill.refreshTokenCookieName
import eu.homeanthill.refreshTokenKey
import eu.homeanthill.securePrefs

/**
 * Adds the refresh token as a Cookie header, but only on requests to the token refresh endpoint.
 * All other requests are passed through unmodified so the refresh token is never leaked.
 */
class SendRefreshTokenCookieInterceptor(private val context: Context) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val isRefreshEndpoint = request.url.encodedPath.endsWith("/token/refresh")
    if (!isRefreshEndpoint) {
      return chain.proceed(request)
    }
    val refreshToken = context.securePrefs().getString(refreshTokenKey, null)
    if (refreshToken.isNullOrEmpty()) {
      return chain.proceed(request)
    }
    return chain.proceed(
      request.newBuilder()
        .header("Cookie", "$refreshTokenCookieName=$refreshToken")
        .build()
    )
  }
}
