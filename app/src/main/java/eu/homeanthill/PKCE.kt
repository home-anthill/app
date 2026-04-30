package eu.homeanthill

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PKCE {
  private const val RFC7636_MAX_BASE64URL_RANDOM_BYTES = 96
  private val secureRandom = SecureRandom()

  private fun generateMaxLengthBase64UrlSecret(): String {
    val bytes = ByteArray(RFC7636_MAX_BASE64URL_RANDOM_BYTES)
    secureRandom.nextBytes(bytes)
    return Base64.encodeToString(
      bytes,
      Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
    )
  }

  fun generateCodeVerifier(): String = generateMaxLengthBase64UrlSecret()

  fun generateState(): String = generateMaxLengthBase64UrlSecret()

  fun buildCodeChallenge(codeVerifier: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
    return Base64.encodeToString(
      digest,
      Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
    )
  }
}
