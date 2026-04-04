package eu.homeanthill.repository

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

import eu.homeanthill.api.model.FCMTokenResponse
import eu.homeanthill.api.requests.FCMTokenServices

class FCMTokenRepositoryTest {

    private val mockFCMTokenService = mockk<FCMTokenServices>()
    private lateinit var fcmTokenRepository: FCMTokenRepository

    @Before
    fun setUp() {
        fcmTokenRepository = FCMTokenRepository(mockFCMTokenService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // --- repoPostFCMToken ---

    @Test
    fun `repoPostFCMToken returns response on success`() = runBlocking {
        val request = mapOf("fcmToken" to "test-fcm-token")
        val expectedResponse = FCMTokenResponse(message = "FCM token registered")
        coEvery { mockFCMTokenService.postFCMToken(request) } returns Response.success(expectedResponse)

        val result = fcmTokenRepository.repoPostFCMToken(request)

        assertEquals(expectedResponse, result)
        coVerify(exactly = 1) { mockFCMTokenService.postFCMToken(request) }
    }

    @Test
    fun `repoPostFCMToken throws IOException on error response`() = runBlocking {
        val request = mapOf("fcmToken" to "test-fcm-token")
        coEvery { mockFCMTokenService.postFCMToken(request) } returns Response.error(500, "{}".toResponseBody())

        try {
            fcmTokenRepository.repoPostFCMToken(request)
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoPostFCMToken", e.message)
        }
    }
}
