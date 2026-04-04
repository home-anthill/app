package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
import eu.homeanthill.api.model.DeviceFeatureValueResponse
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.SendValueResult
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.ui.components.SpinnerItemObj

@OptIn(ExperimentalCoroutinesApi::class)
class ControllerFeatureValuesViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockDevicesRepo = mockk<DevicesRepository>()

    private val testDevice = Device(
        id = "dev1",
        uuid = "uuid-1",
        mac = "aa:bb:cc:dd:ee:ff",
        manufacturer = "TestCo",
        model = "AC-1",
        features = listOf(
            Feature(uuid = "f-setpoint", type = "controller", name = "setpoint", enable = true, order = 1, unit = ""),
            Feature(uuid = "f-mode", type = "controller", name = "mode", enable = true, order = 2, unit = ""),
        ),
        createdAt = "2024-01-01T00:00:00Z",
        modifiedAt = "2024-01-01T00:00:00Z",
    )

    private val setpointValue = DeviceFeatureValueResponse(
        featureUuid = "f-setpoint",
        type = "controller",
        name = "setpoint",
        value = 22.0,
        createdAt = 1704067200000L,
        modifiedAt = 1704067200000L,
    )

    private val modeValue = DeviceFeatureValueResponse(
        featureUuid = "f-mode",
        type = "controller",
        name = "mode",
        value = 1.0, // Cool
        createdAt = 1704067200000L,
        modifiedAt = 1704067200000L,
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

    // --- loadValues ---

    @Test
    fun `loadValues emits Idle with feature values on success`() = runTest(testScheduler) {
        val values = listOf(setpointValue, modeValue)
        coEvery { mockDevicesRepo.repoGetDeviceValues("dev1") } returns values

        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        vm.loadValues("dev1")
        advanceUntilIdle()

        val state = vm.getValueUiState.value
        assertTrue(state is ControllerFeatureValuesViewModel.ValuesUiState.Idle)
        assertEquals(values, (state as ControllerFeatureValuesViewModel.ValuesUiState.Idle).values)
        coVerify(exactly = 1) { mockDevicesRepo.repoGetDeviceValues("dev1") }
    }

    @Test
    fun `loadValues emits Error when repoGetDeviceValues throws IOException`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoGetDeviceValues("dev1") } throws IOException("Load values failed")

        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        vm.loadValues("dev1")
        advanceUntilIdle()

        val state = vm.getValueUiState.value
        assertTrue(state is ControllerFeatureValuesViewModel.ValuesUiState.Error)
        assertEquals("Load values failed", (state as ControllerFeatureValuesViewModel.ValuesUiState.Error).errorMessage)
    }

    // --- sendCommands ---

    @Test
    fun `sendCommands emits SendValueResult with isError false on success`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoPostSetValues(any(), any()) } returns
                GenericMessageResponse("Commands sent")

        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        // Subscribe before triggering the emission: SharedFlow(replay=0) drops items without active collectors
        val results = mutableListOf<SendValueResult>()
        val collectJob = launch { vm.sendValueResult.collect { results.add(it) } }

        vm.sendCommands(testDevice, listOf(setpointValue, modeValue))
        advanceUntilIdle()

        assertEquals(1, results.size)
        assertEquals("Commands sent", results[0].message)
        assertFalse(results[0].isError)

        collectJob.cancel()
    }

    @Test
    fun `sendCommands emits SendValueResult with isError true on IOException`() = runTest(testScheduler) {
        coEvery { mockDevicesRepo.repoPostSetValues(any(), any()) } throws IOException("Send failed")

        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val results = mutableListOf<SendValueResult>()
        val collectJob = launch { vm.sendValueResult.collect { results.add(it) } }

        vm.sendCommands(testDevice, listOf(setpointValue))
        advanceUntilIdle()

        assertEquals(1, results.size)
        assertEquals("Send failed", results[0].message)
        assertTrue(results[0].isError)

        collectJob.cancel()
    }

    // --- getSetpointByFeatureUuid ---

    @Test
    fun `getSetpointByFeatureUuid returns correct setpoint for valid uuid`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        val values = listOf(setpointValue) // value = 22.0

        val result = vm.getSetpointByFeatureUuid(values, "f-setpoint")

        assertEquals(SpinnerItemObj("22", "22"), result)
    }

    @Test
    fun `getSetpointByFeatureUuid returns first setpoint for unknown uuid`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getSetpointByFeatureUuid(emptyList(), "unknown-uuid")

        assertEquals(SpinnerItemObj("17", "17"), result)
    }

    // --- getModeByFeatureUuid ---

    @Test
    fun `getModeByFeatureUuid returns correct mode for value 1 (Cool)`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        val values = listOf(modeValue) // value = 1.0 => Cool

        val result = vm.getModeByFeatureUuid(values, "f-mode")

        assertEquals(SpinnerItemObj("Cool", "Cool"), result)
    }

    @Test
    fun `getModeByFeatureUuid returns first mode for unknown uuid`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getModeByFeatureUuid(emptyList(), "unknown-uuid")

        assertEquals(SpinnerItemObj("Cool", "Cool"), result)
    }

    // --- getFanSpeedByFeatureUuid ---

    @Test
    fun `getFanSpeedByFeatureUuid returns correct fan speed for value 1 (Min)`() = runTest(testScheduler) {
        val fanSpeedValue = DeviceFeatureValueResponse(
            featureUuid = "f-fan", type = "controller", name = "fanspeed",
            value = 1.0, createdAt = 0L, modifiedAt = 0L,
        )
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getFanSpeedByFeatureUuid(listOf(fanSpeedValue), "f-fan")

        assertEquals(SpinnerItemObj("Min", "Min"), result)
    }

    // --- getToleranceByFeatureUuid ---

    @Test
    fun `getToleranceByFeatureUuid returns correct tolerance for value 2`() = runTest(testScheduler) {
        val toleranceValue = DeviceFeatureValueResponse(
            featureUuid = "f-tol", type = "controller", name = "tolerance",
            value = 2.0, createdAt = 0L, modifiedAt = 0L,
        )
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getToleranceByFeatureUuid(listOf(toleranceValue), "f-tol")

        assertEquals(SpinnerItemObj("2", "2"), result)
    }

    // --- getSetpointValue ---

    @Test
    fun `getSetpointValue returns correct index for temperature 22`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getSetpointValue("22")

        // setpoints start at 17, so 22 - 17 = 5, but value = index + 17 = 22
        assertEquals(22, result)
    }

    // --- getModeValue ---

    @Test
    fun `getModeValue returns 1 for Cool (index 0 + 1)`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getModeValue("Cool")

        assertEquals(1, result)
    }

    @Test
    fun `getModeValue returns 3 for Heat (index 2 + 1)`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getModeValue("Heat")

        assertEquals(3, result)
    }

    // --- getFanSpeedValue ---

    @Test
    fun `getFanSpeedValue returns 1 for Min (index 0 + 1)`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getFanSpeedValue("Min")

        assertEquals(1, result)
    }

    // --- getToleranceValue ---

    @Test
    fun `getToleranceValue returns correct index for tolerance 3`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getToleranceValue("3")

        assertEquals(3, result)
    }

    // --- getPrettyDateFromUnixEpoch ---

    @Test
    fun `getPrettyDateFromUnixEpoch returns formatted date string for valid ISO timestamp`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getPrettyDateFromUnixEpoch("2024-01-15T10:30:00Z")

        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("2024"))
    }

    @Test
    fun `getPrettyDateFromUnixEpoch returns empty string for null input`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)

        val result = vm.getPrettyDateFromUnixEpoch(null)

        assertEquals("", result)
    }
}
