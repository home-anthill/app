package eu.homeanthill.ui.screens.notifications

import eu.homeanthill.api.model.ProfileNotification
import eu.homeanthill.repository.NotificationsRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {
    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockNotificationsRepository = mockk<NotificationsRepository>()

    private val notification = ProfileNotification(
        id = "notification-1",
        sentAt = 1704067200000L,
        title = "home anthill",
        body = "Device is offline",
        deviceCount = 1,
        devices = emptyList(),
        provider = "fcm",
        providerMessageId = "message-1",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `init emits Idle with notifications on success`() = runTest(testScheduler) {
        coEvery { mockNotificationsRepository.repoGetNotifications() } returns listOf(notification)

        val vm = NotificationsViewModel(mockNotificationsRepository)
        advanceUntilIdle()

        val state = vm.notificationsUiState.value
        assertTrue(state is NotificationsViewModel.NotificationsUiState.Idle)
        assertEquals(
            listOf(notification),
            (state as NotificationsViewModel.NotificationsUiState.Idle).notifications,
        )
    }

    @Test
    fun `init emits Error when repository throws IOException`() = runTest(testScheduler) {
        coEvery { mockNotificationsRepository.repoGetNotifications() } throws IOException("Notifications fetch failed")

        val vm = NotificationsViewModel(mockNotificationsRepository)
        advanceUntilIdle()

        val state = vm.notificationsUiState.value
        assertTrue(state is NotificationsViewModel.NotificationsUiState.Error)
        assertEquals(
            "Notifications fetch failed",
            (state as NotificationsViewModel.NotificationsUiState.Error).errorMessage,
        )
    }

    @Test
    fun `getPrettyDateFromUnixEpoch returns feature card date format`() {
        coEvery { mockNotificationsRepository.repoGetNotifications() } returns emptyList()

        val vm = NotificationsViewModel(mockNotificationsRepository)
        val result = vm.getPrettyDateFromUnixEpoch(1704067200000L)

        assertTrue(Regex("""\d{2}:\d{2}:\d{2} \d{2}/\d{2}/\d{4}""").matches(result))
    }
}
