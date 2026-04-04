package eu.homeanthill.ui.screens.main

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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

import eu.homeanthill.api.model.GitHub
import eu.homeanthill.api.model.Profile
import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.ProfileRepository

/**
 * Unit tests for [MainViewModel].
 *
 * NOTE: The `registerDeviceToFirebase()` path in `init()` calls
 * `Firebase.messaging.getToken().await()`, which requires a real Android runtime and cannot be
 * unit-tested without Robolectric or Firebase test infrastructure. These tests exclusively cover
 * the path where a Firebase Cloud Messaging token is already cached in [LoginRepository]
 * (`getFCMToken()` returns non-null), so `Firebase.messaging` is never called.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockLoginRepo = mockk<LoginRepository>(relaxed = true)
    private val mockFCMTokenRepo = mockk<FCMTokenRepository>()
    private val mockProfileRepo = mockk<ProfileRepository>()

    private val cachedFCMToken = "cached-fcm-token"

    private val testProfile = Profile(
        id = "prof1",
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
        github = GitHub(
            id = 123L,
            login = "testuser",
            name = "Test User",
            email = "test@example.com",
            avatarURL = "https://example.com/avatar.jpg",
        ),
        fcmToken = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        // Return a cached FCM token so the Firebase.messaging path is never triggered
        every { mockLoginRepo.getFCMToken() } returns cachedFCMToken
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // --- init (cached FCM token path) ---

    @Test
    fun `init emits Idle with profile when FCM token is cached and profile loads successfully`() =
        runTest(testScheduler) {
            coEvery { mockProfileRepo.repoGetProfile() } returns testProfile

            val vm = MainViewModel(mockLoginRepo, mockFCMTokenRepo, mockProfileRepo)
            advanceUntilIdle()

            val state = vm.mainUiState.value
            assertTrue(state is MainViewModel.MainUiState.Idle)
            // The profile stored in state includes the cached FCM token
            val idleProfile = (state as MainViewModel.MainUiState.Idle).profile
            assertEquals(testProfile.id, idleProfile?.id)
            assertEquals(cachedFCMToken, idleProfile?.fcmToken)
            coVerify(exactly = 1) { mockProfileRepo.repoGetProfile() }
            // Firebase registration must NOT be called when token is already cached
            coVerify(exactly = 0) { mockFCMTokenRepo.repoPostFCMToken(any()) }
        }

    @Test
    fun `init emits Error when repoGetProfile throws IOException`() = runTest(testScheduler) {
        coEvery { mockProfileRepo.repoGetProfile() } throws IOException("Profile load failed")

        val vm = MainViewModel(mockLoginRepo, mockFCMTokenRepo, mockProfileRepo)
        advanceUntilIdle()

        val state = vm.mainUiState.value
        assertTrue(state is MainViewModel.MainUiState.Error)
        assertEquals("Profile load failed", (state as MainViewModel.MainUiState.Error).errorMessage)
    }
}
