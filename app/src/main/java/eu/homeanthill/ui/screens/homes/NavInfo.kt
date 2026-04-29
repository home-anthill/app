package eu.homeanthill.ui.screens.homes

object Graph {
  const val HOMES_GRAPH = "homes_graph"
}

sealed class HomesRoute(val name: String) {
  data object Homes : HomesRoute("Homes")
  data object HomeDetail : HomesRoute("HomeDetail")
}