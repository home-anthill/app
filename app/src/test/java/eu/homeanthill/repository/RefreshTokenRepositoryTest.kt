package eu.homeanthill.repository

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Response as OkHttpResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

import eu.homeanthill.api.model.TokenResponse
import eu.homeanthill.api.requests.RefreshTokenServices

class RefreshTokenRepositoryTest {

    private val mockRefreshTokenService = mockk<RefreshTokenServices>()
    private val mockLoginRepository = mockk<LoginRepository>(relaxed = true)
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Before
    fun setUp() {
        refreshTokenRepository = RefreshTokenRepository(mockRefreshTokenService, mockLoginRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // --- repoRefreshToken ---

    @Test
    fun `repoRefreshToken returns token on success without Set-Cookie header`() {
        val mockCall = mockk<Call<TokenResponse>>()
        val retrofitResponse = Response.success(TokenResponse("new-jwt-token"))
        every { mockRefreshTokenService.refreshToken() } returns mockCall
        every { mockCall.execute() } returns retrofitResponse

        val result = refreshTokenRepository.repoRefreshToken()

        assertNotNull(result)
        assertEquals("new-jwt-token", result?.token)
        verify(exactly = 0) { mockLoginRepository.setRefreshToken(any()) }
    }

    @Test
    fun `repoRefreshToken persists rotated refresh token from Set-Cookie header`() {
        val mockCall = mockk<Call<TokenResponse>>()
        val rawOkHttpResponse = OkHttpResponse.Builder()
            .code(200)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost/api/token/refresh").build())
            .header("Set-Cookie", "refresh_token=new-rotated-rt; Path=/; HttpOnly; SameSite=Strict")
            .build()
        val retrofitResponse = Response.success(TokenResponse("new-jwt-token"), rawOkHttpResponse)
        every { mockRefreshTokenService.refreshToken() } returns mockCall
        every { mockCall.execute() } returns retrofitResponse

        val result = refreshTokenRepository.repoRefreshToken()

        assertNotNull(result)
        assertEquals("new-jwt-token", result?.token)
        verify(exactly = 1) { mockLoginRepository.setRefreshToken("new-rotated-rt") }
    }

    @Test
    fun `repoRefreshToken returns null on failure response`() {
        val mockCall = mockk<Call<TokenResponse>>()
        every { mockRefreshTokenService.refreshToken() } returns mockCall
        every { mockCall.execute() } returns Response.error(401, "{}".toResponseBody())

        val result = refreshTokenRepository.repoRefreshToken()

        assertNull(result)
        verify(exactly = 0) { mockLoginRepository.setRefreshToken(any()) }
    }

    @Test
    fun `repoRefreshToken does not persist refresh token when Set-Cookie value is blank`() {
        val mockCall = mockk<Call<TokenResponse>>()
        val rawOkHttpResponse = OkHttpResponse.Builder()
            .code(200)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost/api/token/refresh").build())
            .header("Set-Cookie", "other_cookie=value; Path=/")
            .build()
        val retrofitResponse = Response.success(TokenResponse("new-jwt-token"), rawOkHttpResponse)
        every { mockRefreshTokenService.refreshToken() } returns mockCall
        every { mockCall.execute() } returns retrofitResponse

        val result = refreshTokenRepository.repoRefreshToken()

        assertNotNull(result)
        verify(exactly = 0) { mockLoginRepository.setRefreshToken(any()) }
    }
}
