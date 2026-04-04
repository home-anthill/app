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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.OnlineValue
import eu.homeanthill.repository.OnlineRepository

@OptIn(ExperimentalCoroutinesApi::class)
class OnlineFeatureValuesViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockOnlineRepo = mockk<OnlineRepository>()

    private val testDevice = Device(
        id = "dev1",
        uuid = "uuid-1",
        mac = "aa:bb:cc:dd:ee:ff",
        manufacturer = "TestCo",
        model = "Sensor-1",
        features = listOf(Feature(uuid = "f1", type = "sensor", name = "online", enable = true, order = 1, unit = "")),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val testOnlineValue = OnlineValue(
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
    fun `initDeviceValues emits Idle with online value on success`() = runTest(testScheduler) {
        coEvery { mockOnlineRepo.repoGetOnlineValues("dev1") } returns testOnlineValue

        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo)
        vm.initDeviceValues(testDevice)
        advanceUntilIdle()

        val state = vm.onlineValuesUiState.value
        assertTrue(state is OnlineFeatureValuesViewModel.OnlineValuesUiState.Idle)
        assertEquals(testOnlineValue, (state as OnlineFeatureValuesViewModel.OnlineValuesUiState.Idle).onlineValue)
        coVerify(exactly = 1) { mockOnlineRepo.repoGetOnlineValues("dev1") }
    }

    @Test
    fun `initDeviceValues emits Error when repoGetOnlineValues throws IOException`() = runTest(testScheduler) {
        coEvery { mockOnlineRepo.repoGetOnlineValues("dev1") } throws IOException("Online fetch failed")

        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo)
        vm.initDeviceValues(testDevice)
        advanceUntilIdle()

        val state = vm.onlineValuesUiState.value
        assertTrue(state is OnlineFeatureValuesViewModel.OnlineValuesUiState.Error)
        assertEquals(
            "Online fetch failed",
            (state as OnlineFeatureValuesViewModel.OnlineValuesUiState.Error).errorMessage
        )
    }

    // --- isOffline ---

    @Test
    fun `isOffline returns false when device updated within 60 seconds`() = runTest(testScheduler) {
        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo)
        // 30 seconds apart — device is online
        val result = vm.isOffline(
            modifiedAtISO = "2024-01-01T10:00:00",
            currentTimeISO = "2024-01-01T10:00:30",
        )

        assertFalse(result)
    }

    @Test
    fun `isOffline returns true when device has not been updated for more than 60 seconds`() = runTest(testScheduler) {
        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo)
        // 90 seconds apart — device is offline
        val result = vm.isOffline(
            modifiedAtISO = "2024-01-01T10:00:00",
            currentTimeISO = "2024-01-01T10:01:30",
        )

        assertTrue(result)
    }

    @Test
    fun `isOffline returns false when device updated exactly 60 seconds ago (boundary)`() = runTest(testScheduler) {
        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo)
        // Exactly 60 seconds — NOT offline (threshold is strictly less than 60s ago)
        val result = vm.isOffline(
            modifiedAtISO = "2024-01-01T10:00:00",
            currentTimeISO = "2024-01-01T10:01:00",
        )

        assertFalse(result)
    }

    // --- getPrettyDateFromUnixEpoch ---

    @Test
    fun `getPrettyDateFromUnixEpoch returns non-empty formatted date string`() = runTest(testScheduler) {
        val vm = OnlineFeatureValuesViewModel(mockOnlineRepo)

        val result = vm.getPrettyDateFromUnixEpoch("2024-01-15T10:30:00")

        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("2024"))
    }
}
