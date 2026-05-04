package eu.homeanthill

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.tasks.await
import com.google.firebase.messaging.FirebaseMessaging

import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.LoginRepository

class FcmTokenWorker(
  appContext: Context,
  workerParams: WorkerParameters,
  private val loginRepository: LoginRepository,
  private val fcmTokenRepository: FCMTokenRepository
) : CoroutineWorker(appContext, workerParams) {
  companion object {
    private const val TAG = "FcmTokenWorker"
  }

  override suspend fun doWork(): Result {
    return try {
      if (BuildConfig.DEBUG) Log.d(TAG, "doWork - started")
      val jwtToken = loginRepository.getJWT()
      if (jwtToken.isNullOrEmpty()) {
        // cannot call server without a JWT, so we return success() to remove the task from the WorkManager queue
        if (BuildConfig.DEBUG) Log.d(TAG, "doWork - JWT missing, skipping FCM token registration")
        return Result.success()
      }
      // get a new FCM token
      val token = FirebaseMessaging.getInstance().token.await()

      // register FCM token
      fcmTokenRepository.repoPostFCMToken(mapOf("fcmToken" to token))
      loginRepository.setFCMToken(token)
      if (BuildConfig.DEBUG) Log.d(TAG, "doWork - FCM token registered")
      Result.success()
    } catch (e: Exception) {
      // WorkManager will retry later
      Log.e(TAG, "doWork - FCM token registration failed", e)
      Result.retry()
    }
  }
}
