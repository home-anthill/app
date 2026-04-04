package eu.homeanthill.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

import eu.homeanthill.cookieKey
import eu.homeanthill.cookieName
import eu.homeanthill.loginTimestampKey
import eu.homeanthill.securePrefs

class SendSavedCookiesInterceptor(private val context: Context) : Interceptor {
  private companion object {
    private const val SESSION_EXPIRY_SECONDS = 28 * 24 * 60 * 60
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val builder = request.newBuilder()
    val prefs = context.securePrefs()
    val cookieValue = prefs.getString(cookieKey, "")
    val loginTimestamp = prefs.getLong(loginTimestampKey, 0)
    val unixTime = System.currentTimeMillis() / 1000L
    val isExpired = loginTimestamp < unixTime - SESSION_EXPIRY_SECONDS
    if (!cookieValue.isNullOrEmpty() && loginTimestamp != 0L && !isExpired) {
      builder.header("Cookie", "$cookieName=$cookieValue")
    }
    return chain.proceed(builder.build())
  }
}