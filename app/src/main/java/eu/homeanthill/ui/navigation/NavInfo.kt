package eu.homeanthill.ui.navigation

object Graph {
    const val MAIN = "main_graph"
}

sealed class MainRoute(val name: String) {
    data object Login: MainRoute("Login")
    data object Home: MainRoute("Home")
}