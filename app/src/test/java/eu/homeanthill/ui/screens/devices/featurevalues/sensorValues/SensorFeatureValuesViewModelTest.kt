package eu.homeanthill.ui.screens.devices.featurevalues.sensorValues

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

import eu.homeanthill.api.model.Feature
import eu.homeanthill.api.model.FeatureValue

class SensorFeatureValuesViewModelTest {

    private val vm = SensorFeatureValuesViewModel()

    private fun makeFeatureValue(name: String, value: Double, unit: String = ""): FeatureValue {
        return FeatureValue(
            feature = Feature(
                uuid = "test-uuid",
                type = "sensor",
                name = name,
                enable = true,
                order = 1,
                unit = unit,
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

    // --- getValue: light ---

    @Test
    fun `getValue returns formatted light with no decimal places`() {
        val fv = makeFeatureValue("light", 512.8, "lx")

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
    fun `getValue returns Extreme pollution for airquality 0`() {
        val fv = makeFeatureValue("airquality", 0.0)

        assertEquals("Extreme pollution", vm.getValue(fv))
    }

    @Test
    fun `getValue returns High pollution for airquality 1`() {
        val fv = makeFeatureValue("airquality", 1.0)

        assertEquals("High pollution", vm.getValue(fv))
    }

    @Test
    fun `getValue returns Mid pollution for airquality 2`() {
        val fv = makeFeatureValue("airquality", 2.0)

        assertEquals("Mid pollution", vm.getValue(fv))
    }

    @Test
    fun `getValue returns Low pollution for airquality 3`() {
        val fv = makeFeatureValue("airquality", 3.0)

        assertEquals("Low pollution", vm.getValue(fv))
    }

    @Test
    fun `getValue returns Unknown for airquality value out of range`() {
        val fv = makeFeatureValue("airquality", 99.0)

        assertEquals("Unknown", vm.getValue(fv))
    }

    // --- getValue: airpressure ---

    @Test
    fun `getValue returns formatted airpressure with no decimal places`() {
        val fv = makeFeatureValue("airpressure", 1013.25, "hPa")

        val result = vm.getValue(fv)

        assertEquals("1013 hPa", result)
    }

    // --- getValue: unknown feature ---

    @Test
    fun `getValue returns raw value and unit for unknown feature name`() {
        val fv = makeFeatureValue("unknown_sensor", 42.0, "units")

        val result = vm.getValue(fv)

        assertEquals("42.0 units", result)
    }
}
