package eu.homeanthill.repository

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.ResponseBody.Companion.toResponseBody
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
    fun `repoRefreshToken posts stored refresh token and persists rotated token on success`() {
        val mockCall = mockk<Call<TokenResponse>>()
        val retrofitResponse = Response.success(TokenResponse("new-jwt-token", "new-refresh-token"))
        every { mockLoginRepository.getRefreshToken() } returns "stored-refresh-token"
        every { mockRefreshTokenService.refreshToken(match { it.refreshToken == "stored-refresh-token" }) } returns mockCall
        every { mockCall.execute() } returns retrofitResponse

        val result = refreshTokenRepository.repoRefreshToken()

        assertNotNull(result)
        assertEquals("new-jwt-token", result?.token)
        assertEquals("new-refresh-token", result?.refreshToken)
        verify(exactly = 1) { mockLoginRepository.setRefreshToken("new-refresh-token") }
    }

    @Test
    fun `repoRefreshToken returns null on failure response`() {
        val mockCall = mockk<Call<TokenResponse>>()
        every { mockLoginRepository.getRefreshToken() } returns "stored-refresh-token"
        every { mockRefreshTokenService.refreshToken(any()) } returns mockCall
        every { mockCall.execute() } returns Response.error(401, "{}".toResponseBody())

        val result = refreshTokenRepository.repoRefreshToken()

        assertNull(result)
        verify(exactly = 0) { mockLoginRepository.setRefreshToken(any()) }
    }

    @Test
    fun `repoRefreshToken returns null without stored refresh token`() {
        every { mockLoginRepository.getRefreshToken() } returns null

        val result = refreshTokenRepository.repoRefreshToken()

        assertNull(result)
        verify(exactly = 0) { mockRefreshTokenService.refreshToken(any()) }
        verify(exactly = 0) { mockLoginRepository.setRefreshToken(any()) }
    }
}
