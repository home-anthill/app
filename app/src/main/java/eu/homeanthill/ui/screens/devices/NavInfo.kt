package eu.homeanthill.ui.screens.devices

object Graph {
    const val DEVICES_GRAPH = "homes_graph"
}

sealed class DevicesRoute(val name: String) {
    data object Devices: DevicesRoute("Devices")
    data object EditDevice: DevicesRoute("EditDevice")
    data object SensorValues: DevicesRoute("SensorValues")
    data object OnlineValues: DevicesRoute("OnlineValues")
    data object DeviceValues: DevicesRoute("DeviceValues")
}