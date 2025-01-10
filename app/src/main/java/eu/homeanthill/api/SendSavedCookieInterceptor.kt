package eu.homeanthill.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

private const val cookieKey = "sessionCookie"
private const val cookieName = "mysession" // must match the one define on server-side

class SendSavedCookiesInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        val cookieValue = context
            .getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
            .getString(cookieKey, "")

        if (cookieValue !== null && cookieValue !== "") {
            builder.header("Cookie", "$cookieName=$cookieValue")
        }
        return chain.proceed(builder.build())
    }
}