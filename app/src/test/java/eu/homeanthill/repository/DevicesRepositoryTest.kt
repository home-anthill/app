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

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetFeatureDeviceValue
import eu.homeanthill.api.model.PutDevice
import eu.homeanthill.api.requests.DevicesServices

class DevicesRepositoryTest {

    private val mockDevicesService = mockk<DevicesServices>()
    private lateinit var devicesRepository: DevicesRepository

    private val testDevice = Device(
        id = "dev1",
        uuid = "uuid-1",
        mac = "aa:bb:cc:dd:ee:ff",
        manufacturer = "TestCo",
        model = "Model-X",
        features = listOf(
            Feature(uuid = "feat-1", type = "sensor", name = "temperature", enable = true, order = 1, unit = "°C")
        ),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val testFeatureValue = DeviceFeatureValueResponse(
        featureUuid = "feat-1",
        type = "sensor",
        name = "temperature",
        value = 22.5,
        createdAt = 1704067200000L,
        modifiedAt = 1704067200000L,
    )

    private val okMessage = GenericMessageResponse(message = "ok")

    @Before
    fun setUp() {
        devicesRepository = DevicesRepository(mockDevicesService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // --- repoGetDevices ---

    @Test
    fun `repoGetDevices returns list of devices on success`() = runBlocking {
        coEvery { mockDevicesService.getDevices() } returns Response.success(listOf(testDevice))

        val result = devicesRepository.repoGetDevices()

        assertEquals(1, result.size)
        assertEquals(testDevice, result[0])
        coVerify(exactly = 1) { mockDevicesService.getDevices() }
    }

    @Test
    fun `repoGetDevices throws IOException on error response`() = runBlocking {
        coEvery { mockDevicesService.getDevices() } returns Response.error(500, "{}".toResponseBody())

        try {
            devicesRepository.repoGetDevices()
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoGetDevices", e.message)
        }
    }

    // --- repoAssignDeviceToHomeRoom ---

    @Test
    fun `repoAssignDeviceToHomeRoom returns message on success`() = runBlocking {
        val body = PutDevice(homeId = "home1", roomId = "room1")
        coEvery { mockDevicesService.putAssignDeviceToHomeRoom("dev1", body) } returns Response.success(okMessage)

        val result = devicesRepository.repoAssignDeviceToHomeRoom("dev1", body)

        assertEquals(okMessage, result)
        coVerify(exactly = 1) { mockDevicesService.putAssignDeviceToHomeRoom("dev1", body) }
    }

    @Test
    fun `repoAssignDeviceToHomeRoom throws IOException on error response`() = runBlocking {
        val body = PutDevice(homeId = "home1", roomId = "room1")
        coEvery { mockDevicesService.putAssignDeviceToHomeRoom("dev1", body) } returns Response.error(400, "{}".toResponseBody())

        try {
            devicesRepository.repoAssignDeviceToHomeRoom("dev1", body)
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoAssignDeviceToHomeRoom", e.message)
        }
    }

    // --- repoDeleteDevice ---

    @Test
    fun `repoDeleteDevice returns message on success`() = runBlocking {
        coEvery { mockDevicesService.deleteDevice("dev1") } returns Response.success(okMessage)

        val result = devicesRepository.repoDeleteDevice("dev1")

        assertEquals(okMessage, result)
        coVerify(exactly = 1) { mockDevicesService.deleteDevice("dev1") }
    }

    @Test
    fun `repoDeleteDevice throws IOException on error response`() = runBlocking {
        coEvery { mockDevicesService.deleteDevice("dev1") } returns Response.error(404, "{}".toResponseBody())

        try {
            devicesRepository.repoDeleteDevice("dev1")
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoDeleteDevice", e.message)
        }
    }

    // --- repoGetDeviceValues ---

    @Test
    fun `repoGetDeviceValues returns list of values on success`() = runBlocking {
        coEvery { mockDevicesService.getDeviceValues("dev1") } returns Response.success(listOf(testFeatureValue))

        val result = devicesRepository.repoGetDeviceValues("dev1")

        assertEquals(1, result.size)
        assertEquals(testFeatureValue, result[0])
        coVerify(exactly = 1) { mockDevicesService.getDeviceValues("dev1") }
    }

    @Test
    fun `repoGetDeviceValues throws IOException on error response`() = runBlocking {
        coEvery { mockDevicesService.getDeviceValues("dev1") } returns Response.error(500, "{}".toResponseBody())

        try {
            devicesRepository.repoGetDeviceValues("dev1")
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoGetDeviceValues", e.message)
        }
    }

    // --- repoPostSetValues ---

    @Test
    fun `repoPostSetValues returns message on success`() = runBlocking {
        val body = listOf(
            PostSetFeatureDeviceValue(featureUuid = "feat-1", type = "controller", name = "setpoint", value = 22.0)
        )
        coEvery { mockDevicesService.postSetValues("dev1", body) } returns Response.success(okMessage)

        val result = devicesRepository.repoPostSetValues("dev1", body)

        assertEquals(okMessage, result)
        coVerify(exactly = 1) { mockDevicesService.postSetValues("dev1", body) }
    }

    @Test
    fun `repoPostSetValues throws IOException on error response`() = runBlocking {
        val body = listOf(
            PostSetFeatureDeviceValue(featureUuid = "feat-1", type = "controller", name = "setpoint", value = 22.0)
        )
        coEvery { mockDevicesService.postSetValues("dev1", body) } returns Response.error(500, "{}".toResponseBody())

        try {
            devicesRepository.repoPostSetValues("dev1", body)
            fail("Expected IOException")
        } catch (e: IOException) {
            assertEquals("Error repoPostSetValues", e.message)
        }
    }
}
