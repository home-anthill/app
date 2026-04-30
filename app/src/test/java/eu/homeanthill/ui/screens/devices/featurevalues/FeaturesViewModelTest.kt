package eu.homeanthill.ui.screens.devices.featurevalues

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
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

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.Home
import eu.homeanthill.api.model.PutDevice
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.HomesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class FeaturesViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockHomesRepo = mockk<HomesRepository>()
    private val mockDevicesRepo = mockk<DevicesRepository>()

    private val testDevice = Device(
        id = "dev1",
        uuid = "uuid-1",
        mac = "aa:bb:cc:dd:ee:ff",
        name = "Test Device",
        manufacturer = "TestCo",
        model = "Model-X",
        features = listOf(
            Feature(uuid = "feat-1", type = "sensor", name = "temperature", enable = true, order = 1, unit = "°C"),
        ),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val testHome = Home(
        id = "home1",
        name = "Test Home",
        location = "Test City",
        rooms = null,
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val okMessage = GenericMessageResponse(message = "ok")

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
    fun `initDeviceValues emits Idle with device values and homes on success`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoGetDeviceValues("dev1") } returns emptyList()
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)

        val vm = FeaturesViewModel(mockDevicesRepo, mockHomesRepo)
        vm.initDeviceValues(testDevice)
        advanceUntilIdle()

        val state = vm.featureValuesUiState.value
        assertTrue(state is FeaturesViewModel.FeatureValuesUiState.Idle)
        val idle = state as FeaturesViewModel.FeatureValuesUiState.Idle
        assertEquals(testDevice, idle.deviceValue?.device)
        assertEquals(listOf(testHome), idle.homes)
        coVerify(exactly = 1) { mockDevicesRepo.repoGetDeviceValues("dev1") }
        coVerify(exactly = 1) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `initDeviceValues emits Error when repoGetHomes throws IOException`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoGetDeviceValues("dev1") } returns emptyList()
        coEvery { mockHomesRepo.repoGetHomes() } throws IOException("Load homes failed")

        val vm = FeaturesViewModel(mockDevicesRepo, mockHomesRepo)
        vm.initDeviceValues(testDevice)
        advanceUntilIdle()

        val state = vm.featureValuesUiState.value
        assertTrue(state is FeaturesViewModel.FeatureValuesUiState.Error)
        assertEquals("Load homes failed", (state as FeaturesViewModel.FeatureValuesUiState.Error).errorMessage)
    }

    @Test
    fun `updateDeviceSettings assigns device and refreshes values on success`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoGetDeviceValues("dev1") } returns emptyList()
        coEvery { mockHomesRepo.repoGetHomes() } returns listOf(testHome)
        coEvery { mockDevicesRepo.repoAssignDeviceToHomeRoom(any(), any()) } returns okMessage

        val vm = FeaturesViewModel(mockDevicesRepo, mockHomesRepo)
        vm.initDeviceValues(testDevice)
        advanceUntilIdle()

        vm.updateDeviceSettings("dev1", "New Name", "home1", "room1")
        advanceUntilIdle()

        val state = vm.featureValuesUiState.value
        assertTrue(state is FeaturesViewModel.FeatureValuesUiState.Idle)
        assertEquals("New Name", (state as FeaturesViewModel.FeatureValuesUiState.Idle).deviceValue?.device?.name)
        coVerify(exactly = 1) {
            mockDevicesRepo.repoAssignDeviceToHomeRoom(
                "dev1",
                PutDevice(name = "New Name", homeId = "home1", roomId = "room1")
            )
        }
        coVerify(exactly = 2) { mockDevicesRepo.repoGetDeviceValues("dev1") }
        coVerify(exactly = 2) { mockHomesRepo.repoGetHomes() }
    }

    @Test
    fun `updateDeviceSettings emits Error when repoAssignDeviceToHomeRoom throws IOException`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoAssignDeviceToHomeRoom(any(), any()) } throws IOException("Assign failed")

        val vm = FeaturesViewModel(mockDevicesRepo, mockHomesRepo)
        vm.updateDeviceSettings("dev1", "New Name", "home1", "room1")
        advanceUntilIdle()

        val state = vm.featureValuesUiState.value
        assertTrue(state is FeaturesViewModel.FeatureValuesUiState.Error)
        assertEquals("Assign failed", (state as FeaturesViewModel.FeatureValuesUiState.Error).errorMessage)
    }

    @Test
    fun `deleteDevice calls repoDeleteDevice and onDeleted on success`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoDeleteDevice("dev1") } returns okMessage
        var deleted = false

        val vm = FeaturesViewModel(mockDevicesRepo, mockHomesRepo)
        vm.deleteDevice("dev1") { deleted = true }
        advanceUntilIdle()

        assertTrue(deleted)
        coVerify(exactly = 1) { mockDevicesRepo.repoDeleteDevice("dev1") }
    }

    @Test
    fun `deleteDevice emits Error when repoDeleteDevice throws IOException`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoDeleteDevice("dev1") } throws IOException("Delete failed")

        val vm = FeaturesViewModel(mockDevicesRepo, mockHomesRepo)
        vm.deleteDevice("dev1") {}
        advanceUntilIdle()

        val state = vm.featureValuesUiState.value
        assertTrue(state is FeaturesViewModel.FeatureValuesUiState.Error)
        assertEquals("Delete failed", (state as FeaturesViewModel.FeatureValuesUiState.Error).errorMessage)
    }
}
