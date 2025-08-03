package eu.homeanthill.repository

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import androidx.core.content.edit
import eu.homeanthill.LoginActivity

import eu.homeanthill.api.model.Profile
import eu.homeanthill.cookieKey
import eu.homeanthill.fcmTokenKey
import eu.homeanthill.jwtKey
import eu.homeanthill.loginTimestampKey
import eu.homeanthill.mainKey
import eu.homeanthill.profileKey

class LoginRepository(private val context: Context) {
    fun getJWT(): String? {
        val sharedPreference = context.getSharedPreferences(mainKey, Context.MODE_PRIVATE)
        val jwt: String? = sharedPreference.getString(jwtKey, null)
        return jwt
    }

    // setJWT is missing because it's manually set in MainActivity
    fun setFCMToken(fcmToken: String) {
        context.getSharedPreferences(mainKey, Context.MODE_PRIVATE).edit {
            putString(fcmTokenKey, fcmToken)
        }
    }

    fun getFCMToken(): String? {
        return context.getSharedPreferences(mainKey, Context.MODE_PRIVATE)
            .getString(fcmTokenKey, null)
    }

    fun getLoggedProfile(): Profile? {
        val sharedPreference = context.getSharedPreferences(mainKey, Context.MODE_PRIVATE)
        val json: String = sharedPreference.getString(profileKey, null) ?: return null
        return try {
            Gson().fromJson(json, Profile::class.java)
        } catch (err: JsonSyntaxException) {
            null
        }
    }

    fun setLoggedProfile(profile: Profile) {
        val json = Gson().toJson(profile)
        context.getSharedPreferences(mainKey, Context.MODE_PRIVATE).edit {
            putString(profileKey, json)
        }
    }

    fun logoutAndRedirect() {
        this.logout()
        val i = Intent(context, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(i)
    }

    private fun logout() {
        context.getSharedPreferences(mainKey, Context.MODE_PRIVATE).edit {
            remove(profileKey).remove(fcmTokenKey).remove(jwtKey).remove(cookieKey)
                .remove(loginTimestampKey)
        }
    }
}