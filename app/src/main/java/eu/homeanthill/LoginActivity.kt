package eu.homeanthill

import android.content.Context
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

  public override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val data: Uri? = intent.data
    Log.d(TAG, "onNewIntent query = ${data?.query}")

    // these 2 query params must match those on server-side
    val jwt = data?.getQueryParameter("token")
    val cookie = data?.getQueryParameter("session_cookie")

    if (jwt === null || cookie === null) {
      Log.e(TAG, "onNewIntent either jwt or cookie are missing")
      return
    }

    val unixTime = System.currentTimeMillis() / 1000L

    this.getSharedPreferences(mainKey, MODE_PRIVATE)
      .edit {
        putString(cookieKey, cookie)
          .putString(jwtKey, jwt)
          .putLong(loginTimestampKey, unixTime)
      }

    // restart the activity
    val i = Intent(this@LoginActivity, MainActivity::class.java)
    // set the new task and clear flags
    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(i)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(TAG, "secret env - API_BASE_URL = ${BuildConfig.API_BASE_URL}")

    val jwt = this.getSharedPreferences(mainKey, MODE_PRIVATE)
      .getString(jwtKey, null)

    if (jwt != null) {
      val i = Intent(this@LoginActivity, MainActivity::class.java)
      finish()
      startActivity(i)
    }
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