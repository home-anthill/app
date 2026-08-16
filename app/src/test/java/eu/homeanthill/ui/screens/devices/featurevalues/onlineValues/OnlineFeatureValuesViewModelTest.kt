package eu.homeanthill.ui.screens.devices.featurevalues.onlineValues

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

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.OnlineDeviceStatus
import eu.homeanthill.api.model.OnlineStatus
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.OnlineRepository

@OptIn(ExperimentalCoroutinesApi::class)
class OnlineFeatureValuesViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockOnlineRepo = mockk<OnlineRepository>()
    private val mockDevicesRepo = mockk<DevicesRepository>()

    private val testDevice = Device(
        id = "dev1",
        uuid = "uuid-1",
        mac = "aa:bb:cc:dd:ee:ff",
        name = "Online Sensor",
        manufacturer = "TestCo",
        model = "Sensor-1",
        features = listOf(Feature(uuid = "f1", type = "sensor", name = "online", enable = true, order = 1, unit = "")),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val testOnlineStatus = OnlineDeviceStatus(
        deviceId = "dev1",
        featureUuid = "f1",
        status = OnlineStatus.ONLINE,
        createdAt = "2024-01-01T10:00:00",
        modifiedAt = "2024-01-01T10:00:00",
        currentTime = "2024-01-01T10:00:30",
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

    // --- initDeviceValues ---

    @Test
    fun `initDeviceValues selects device status from bulk response`() = runTest(testScheduler) {
        coEvery { mockOnlineRepo.repoGetOnlineStatuses() } returns listOf(testOnlineStatus)

        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo, mockDevicesRepo)
        vm.initDeviceValues(testDevice)
        advanceUntilIdle()

        val state = vm.onlineValuesUiState.value
        assertTrue(state is OnlineFeatureValuesViewModel.OnlineValuesUiState.Idle)
        assertEquals(testOnlineStatus, (state as OnlineFeatureValuesViewModel.OnlineValuesUiState.Idle).onlineStatus)
        coVerify(exactly = 1) { mockOnlineRepo.repoGetOnlineStatuses() }
    }

    @Test
    fun `initDeviceValues emits Error when bulk request throws IOException`() = runTest(testScheduler) {
        coEvery { mockOnlineRepo.repoGetOnlineStatuses() } throws IOException("Online fetch failed")

        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo, mockDevicesRepo)
        vm.initDeviceValues(testDevice)
        advanceUntilIdle()

        val state = vm.onlineValuesUiState.value
        assertTrue(state is OnlineFeatureValuesViewModel.OnlineValuesUiState.Error)
        assertEquals(
            "Online fetch failed",
            (state as OnlineFeatureValuesViewModel.OnlineValuesUiState.Error).errorMessage
        )
    }

    // --- getPrettyDateFromUnixEpoch ---

    @Test
    fun `getPrettyDateFromUnixEpoch returns non-empty formatted date string`() = runTest(testScheduler) {
        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo, mockDevicesRepo)

        val result = vm.getPrettyDateFromUnixEpoch("2024-01-15T10:30:00")

        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("2024"))
    }
}
