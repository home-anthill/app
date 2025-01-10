package eu.homeanthill.repository

import android.content.Context

import eu.homeanthill.api.model.LoggedUser

class LoginRepository(private val context: Context) {
    fun login(jwt: String) {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("jwt", jwt)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        return sharedPreference.contains("jwt")
    }

    fun setJWT(jwt: String) {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("jwt", jwt)
        editor.apply()
    }

    fun getJWT(): String? {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val jwt: String? = sharedPreference.getString("jwt", null)
        return jwt
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

    fun getLoggedUser(): LoggedUser {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val fcmToken: String? = sharedPreference.getString("fcmToken", null)
        val loggedUser: LoggedUser = LoggedUser(
            fcmToken = fcmToken,
        )
        return loggedUser
    }
}