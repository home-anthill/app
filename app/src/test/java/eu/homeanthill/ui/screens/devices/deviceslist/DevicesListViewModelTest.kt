package eu.homeanthill.ui.screens.devices.deviceslist

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.Room
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.HomesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesListViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockDevicesRepo = mockk<DevicesRepository>()
    private val mockHomesRepo = mockk<HomesRepository>()

    private val sensorDevice = Device(
        id = "dev-sensor",
        uuid = "uuid-sensor",
        mac = "aa:bb:cc:00:00:01",
        name = "Temperature Sensor",
        manufacturer = "SensorCo",
        model = "Sensor-1",
        features = listOf(Feature(uuid = "f1", type = "sensor", name = "temperature", enable = true, order = 1, unit = "°C")),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val controllerDevice = Device(
        id = "dev-ctrl",
        uuid = "uuid-ctrl",
        mac = "aa:bb:cc:00:00:02",
        name = "Thermostat",
        manufacturer = "CtrlCo",
        model = "Controller-1",
        features = listOf(Feature(uuid = "f2", type = "controller", name = "setpoint", enable = true, order = 1, unit = "")),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val unassignedDevice = Device(
        id = "dev-unassigned",
        uuid = "uuid-unassigned",
        mac = "aa:bb:cc:00:00:03",
        name = "Unassigned Sensor",
        manufacturer = "UnknownCo",
        model = "Unknown-1",
        features = listOf(Feature(uuid = "f3", type = "sensor", name = "humidity", enable = true, order = 1, unit = "%")),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val roomWithDevices = Room(
        id = "room1",
        name = "Living Room",
        floor = 0,
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
        devices = listOf(sensorDevice.id, controllerDevice.id),
    )

    private val testHome = Home(
        id = "home1",
        name = "Test Home",
        location = "Test City",
        rooms = listOf(roomWithDevices),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
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
    fun `init builds MyDevicesList with assigned and unassigned devices on success`() = runTest(testScheduler) {
        val allDevices = listOf(sensorDevice, controllerDevice, unassignedDevice)
        coEvery { mockDevicesRepo.repoGetDevices() } returns allDevices
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)

        val vm = DevicesListViewModel(mockDevicesRepo, mockHomesRepo)
        advanceUntilIdle()

        val state = vm.devicesUiState.value
        assertTrue(state is DevicesListViewModel.DevicesUiState.Idle)
        val deviceList = (state as DevicesListViewModel.DevicesUiState.Idle).deviceList
        assertNotNull(deviceList)

        // Unassigned: only the device not referenced in any room
        assertEquals(1, deviceList!!.unassignedDevices.size)
        assertEquals(unassignedDevice.id, deviceList.unassignedDevices[0].id)

        // Home devices: one home with one room containing sensor + controller
        assertEquals(1, deviceList.homeDevices.size)
        val roomSplit = deviceList.homeDevices[0].rooms[0]
        assertEquals(1, roomSplit.sensorDevices.size)
        assertEquals(sensorDevice.id, roomSplit.sensorDevices[0].id)
        assertEquals(1, roomSplit.controllerDevices.size)
        assertEquals(controllerDevice.id, roomSplit.controllerDevices[0].id)
    }

    @Test
    fun `init emits Idle with empty MyDevicesList when no devices or homes`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoGetDevices() } returns emptyList()
        coEvery { mockHomesRepo.repoGetHomes() } returns emptyList()

        val vm = DevicesListViewModel(mockDevicesRepo, mockHomesRepo)
        advanceUntilIdle()

        val state = vm.devicesUiState.value
        assertTrue(state is DevicesListViewModel.DevicesUiState.Idle)
        val deviceList = (state as DevicesListViewModel.DevicesUiState.Idle).deviceList
        assertNotNull(deviceList)
        assertEquals(0, deviceList!!.unassignedDevices.size)
        assertEquals(0, deviceList.homeDevices.size)
    }

    @Test
    fun `init emits Error when repoGetDevices throws IOException`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoGetDevices() } throws IOException("Devices fetch failed")
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)

        val vm = DevicesListViewModel(mockDevicesRepo, mockHomesRepo)
        advanceUntilIdle()

        val state = vm.devicesUiState.value
        assertTrue(state is DevicesListViewModel.DevicesUiState.Error)
        assertEquals("Devices fetch failed", (state as DevicesListViewModel.DevicesUiState.Error).errorMessage)
    }

    @Test
    fun `init emits Error when repoGetHomes throws IOException`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoGetDevices() } returns listOf(sensorDevice)
        coEvery { mockHomesRepo.repoGetHomes() } throws IOException("Homes fetch failed")

        val vm = DevicesListViewModel(mockDevicesRepo, mockHomesRepo)
        advanceUntilIdle()

        val state = vm.devicesUiState.value
        assertTrue(state is DevicesListViewModel.DevicesUiState.Error)
        assertEquals("Homes fetch failed", (state as DevicesListViewModel.DevicesUiState.Error).errorMessage)
    }
}
