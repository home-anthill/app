package eu.homeanthill.api

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
import org.junit.Assert.assertNull
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
  fun `authenticate refreshes token and retries with refreshed authorization header`() {
    every { refreshTokenRepository.repoRefreshToken() } returns TokenResponse(
      token = "new-jwt-token",
      refreshToken = "new-refresh-token"
    )
    every { loginRepository.getJWT() } returns "old-jwt-token"
    val response = responseWithCode(401, "Bearer old-jwt-token")

    val retry = authenticator.authenticate(null, response)

    assertNotNull(retry)
    assertEquals("Bearer new-jwt-token", retry?.header("Authorization"))
    assertNull(retry?.header("Cookie"))
    verify(exactly = 1) { loginRepository.setJWT("new-jwt-token") }
  }

  @Test
  fun `authenticate reuses token refreshed by another request`() {
    every { loginRepository.getJWT() } returns "new-jwt-token"
    val response = responseWithCode(401, "Bearer old-jwt-token")

    val retry = authenticator.authenticate(null, response)

    assertNotNull(retry)
    assertEquals("Bearer new-jwt-token", retry?.header("Authorization"))
    verify(exactly = 0) { refreshTokenRepository.repoRefreshToken() }
    verify(exactly = 0) { loginRepository.setJWT(any()) }
  }

  @Test
  fun `authenticate logs out and stops when refresh fails`() {
    every { loginRepository.getJWT() } returns "old-jwt-token"
    every { refreshTokenRepository.repoRefreshToken() } returns null
    val response = responseWithCode(401, "Bearer old-jwt-token")

    val retry = authenticator.authenticate(null, response)

    assertNull(retry)
    verify(exactly = 1) { loginRepository.logoutAndRedirect() }
    verify(exactly = 0) { loginRepository.setJWT(any()) }
  }

  @Test
  fun `authenticate logs out and stops on repeated unauthorized response`() {
    val response = responseWithCode(
      code = 401,
      authorization = "Bearer refreshed-jwt-token",
      priorResponse = responseWithCode(401, "Bearer old-jwt-token"),
    )

    val retry = authenticator.authenticate(null, response)

    assertNull(retry)
    verify(exactly = 1) { loginRepository.logoutAndRedirect() }
    verify(exactly = 0) { refreshTokenRepository.repoRefreshToken() }
    verify(exactly = 0) { loginRepository.setJWT(any()) }
  }

  private fun responseWithCode(
    code: Int,
    authorization: String? = null,
    priorResponse: Response? = null,
  ): Response {
    val requestBuilder = Request.Builder()
      .url("http://localhost/api/devices")
    authorization?.let { requestBuilder.header("Authorization", it) }

    return Response.Builder()
      .request(requestBuilder.build())
      .protocol(Protocol.HTTP_1_1)
      .code(code)
      .message("Unauthorized")
      .priorResponse(priorResponse?.withoutBody())
      .build()
  }

  private fun Response.withoutBody(): Response {
    return Response.Builder()
      .request(request)
      .protocol(protocol)
      .code(code)
      .message(message)
      .build()
  }
}
