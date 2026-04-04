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
import eu.homeanthill.profileKey
import eu.homeanthill.refreshTokenKey
import eu.homeanthill.securePrefs

class LoginRepository(private val context: Context) {
  private val gson = Gson()
  fun getJWT(): String? {
    return context.securePrefs().getString(jwtKey, null)
  }

  fun setJWT(token: String) {
    context.securePrefs().edit { putString(jwtKey, token) }
  }

  fun getRefreshToken(): String? {
    return context.securePrefs().getString(refreshTokenKey, null)
  }

  fun setRefreshToken(token: String) {
    context.securePrefs().edit { putString(refreshTokenKey, token) }
  }

  fun setFCMToken(fcmToken: String) {
    context.securePrefs().edit { putString(fcmTokenKey, fcmToken) }
  }

  fun getFCMToken(): String? {
    return context.securePrefs().getString(fcmTokenKey, null)
  }

  fun getLoggedProfile(): Profile? {
    val json: String = context.securePrefs().getString(profileKey, null) ?: return null
    return try {
      gson.fromJson(json, Profile::class.java)
    } catch (err: JsonSyntaxException) {
      null
    }
  }

  fun setLoggedProfile(profile: Profile) {
    val json = gson.toJson(profile)
    context.securePrefs().edit { putString(profileKey, json) }
  }

  fun logoutAndRedirect() {
    this.logout()
    val i = Intent(context, LoginActivity::class.java)
    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(i)
  }

  private fun logout() {
    context.securePrefs().edit {
      remove(profileKey).remove(fcmTokenKey).remove(jwtKey).remove(cookieKey)
        .remove(loginTimestampKey).remove(refreshTokenKey)
    }
  }
}