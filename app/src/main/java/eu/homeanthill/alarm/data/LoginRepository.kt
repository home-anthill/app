package eu.homeanthill.alarm.data

import android.content.Context
import android.util.Log
import eu.homeanthill.alarm.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout(context: Context) {
        user = null
        dataSource.logout()

        val sharedPreference =  context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.remove("apiToken")
        editor.apply()
    }

    fun login(context: Context, apiToken: String): Result<LoggedInUser> {
        Log.d("test", "apiToken = $apiToken")
        val result = dataSource.login(apiToken)
        Log.d("test", "result = $result")

        if (result is Result.Success) {
            setLoggedInUser(context, result.data)
        }
        return result
    }

    private fun setLoggedInUser(context: Context, loggedInUser: LoggedInUser) {
        this.user = loggedInUser

        val sharedPreference =  context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("apiToken",this.user?.apiToken)
        editor.apply()

        // TODO If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}