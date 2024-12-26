package eu.homeanthill.alarm.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val apiToken: String
    //... other data fields that may be accessible to the UI
)