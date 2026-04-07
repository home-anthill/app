package eu.homeanthill

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.LoginRepository

class FCMService : FirebaseMessagingService() {
  companion object {
    private const val TAG = "FCMService"
  }

  // Service-scoped coroutine scope; cancelled in onDestroy so no work outlives the service.
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val loginRepository: LoginRepository by inject()
  private val fcmTokenRepository: FCMTokenRepository by inject()

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    if (BuildConfig.DEBUG) Log.d(TAG, "From: ${remoteMessage.from}")

    if (remoteMessage.data.isNotEmpty()) {
      handleNow()
    }

    remoteMessage.notification?.let {
      if (BuildConfig.DEBUG) Log.d(TAG, "Message Notification Body: ${it.body}")
    }

    // Forward to the foreground bus so the UI can show an in-app snackbar.
    FCMNotificationBus.emit(remoteMessage)
  }

  /**
   * Called when the FCM registration token is refreshed (initially generated or rotated).
   * Persists the new token and re-registers it with the server if the user is logged in.
   */
  override fun onNewToken(token: String) {
    loginRepository.setFCMToken(token)
    // Only attempt server registration if a JWT is present (user is logged in).
    if (loginRepository.getJWT() != null) {
      sendRegistrationToServer(token)
    }
  }

  private fun handleNow() {
    if (BuildConfig.DEBUG) Log.d(TAG, "Short lived task is done.")
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
  }

  private fun sendRegistrationToServer(token: String) {
    serviceScope.launch {
      try {
        fcmTokenRepository.repoPostFCMToken(mapOf("fcmToken" to token))
      } catch (e: IOException) {
        Log.e(TAG, "sendRegistrationToServer - failed to register refreshed FCM token: $e")
      }
    }
  }
}
