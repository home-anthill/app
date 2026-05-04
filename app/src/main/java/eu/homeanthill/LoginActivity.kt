package eu.homeanthill

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import eu.homeanthill.ui.screens.login.LoginScreen
import eu.homeanthill.ui.theme.AppTheme
import eu.homeanthill.repository.AppLoginExchangeRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LoginActivity : ComponentActivity() {
  private val appLoginExchangeRepository: AppLoginExchangeRepository by inject()

  companion object {
    private const val TAG = "LoginActivity"
  }

  /**
   * Extracts OAuth2 callback params from [data], persists credentials, and launches
   * [PermissionActivity]. Returns true if all required parameters were present and handled.
   */
  private fun handleOAuthCallback(data: Uri): Boolean {
    if (!isAllowedOAuthCallback(data)) {
      Log.e(
        TAG,
        "handleOAuthCallback - callback URI is not allowed: " +
            "scheme=${data.scheme}, host=${data.host}, port=${data.port}, path=${data.path}; " +
            "expected scheme=${BuildConfig.OAUTH_CALLBACK_SCHEME}, " +
            "host=${BuildConfig.OAUTH_CALLBACK_HOST}, " +
            "port=${BuildConfig.OAUTH_CALLBACK_PORT}, " +
            "path=${BuildConfig.OAUTH_CALLBACK_PATH}",
      )
      clearPendingOAuth()
      return false
    }
    val code = data.getQueryParameter("code") ?: run {
      Log.e(TAG, "handleOAuthCallback - login code is missing")
      clearPendingOAuth()
      return false
    }
    val returnedState = data.getQueryParameter("state") ?: run {
      Log.e(TAG, "handleOAuthCallback - OAuth state is missing")
      clearPendingOAuth()
      return false
    }
    val storedState = this.securePrefs().getString(oauthStateKey, null) ?: run {
      Log.e(TAG, "handleOAuthCallback - stored OAuth state is missing")
      clearPendingOAuth()
      return false
    }
    if (returnedState != storedState) {
      Log.e(TAG, "handleOAuthCallback - OAuth state mismatch")
      clearPendingOAuth()
      return false
    }
    val codeVerifier = this.securePrefs().getString(pkceCodeVerifierKey, null) ?: run {
      Log.e(TAG, "handleOAuthCallback - PKCE code verifier is missing")
      clearPendingOAuth()
      return false
    }
    lifecycleScope.launch {
      val result = appLoginExchangeRepository.exchangeCode(code, codeVerifier)
      if (result == null) {
        Log.e(TAG, "handleOAuthCallback - code exchange failed")
        clearPendingOAuth()
        return@launch
      }

      this@LoginActivity.securePrefs().edit {
        putString(jwtKey, result.token)
          .putString(refreshTokenKey, result.refreshToken)
          .remove(pkceCodeVerifierKey)
          .remove(oauthStateKey)
      }
      FcmScheduler.scheduleImmediateRefresh(this@LoginActivity.applicationContext)
      val i = Intent(this@LoginActivity, PermissionActivity::class.java)
      i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      startActivity(i)
      finish()
    }
    return true
  }

  private fun isAllowedOAuthCallback(data: Uri): Boolean {
    return matchesOAuthCallback(
      data = data,
      scheme = BuildConfig.OAUTH_CALLBACK_SCHEME,
      host = BuildConfig.OAUTH_CALLBACK_HOST,
      port = BuildConfig.OAUTH_CALLBACK_PORT,
      path = BuildConfig.OAUTH_CALLBACK_PATH,
    ) || (BuildConfig.DEBUG && matchesOAuthCallback(
      data = data,
      scheme = BuildConfig.DEBUG_OAUTH_CALLBACK_SCHEME,
      host = BuildConfig.DEBUG_OAUTH_CALLBACK_HOST,
      port = BuildConfig.DEBUG_OAUTH_CALLBACK_PORT,
      path = BuildConfig.DEBUG_OAUTH_CALLBACK_PATH,
    ))
  }

  private fun matchesOAuthCallback(
    data: Uri,
    scheme: String,
    host: String,
    port: String,
    path: String,
  ): Boolean {
    if (scheme.isBlank() || host.isBlank() || path.isBlank()) return false
    val expectedPort = port.toIntOrNull()
    val actualPort = data.port
    return data.scheme == scheme &&
        data.host == host &&
        data.path == path &&
        when {
          port.isBlank() -> true
          actualPort == expectedPort -> true
          actualPort == -1 && scheme == "https" && expectedPort == 443 -> true
          actualPort == -1 && scheme == "http" && expectedPort == 80 -> true
          else -> false
        }
  }

  private fun clearPendingOAuth() {
    this.securePrefs().edit {
      remove(pkceCodeVerifierKey)
        .remove(oauthStateKey)
    }
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
      Log.e(TAG, "onNewIntent - login code is missing")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // If already authenticated, go straight to _root_ide_package_.eu.homeanthill.PermissionActivity — also protects against a duplicate
    // OAuth callback arriving via intent.data after the first callback already saved the JWT.
    val storedJwt = this.securePrefs().getString(jwtKey, null)
    if (storedJwt != null) {
      clearPendingOAuth()
      val i = Intent(this@LoginActivity, PermissionActivity::class.java)
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
      AppTheme {
        LoginScreen(
          onLoginClick = {
            val codeVerifier = PKCE.generateCodeVerifier()
            val codeChallenge = PKCE.buildCodeChallenge(codeVerifier)
            val oauthState = PKCE.generateState()
            context.securePrefs().edit {
              putString(pkceCodeVerifierKey, codeVerifier)
                .putString(oauthStateKey, oauthState)
            }
            val loginUri = (BuildConfig.API_BASE_URL + "oauth/app/login")
              .toUri()
              .buildUpon()
              .appendQueryParameter("code_challenge", codeChallenge)
              .appendQueryParameter("code_challenge_method", "S256")
              .appendQueryParameter("app_state", oauthState)
              .build()
            val intent = Intent(
              Intent.ACTION_VIEW,
              loginUri
            )
            context.startActivity(intent)
          }
        )
      }
    }
  }
}
