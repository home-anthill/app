package eu.homeanthill

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri

import eu.homeanthill.ui.theme.AppTheme

class LoginActivity : ComponentActivity() {
  companion object {
    private const val TAG = "LoginActivity"
  }

  /**
   * Extracts OAuth2 callback params from [data], persists credentials, and launches
   * [MainActivity]. Returns true if all required parameters were present and handled.
   */
  private fun handleOAuthCallback(data: Uri): Boolean {
    val jwt = data.getQueryParameter("token")
    val cookie = data.getQueryParameter("session_cookie")
    val refreshToken = data.getQueryParameter("refresh_token")
    if (jwt == null || cookie == null || refreshToken == null) return false

    val unixTime = System.currentTimeMillis() / 1000L
    this.securePrefs().edit {
      putString(cookieKey, cookie)
        .putString(jwtKey, jwt)
        .putString(refreshTokenKey, refreshToken)
        .putLong(loginTimestampKey, unixTime)
    }
    val i = Intent(this@LoginActivity, MainActivity::class.java)
    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(i)
    return true
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val data: Uri? = intent.data

    // Guard against a duplicate OAuth callback from the server (LoginMobileAppCallback can fire
    // twice in quick succession on first install). If a JWT is already stored the first callback
    // already succeeded — discard this one so we don't overwrite a valid session with a broken one.
    val existingJwt = this.securePrefs().getString(jwtKey, null)
    if (existingJwt != null) {
      Log.w(TAG, "onNewIntent - JWT already stored, discarding duplicate OAuth callback")
      finish()
      return
    }

    if (data == null || !handleOAuthCallback(data)) {
      Log.e(TAG, "onNewIntent - jwt, cookie, or refreshToken are missing")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // If already authenticated, go straight to MainActivity — also protects against a duplicate
    // OAuth callback arriving via intent.data after the first callback already saved the JWT.
    val storedJwt = this.securePrefs().getString(jwtKey, null)
    if (storedJwt != null) {
      val i = Intent(this@LoginActivity, MainActivity::class.java)
      finish()
      startActivity(i)
      return
    }

    // Handle OAuth callback delivered via intent.data when the process was killed while
    // the user was in the browser (process death + singleTask means onNewIntent is never called).
    val data: Uri? = intent.data
    if (data != null && handleOAuthCallback(data)) return

    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      AppTheme(dynamicColor = false) {
        Scaffold(
          topBar = {},
          content = { padding ->
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Button(
                onClick = {
                  val intent = Intent(
                    Intent.ACTION_VIEW,
                    (BuildConfig.API_BASE_URL + "login_app").toUri()
                  )
                  context.startActivity(intent)
                },
                enabled = true,
              ) {
                Text(text = stringResource(R.string.login_button))
              }
            }
          }
        )
      }
    }
  }
}