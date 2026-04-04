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

import eu.homeanthill.api.model.GitHub
import eu.homeanthill.api.model.Profile
import eu.homeanthill.api.model.ProfileAPITokenResponse
import eu.homeanthill.api.requests.ProfileServices

class ProfileRepositoryTest {

    private val mockProfileService = mockk<ProfileServices>()
    private lateinit var profileRepository: ProfileRepository

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
        profileRepository = ProfileRepository(mockProfileService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // --- repoGetProfile ---

    @Test
    fun `repoGetProfile returns profile on success`() = runBlocking {
        coEvery { mockProfileService.getProfile() } returns Response.success(testProfile)

        val result = profileRepository.repoGetProfile()

        assertEquals(testProfile, result)
        coVerify(exactly = 1) { mockProfileService.getProfile() }
    }

    @Test
    fun `repoGetProfile throws IOException on error response`() = runBlocking {
        coEvery { mockProfileService.getProfile() } returns Response.error(401, "{}".toResponseBody())

        try {
            profileRepository.repoGetProfile()
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoGetProfile", e.message)
        }
    }

    // --- repoPostRegenAPIToken ---

    @Test
    fun `repoPostRegenAPIToken returns new token on success`() = runBlocking {
        val tokenResponse = ProfileAPITokenResponse(apiToken = "new-api-token-value")
        coEvery { mockProfileService.postRegenApiToken("prof1") } returns Response.success(tokenResponse)

        val result = profileRepository.repoPostRegenAPIToken("prof1")

        assertEquals(tokenResponse, result)
        coVerify(exactly = 1) { mockProfileService.postRegenApiToken("prof1") }
    }

    @Test
    fun `repoPostRegenAPIToken throws IOException on error response`() = runBlocking {
        coEvery { mockProfileService.postRegenApiToken("prof1") } returns Response.error(500, "{}".toResponseBody())

        try {
            profileRepository.repoPostRegenAPIToken("prof1")
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoPostRegenAPIToken", e.message)
        }
    }
}
