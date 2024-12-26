package eu.homeanthill.alarm.data

import eu.homeanthill.alarm.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(apiToken: String): Result<LoggedInUser> {
        try {
            val user = LoggedInUser(apiToken)
            return Result.Success(user)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}