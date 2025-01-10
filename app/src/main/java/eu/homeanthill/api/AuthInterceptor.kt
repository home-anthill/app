package eu.homeanthill.api

import okhttp3.Interceptor
import okhttp3.Response

import eu.homeanthill.repository.LoginRepository

/**
 * Attach authorization header with access token to network requests that don't yet have this header.
 */
class AuthInterceptor(
    private val loginRepository: LoginRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        if (request.header("Authorization") == null) {
            loginRepository.getJWT()?.let { token ->
                builder.header(
                    "Authorization",
                    "Bearer $token"
                )
            }
        }
        return chain.proceed(builder.build())
    }
}