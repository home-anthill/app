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

import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.NewHome
import eu.homeanthill.api.model.Room
import eu.homeanthill.api.model.RoomRequest
import eu.homeanthill.api.model.UpdateHome
import eu.homeanthill.api.requests.HomesServices

class HomesRepositoryTest {

    private val mockHomesService = mockk<HomesServices>()
    private lateinit var homesRepository: HomesRepository

    private val testHome = Home(
        id = "home1",
        name = "Test Home",
        location = "Test City",
        rooms = null,
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val testRoom = Room(
        id = "room1",
        name = "Living Room",
        floor = 0,
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
        devices = null,
    )

    private val okMessage = GenericMessageResponse(message = "ok")

    @Before
    fun setUp() {
        homesRepository = HomesRepository(mockHomesService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // --- repoGetHomes ---

    @Test
    fun `repoGetHomes returns list of homes on success`() = runBlocking {
        coEvery { mockHomesService.getHomes() } returns Response.success(listOf(testHome))

        val result = homesRepository.repoGetHomes()

        assertEquals(1, result.size)
        assertEquals(testHome, result[0])
        coVerify(exactly = 1) { mockHomesService.getHomes() }
    }

    @Test
    fun `repoGetHomes throws IOException on error response`() = runBlocking {
        coEvery { mockHomesService.getHomes() } returns Response.error(500, "{}".toResponseBody())

        try {
            homesRepository.repoGetHomes()
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoGetHomes", e.message)
        }
    }

    // --- repoPostHome ---

    @Test
    fun `repoPostHome returns created home on success`() = runBlocking {
        val body = NewHome(name = "New Home", location = "City", rooms = listOf())
        coEvery { mockHomesService.postHome(body) } returns Response.success(testHome)

        val result = homesRepository.repoPostHome(body)

        assertEquals(testHome, result)
        coVerify(exactly = 1) { mockHomesService.postHome(body) }
    }

    @Test
    fun `repoPostHome throws IOException on error response`() = runBlocking {
        val body = NewHome(name = "New Home", location = "City", rooms = listOf())
        coEvery { mockHomesService.postHome(body) } returns Response.error(400, "{}".toResponseBody())

        try {
            homesRepository.repoPostHome(body)
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoPostHome", e.message)
        }
    }

    // --- repoPutHome ---

    @Test
    fun `repoPutHome returns message on success`() = runBlocking {
        val body = UpdateHome(name = "Updated Home", location = "New City")
        coEvery { mockHomesService.putHome("home1", body) } returns Response.success(okMessage)

        val result = homesRepository.repoPutHome("home1", body)

        assertEquals(okMessage, result)
        coVerify(exactly = 1) { mockHomesService.putHome("home1", body) }
    }

    @Test
    fun `repoPutHome throws IOException on error response`() = runBlocking {
        val body = UpdateHome(name = "Updated Home", location = "New City")
        coEvery { mockHomesService.putHome("home1", body) } returns Response.error(404, "{}".toResponseBody())

        try {
            homesRepository.repoPutHome("home1", body)
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoPutHome", e.message)
        }
    }

    // --- repoDeleteHome ---

    @Test
    fun `repoDeleteHome returns message on success`() = runBlocking {
        coEvery { mockHomesService.deleteHome("home1") } returns Response.success(okMessage)

        val result = homesRepository.repoDeleteHome("home1")

        assertEquals(okMessage, result)
        coVerify(exactly = 1) { mockHomesService.deleteHome("home1") }
    }

    @Test
    fun `repoDeleteHome throws IOException on error response`() = runBlocking {
        coEvery { mockHomesService.deleteHome("home1") } returns Response.error(500, "{}".toResponseBody())

        try {
            homesRepository.repoDeleteHome("home1")
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoDeleteHome", e.message)
        }
    }

    // --- repoGetRooms ---

    @Test
    fun `repoGetRooms returns list of rooms on success`() = runBlocking {
        coEvery { mockHomesService.getRooms("home1") } returns Response.success(listOf(testRoom))

        val result = homesRepository.repoGetRooms("home1")

        assertEquals(1, result.size)
        assertEquals(testRoom, result[0])
        coVerify(exactly = 1) { mockHomesService.getRooms("home1") }
    }

    @Test
    fun `repoGetRooms throws IOException on error response`() = runBlocking {
        coEvery { mockHomesService.getRooms("home1") } returns Response.error(500, "{}".toResponseBody())

        try {
            homesRepository.repoGetRooms("home1")
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoGetRooms", e.message)
        }
    }

    // --- repoPostRoom ---

    @Test
    fun `repoPostRoom returns created room on success`() = runBlocking {
        val body = RoomRequest(name = "Kitchen", floor = 1)
        coEvery { mockHomesService.postRoom("home1", body) } returns Response.success(testRoom)

        val result = homesRepository.repoPostRoom("home1", body)

        assertEquals(testRoom, result)
        coVerify(exactly = 1) { mockHomesService.postRoom("home1", body) }
    }

    @Test
    fun `repoPostRoom throws IOException on error response`() = runBlocking {
        val body = RoomRequest(name = "Kitchen", floor = 1)
        coEvery { mockHomesService.postRoom("home1", body) } returns Response.error(400, "{}".toResponseBody())

        try {
            homesRepository.repoPostRoom("home1", body)
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoPostRoom", e.message)
        }
    }

    // --- repoPutRoom ---

    @Test
    fun `repoPutRoom returns message on success`() = runBlocking {
        val body = RoomRequest(name = "Updated Room", floor = 2)
        coEvery { mockHomesService.putRoom("home1", "room1", body) } returns Response.success(okMessage)

        val result = homesRepository.repoPutRoom("home1", "room1", body)

        assertEquals(okMessage, result)
        coVerify(exactly = 1) { mockHomesService.putRoom("home1", "room1", body) }
    }

    @Test
    fun `repoPutRoom throws IOException on error response`() = runBlocking {
        val body = RoomRequest(name = "Updated Room", floor = 2)
        coEvery { mockHomesService.putRoom("home1", "room1", body) } returns Response.error(404, "{}".toResponseBody())

        try {
            homesRepository.repoPutRoom("home1", "room1", body)
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoPutRoom", e.message)
        }
    }

    // --- repoDeleteRoom ---

    @Test
    fun `repoDeleteRoom returns message on success`() = runBlocking {
        coEvery { mockHomesService.deleteRoom("home1", "room1") } returns Response.success(okMessage)

        val result = homesRepository.repoDeleteRoom("home1", "room1")

        assertEquals(okMessage, result)
        coVerify(exactly = 1) { mockHomesService.deleteRoom("home1", "room1") }
    }

    @Test
    fun `repoDeleteRoom throws IOException on error response`() = runBlocking {
        coEvery { mockHomesService.deleteRoom("home1", "room1") } returns Response.error(500, "{}".toResponseBody())

        try {
            homesRepository.repoDeleteRoom("home1", "room1")
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoDeleteRoom", e.message)
        }
    }
}
