package eu.homeanthill.ui.screens.devices.featurevalues.controllerValues

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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
import eu.homeanthill.api.model.Format
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.api.model.PostSetFeatureDeviceValue
import eu.homeanthill.api.model.SendValueResult
import eu.homeanthill.api.model.Spec
import eu.homeanthill.api.model.SpecListItem
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
        name = "AC Controller",
        manufacturer = "TestCo",
        model = "AC-1",
        features = listOf(
            Feature(
                uuid = "f-setpoint",
                type = "controller",
                name = "setpoint",
                enable = true,
                order = 1,
                unit = "",
                spec = Spec(format = Format.INT, min = 17f, max = 30f, step = 1f),
            ),
            Feature(
                uuid = "f-mode",
                type = "controller",
                name = "mode",
                enable = true,
                order = 2,
                unit = "",
                spec = Spec(
                    format = Format.LIST,
                    list = listOf(
                        SpecListItem(1, "Cool"),
                        SpecListItem(2, "Auto"),
                        SpecListItem(3, "Heat"),
                    )
                ),
            ),
            Feature(
                uuid = "f-tol",
                type = "controller",
                name = "tolerance",
                enable = true,
                order = 3,
                unit = "",
                spec = Spec(format = Format.INT, min = 0f, max = 10f, step = 1f),
            ),
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

    @Test
    fun `sendCommands rounds and clamps setpoint and tolerance values`() = runTest(testScheduler) {
        val sentValues = slot<List<PostSetFeatureDeviceValue>>()
        coEvery { mockDevicesRepo.repoPostSetValues("dev1", capture(sentValues)) } returns
                GenericMessageResponse("Commands sent")

        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        val fractionalSetpoint = setpointValue.copy(value = 30.7)
        val fractionalTolerance = DeviceFeatureValueResponse(
            featureUuid = "f-tol",
            type = "controller",
            name = "tolerance",
            value = 1.6,
            createdAt = 1704067200000L,
            modifiedAt = 1704067200000L,
        )

        vm.sendCommands(testDevice, listOf(fractionalSetpoint, fractionalTolerance))
        advanceUntilIdle()

        assertEquals(30.0, sentValues.captured.first { it.name == "setpoint" }.value, 0.0)
        assertEquals(2.0, sentValues.captured.first { it.name == "tolerance" }.value, 0.0)
    }

    @Test
    fun `sendCommands excludes disabled controller features`() = runTest(testScheduler) {
        val sentValues = slot<List<PostSetFeatureDeviceValue>>()
        coEvery { mockDevicesRepo.repoPostSetValues("dev1", capture(sentValues)) } returns
                GenericMessageResponse("Commands sent")
        val deviceWithDisabledSetpoint = testDevice.copy(
            features = testDevice.features.map {
                if (it.uuid == "f-setpoint") it.copy(enable = false) else it
            }
        )

        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        vm.sendCommands(deviceWithDisabledSetpoint, listOf(setpointValue, modeValue))
        advanceUntilIdle()

        assertEquals(listOf("f-mode"), sentValues.captured.map { it.featureUuid })
    }

    // --- spec list options ---

    @Test
    fun `getOptions returns list values from feature spec`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        val modeFeature = testDevice.features.first { it.uuid == "f-mode" }

        val result = vm.getOptions(modeFeature)

        assertEquals(
            listOf(
                SpinnerItemObj("1", "Cool"),
                SpinnerItemObj("2", "Auto"),
                SpinnerItemObj("3", "Heat"),
            ),
            result
        )
    }

    @Test
    fun `getSelectedOption returns matching list option for current value`() = runTest(testScheduler) {
        val vm = ControllerFeatureValuesViewModel(mockDevicesRepo)
        val modeFeature = testDevice.features.first { it.uuid == "f-mode" }

        val result = vm.getSelectedOption(modeFeature, modeValue.copy(value = 3.0))

        assertEquals(SpinnerItemObj("3", "Heat"), result)
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
