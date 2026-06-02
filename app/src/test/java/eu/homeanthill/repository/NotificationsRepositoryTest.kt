package eu.homeanthill.repository

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.ProfileNotification
import eu.homeanthill.api.model.ProfileNotificationResponse
import eu.homeanthill.api.requests.NotificationsServices
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

class NotificationsRepositoryTest {
    private val mockNotificationsService = mockk<NotificationsServices>()
    private lateinit var notificationsRepository: NotificationsRepository

    private val testDevice = Device(
        id = "dev1",
        uuid = "uuid-1",
        mac = "aa:bb:cc:dd:ee:ff",
        name = "Kitchen sensor",
        manufacturer = "TestCo",
        model = "Model-X",
        features = listOf(
            Feature(uuid = "feat-1", type = "sensor", name = "online", enable = true, order = 1, unit = "-")
        ),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val oldNotification = ProfileNotification(
        id = "old",
        sentAt = 1704067200000L,
        title = "home anthill",
        body = "Device is offline",
        deviceCount = 1,
        devices = listOf(testDevice),
        provider = "fcm",
        providerMessageId = "message-old",
    )

    private val newNotification = oldNotification.copy(
        id = "new",
        sentAt = 1704067300000L,
        providerMessageId = "message-new",
    )

    @Before
    fun setUp() {
        notificationsRepository = NotificationsRepository(mockNotificationsService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `repoGetNotifications returns notifications ordered by newest first`() = runBlocking {
        coEvery { mockNotificationsService.getNotifications() } returns Response.success(
            ProfileNotificationResponse(notifications = listOf(oldNotification, newNotification))
        )

        val result = notificationsRepository.repoGetNotifications()

        assertEquals(listOf(newNotification, oldNotification), result)
        coVerify(exactly = 1) { mockNotificationsService.getNotifications() }
    }

    @Test
    fun `repoGetNotifications throws IOException on error response`() = runBlocking {
        coEvery { mockNotificationsService.getNotifications() } returns Response.error(500, "{}".toResponseBody())

        try {
            notificationsRepository.repoGetNotifications()
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoGetNotifications", e.message)
        }
    }
}
