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

import eu.homeanthill.api.model.OnlineDeviceStatus
import eu.homeanthill.api.model.OnlineStatus
import eu.homeanthill.api.requests.OnlineServices

class OnlineRepositoryTest {

    private val mockOnlineService = mockk<OnlineServices>()
    private lateinit var onlineRepository: OnlineRepository

    private val testOnlineStatus = OnlineDeviceStatus(
        deviceId = "dev1",
        featureUuid = "feature1",
        status = OnlineStatus.ONLINE,
        createdAt = "2024-01-01T10:00:00Z",
        modifiedAt = "2024-01-01T10:00:00Z",
        currentTime = "2024-01-01T10:01:00Z",
    )

    @Before
    fun setUp() {
        onlineRepository = OnlineRepository(mockOnlineService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // --- repoGetOnlineStatuses ---

    @Test
    fun `repoGetOnlineStatuses returns profile statuses on success`() = runBlocking {
        coEvery { mockOnlineService.getOnlineStatuses() } returns Response.success(listOf(testOnlineStatus))

        val result = onlineRepository.repoGetOnlineStatuses()

        assertEquals(listOf(testOnlineStatus), result)
        coVerify(exactly = 1) { mockOnlineService.getOnlineStatuses() }
    }

    @Test
    fun `repoGetOnlineStatuses throws IOException on error response`() = runBlocking {
        coEvery { mockOnlineService.getOnlineStatuses() } returns Response.error(500, "{}".toResponseBody())

        try {
            onlineRepository.repoGetOnlineStatuses()
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoGetOnlineStatuses", e.message)
        }
    }
}
