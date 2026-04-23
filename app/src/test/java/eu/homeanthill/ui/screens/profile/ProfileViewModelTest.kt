package eu.homeanthill.ui.screens.profile

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
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
import eu.homeanthill.api.model.ProfileAPITokenResponse
import eu.homeanthill.repository.LogoutRepository
import eu.homeanthill.repository.ProfileRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockLogoutRepo = mockk<LogoutRepository>(relaxed = true)
    private val mockProfileRepo = mockk<ProfileRepository>()

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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // --- init ---

    @Test
    fun `init emits Idle with profile on success`() = runTest(testScheduler) {
        coEvery { mockProfileRepo.repoGetProfile() } returns testProfile

        val vm = ProfileViewModel(mockLogoutRepo, mockProfileRepo)
        advanceUntilIdle()

        val state = vm.profileUiState.value
        assertTrue(state is ProfileViewModel.ProfileUiState.Idle)
        assertEquals(testProfile, (state as ProfileViewModel.ProfileUiState.Idle).profile)
        coVerify(exactly = 1) { mockProfileRepo.repoGetProfile() }
    }

    @Test
    fun `init emits Error when repoGetProfile throws IOException`() = runTest(testScheduler) {
        coEvery { mockProfileRepo.repoGetProfile() } throws IOException("Profile load failed")

        val vm = ProfileViewModel(mockLogoutRepo, mockProfileRepo)
        advanceUntilIdle()

        val state = vm.profileUiState.value
        assertTrue(state is ProfileViewModel.ProfileUiState.Error)
        assertEquals("Profile load failed", (state as ProfileViewModel.ProfileUiState.Error).errorMessage)
    }

    // --- regenApiToken ---

    @Test
    fun `regenApiToken emits Idle with new api token on success`() = runTest(testScheduler) {
        coEvery { mockProfileRepo.repoGetProfile() } returns testProfile
        coEvery { mockProfileRepo.repoPostRegenAPIToken("prof1") } returns
                ProfileAPITokenResponse(apiToken = "new-api-token")

        val vm = ProfileViewModel(mockLogoutRepo, mockProfileRepo)
        advanceUntilIdle()

        vm.regenApiToken("prof1")
        advanceUntilIdle()

        val state = vm.apiTokenUiState.value
        assertTrue(state is ProfileViewModel.ApiTokenUiState.Idle)
        assertEquals("new-api-token", (state as ProfileViewModel.ApiTokenUiState.Idle).apiToken)
        coVerify(exactly = 1) { mockProfileRepo.repoPostRegenAPIToken("prof1") }
    }

    @Test
    fun `regenApiToken emits Error when repoPostRegenAPIToken throws IOException`() = runTest(testScheduler) {
        coEvery { mockProfileRepo.repoGetProfile() } returns testProfile
        coEvery { mockProfileRepo.repoPostRegenAPIToken("prof1") } throws IOException("Regen failed")

        val vm = ProfileViewModel(mockLogoutRepo, mockProfileRepo)
        advanceUntilIdle()

        vm.regenApiToken("prof1")
        advanceUntilIdle()

        val state = vm.apiTokenUiState.value
        assertTrue(state is ProfileViewModel.ApiTokenUiState.Error)
        assertEquals("Regen failed", (state as ProfileViewModel.ApiTokenUiState.Error).errorMessage)
    }

    @Test
    fun `regenApiToken does nothing when id is null`() = runTest(testScheduler) {
        coEvery { mockProfileRepo.repoGetProfile() } returns testProfile

        val vm = ProfileViewModel(mockLogoutRepo, mockProfileRepo)
        advanceUntilIdle()

        vm.regenApiToken(null)
        advanceUntilIdle()

        // repoPostRegenAPIToken must NOT be called when id is null
        coVerify(exactly = 0) { mockProfileRepo.repoPostRegenAPIToken(any()) }
    }

    // --- logout ---

    @Test
    fun `logout calls logoutRepository logoutWithServerAndRedirect`() = runTest(testScheduler) {
        coEvery { mockProfileRepo.repoGetProfile() } returns testProfile

        val vm = ProfileViewModel(mockLogoutRepo, mockProfileRepo)
        advanceUntilIdle()

        vm.logout()
        advanceUntilIdle()

        coVerify(exactly = 1) { mockLogoutRepo.logoutWithServerAndRedirect() }
    }
}
