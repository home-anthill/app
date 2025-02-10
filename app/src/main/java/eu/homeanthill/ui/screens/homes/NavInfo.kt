package eu.homeanthill.ui.screens.homes

object Graph {
    const val HOMES_GRAPH = "homes_graph"
}

sealed class HomesRoute(val name: String) {
    data object Homes: HomesRoute("Homes")
    data object EditHome: HomesRoute("EditHome")
    data object Rooms: HomesRoute("Rooms")
}