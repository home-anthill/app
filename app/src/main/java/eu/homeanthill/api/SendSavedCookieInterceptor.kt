package eu.homeanthill.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

import eu.homeanthill.cookieKey
import eu.homeanthill.cookieName
import eu.homeanthill.loginTimestampKey
import eu.homeanthill.mainKey

class SendSavedCookiesInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        val cookieValue = context
            .getSharedPreferences(mainKey, Context.MODE_PRIVATE)
            .getString(cookieKey, "")
        val loginTimestamp = context
            .getSharedPreferences(mainKey, Context.MODE_PRIVATE)
            .getLong(loginTimestampKey, 0)
        val unixTime = System.currentTimeMillis() / 1000L
        val isExpired = loginTimestamp < unixTime - (28*24*60*60)
        if (cookieValue !== null && cookieValue !== "" && loginTimestamp != 0L && !isExpired) {
            builder.header("Cookie", "$cookieName=$cookieValue")
        }
        return chain.proceed(builder.build())
    }
}