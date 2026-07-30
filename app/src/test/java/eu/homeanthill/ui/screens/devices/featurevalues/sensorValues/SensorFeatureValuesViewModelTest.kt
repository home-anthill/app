package eu.homeanthill.ui.screens.devices.featurevalues.sensorValues

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
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

import eu.homeanthill.api.model.Device
import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.FeatureValue
import eu.homeanthill.api.model.Format
import eu.homeanthill.api.model.Spec
import eu.homeanthill.api.model.GenericMessageResponse
import eu.homeanthill.repository.DevicesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SensorFeatureValuesViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val mockDevicesRepository = mockk<DevicesRepository>()
    private val vm = SensorFeatureValuesViewModel(mockDevicesRepository)

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun makeFeatureValue(
        name: String,
        value: Double,
        unit: String = "",
        spec: Spec = Spec(format = Format.FLOAT, step = 0.01f),
    ): FeatureValue {
        return FeatureValue(
            feature = Feature(
                uuid = "test-uuid",
                type = "sensor",
                name = name,
                enable = true,
                order = 1,
                unit = unit,
                spec = spec,
            ),
            value = value,
            createdAt = 1704067200000L,
            modifiedAt = 1704067200000L,
        )
    }

    // --- getPrettyDateFromUnixEpoch ---

    @Test
    fun `getPrettyDateFromUnixEpoch returns 19-char formatted date for valid epoch millis`() {
        val result = vm.getPrettyDateFromUnixEpoch(1704067200000L) // 2024-01-01 00:00:00 UTC

        // format "HH:mm:ss dd/MM/yyyy" = 19 chars; content is timezone-dependent but length is fixed
        assertEquals(19, result.length)
        assertTrue(result.contains("/"))
    }

    // --- getValue: temperature ---

    @Test
    fun `getValue returns formatted temperature with unit`() {
        val fv = makeFeatureValue("temperature", 22.567, "°C")

        val result = vm.getValue(fv)

        // String.format("%.2f") is locale-dependent; match either '.' or ',' as decimal separator
        assertTrue(result.matches(Regex("22[.,]57 °C")))
    }

    // --- getValue: humidity ---

    @Test
    fun `getValue returns formatted humidity with unit`() {
        val fv = makeFeatureValue("humidity", 65.4321, "%")

        val result = vm.getValue(fv)

        assertTrue(result.matches(Regex("65[.,]43 %")))
    }

    @Test
    fun `getValue caps spec step precision at two decimal places`() {
        val fv = makeFeatureValue("temperature", 12.3456, "°C", Spec(format = Format.FLOAT, step = 0.001f))

        val result = vm.getValue(fv)

        assertEquals("12.35 °C", result)
    }

    // --- getValue: light ---

    @Test
    fun `getValue returns formatted light with no decimal places`() {
        val fv = makeFeatureValue("light", 512.8, "lx", Spec(format = Format.FLOAT, step = 1f))

        val result = vm.getValue(fv)

        assertEquals("513 lx", result)
    }

    // --- getValue: motion ---

    @Test
    fun `getValue returns False for motion value 0`() {
        val fv = makeFeatureValue("motion", 0.0)

        val result = vm.getValue(fv)

        assertEquals("False", result)
    }

    @Test
    fun `getValue returns True for motion value 1`() {
        val fv = makeFeatureValue("motion", 1.0)

        val result = vm.getValue(fv)

        assertEquals("True", result)
    }

    // --- getValue: airquality ---

    @Test
    fun `getValue returns Poor for airquality 0`() {
        val fv = makeFeatureValue("airquality", 0.0)

        assertEquals("Poor", vm.getValue(fv))
    }

    @Test
    fun `getValue returns Low for airquality 1`() {
        val fv = makeFeatureValue("airquality", 1.0)

        assertEquals("Low", vm.getValue(fv))
    }

    @Test
    fun `getValue returns Good for airquality 2`() {
        val fv = makeFeatureValue("airquality", 2.0)

        assertEquals("Good", vm.getValue(fv))
    }

    @Test
    fun `getValue returns Excellent for airquality 3`() {
        val fv = makeFeatureValue("airquality", 3.0)

        assertEquals("Excellent", vm.getValue(fv))
    }

    @Test
    fun `getValue returns Unknown for airquality value out of range`() {
        val fv = makeFeatureValue("airquality", 99.0)

        assertEquals("Unknown", vm.getValue(fv))
    }

    // --- getValue: airpressure ---

    @Test
    fun `getValue returns formatted airpressure with no decimal places`() {
        val fv = makeFeatureValue("airpressure", 1013.25, "hPa", Spec(format = Format.FLOAT, step = 1f))

        val result = vm.getValue(fv)

        assertEquals("1013 hPa", result)
    }

    // --- getValue: unknown feature ---

    @Test
    fun `getValue formats unknown numeric feature from spec step`() {
        val fv = makeFeatureValue("unknown_sensor", 42.0, "units")

        val result = vm.getValue(fv)

        assertEquals("42.00 units", result)
    }

    @Test
    fun `getThermostatMode maps supported mode sensor values`() {
        assertEquals(SensorFeatureValuesViewModel.ThermostatMode.ERROR, vm.getThermostatMode(makeFeatureValue("mode", -1.0)))
        assertEquals(SensorFeatureValuesViewModel.ThermostatMode.SLEEP, vm.getThermostatMode(makeFeatureValue("mode", 0.0)))
        assertEquals(SensorFeatureValuesViewModel.ThermostatMode.COLD, vm.getThermostatMode(makeFeatureValue("mode", 1.0)))
        assertEquals(SensorFeatureValuesViewModel.ThermostatMode.HEAT, vm.getThermostatMode(makeFeatureValue("mode", 2.0)))
    }

    @Test
    fun `getThermostatMode returns null for unsupported and fractional values`() {
        assertEquals(null, vm.getThermostatMode(makeFeatureValue("mode", 3.0)))
        assertEquals(null, vm.getThermostatMode(makeFeatureValue("mode", 1.5)))
    }

    @Test
    fun `getThermostatMode only accepts float mode sensors`() {
        val integerMode = makeFeatureValue(
            name = "mode",
            value = 1.0,
            spec = Spec(format = Format.INT, step = 1f),
        )

        assertEquals(null, vm.getThermostatMode(integerMode))
        assertEquals(null, vm.getThermostatMode(makeFeatureValue("temperature", 2.0)))
    }

    @Test
    fun `supportsNotifications accepts motion and thermostat mode only`() {
        assertTrue(vm.supportsNotifications(makeFeatureValue("motion", 1.0)))
        assertTrue(vm.supportsNotifications(makeFeatureValue("mode", -1.0)))
        assertEquals(false, vm.supportsNotifications(makeFeatureValue("temperature", 22.0)))
    }

    @Test
    fun `setFeatureNotificationSilenced updates the selected sensor feature`() = runTest(testScheduler) {
        val device = Device(
            id = "device-id",
            uuid = "device-uuid",
            mac = "AA:BB:CC:DD:EE:FF",
            name = "Device",
            manufacturer = "home-anthill",
            model = "sensor",
            features = emptyList(),
            createdAt = "",
            modifiedAt = "",
        )
        var succeeded = false
        coEvery {
            mockDevicesRepository.repoSetFeatureNotification(
                device.id,
                "motion-feature",
                true,
            )
        } returns GenericMessageResponse("updated")

        vm.setFeatureNotificationSilenced(
            device = device,
            featureUuid = "motion-feature",
            notificationSilenced = true,
            onSuccess = { succeeded = true },
        )
        advanceUntilIdle()

        assertTrue(succeeded)
        coVerify(exactly = 1) {
            mockDevicesRepository.repoSetFeatureNotification(
                device.id,
                "motion-feature",
                true,
            )
        }
    }
}
