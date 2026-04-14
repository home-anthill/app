package eu.homeanthill

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
  companion object {
    private const val TAG = "FCMService"
  }

  // Service-scoped coroutine scope; cancelled in onDestroy so no work outlives the service.
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    // Don't call Server APIs here, instead we delegate JWT and retry management to the Worker.
    FcmScheduler.scheduleImmediateRefresh(applicationContext)
  }

  private fun handleNow() {
    if (BuildConfig.DEBUG) Log.d(TAG, "Short lived task is done.")
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
  }
}
