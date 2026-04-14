package eu.homeanthill

import java.util.concurrent.TimeUnit
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

object FcmScheduler {
  private const val WORK_NAME_PERIODIC = "fcm_token_periodic_refresh"
  private const val WORK_NAME_ONE_TIME = "fcm_token_immediate_sync"

  // periodic refresh function
  fun scheduleMonthlyRefresh(context: Context) {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val periodicRequest = PeriodicWorkRequestBuilder<FcmTokenWorker>(1, TimeUnit.DAYS)
      .setConstraints(constraints)
      .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
      .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      WORK_NAME_PERIODIC,
      ExistingPeriodicWorkPolicy.KEEP, // It keeps existing countdown
      periodicRequest
    )
  }

  // Immediate refresh function (used in onNewToken)
  fun scheduleImmediateRefresh(context: Context) {
    val immediateRequest = OneTimeWorkRequestBuilder<FcmTokenWorker>()
      .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      WORK_NAME_ONE_TIME,
      ExistingWorkPolicy.REPLACE,
      immediateRequest
    )
  }
}