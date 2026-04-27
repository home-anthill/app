package eu.homeanthill.api

import eu.homeanthill.cookieName
import eu.homeanthill.api.model.TokenResponse
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.RefreshTokenRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppAuthenticatorTest {
  private val loginRepository = mockk<LoginRepository>(relaxed = true)
  private val refreshTokenRepository = mockk<RefreshTokenRepository>()
  private val authenticator = AppAuthenticator(loginRepository, refreshTokenRepository)

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `authenticate refreshes token and retries with refreshed session cookie`() {
    every { refreshTokenRepository.repoRefreshToken() } returns TokenResponse(
      token = "new-jwt-token",
      refreshToken = "new-refresh-token"
    )
    every { loginRepository.getSessionCookie() } returns "new-session-cookie"
    val response = responseWithCode(401)

    val retry = authenticator.authenticate(null, response)

    assertNotNull(retry)
    assertEquals("Bearer new-jwt-token", retry?.header("Authorization"))
    assertEquals("$cookieName=new-session-cookie", retry?.header("Cookie"))
    verify(exactly = 1) { loginRepository.setJWT("new-jwt-token") }
  }

  private fun responseWithCode(code: Int): Response {
    val request = Request.Builder()
      .url("http://localhost:8082/api/devices")
      .header("Cookie", "$cookieName=old-session-cookie")
      .build()

    return Response.Builder()
      .request(request)
      .protocol(Protocol.HTTP_1_1)
      .code(code)
      .message("Unauthorized")
      .build()
  }
}
