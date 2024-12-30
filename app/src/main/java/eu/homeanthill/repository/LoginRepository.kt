package eu.homeanthill.repository

import android.content.Context
import android.util.Log
import eu.homeanthill.api.model.LoggedUser

import eu.homeanthill.api.requests.FCMTokenServices

class LoginRepository(private val context: Context, private val fcmTokenService: FCMTokenServices) {
    fun login(apiToken: String) {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("apiToken", apiToken)
        editor.apply()
    }

    fun logout() {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.remove("apiToken")
        editor.apply()
    }

    fun setFCMToken(fcmToken: String) {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("fcmToken", fcmToken)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        return sharedPreference.contains("apiToken")
    }

    fun getLoggedUser(): LoggedUser {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val apiToken: String? = sharedPreference.getString("apiToken", null)
        val fcmToken: String? = sharedPreference.getString("fcmToken", null)
        Log.d("LoginRepository", "preferences apiToken = $apiToken")
        Log.d("LoginRepository", "preferences fcmToken = $fcmToken")

        val loggedUser: LoggedUser = LoggedUser(
            apiToken = apiToken,
            fcmToken = fcmToken,
        )
        return loggedUser
    }
}