package eu.homeanthill.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

import eu.homeanthill.api.model.Profile

class LoginRepository(private val context: Context) {
    fun hasJWT(): Boolean {
        return context
            .getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
            .contains("jwt")
    }

    fun getJWT(): String? {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val jwt: String? = sharedPreference.getString("jwt", null)
        return jwt
    }

    // setJWT is missing because it's manually set in MainActivity

    fun setFCMToken(fcmToken: String) {
        context
            .getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
            .edit()
            .putString("fcmToken", fcmToken)
            .apply()
    }

    fun getFCMToken(): String? {
        return context
            .getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
            .getString("fcmToken", null)
    }

    fun getLoggedProfile(): Profile? {
        val sharedPreference = context.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val json: String = sharedPreference.getString("profile", null) ?: return null
        return try {
            Gson().fromJson(json, Profile::class.java)
        } catch (err: JsonSyntaxException) {
            null
        }
    }

    fun setLoggedProfile(profile: Profile) {
        val json = Gson().toJson(profile)
        context
            .getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
            .edit()
            .putString("profile", json)
            .apply()
    }
}