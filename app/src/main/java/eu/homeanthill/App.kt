package eu.homeanthill

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.ext.android.getKoin
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.androidx.workmanager.koin.workManagerFactory
import androidx.work.Configuration

import eu.homeanthill.di.apiModule
import eu.homeanthill.di.fcmModule
import eu.homeanthill.di.repositoryModule
import eu.homeanthill.di.retrofitModule
import eu.homeanthill.di.viewModelModule

class App : Application(), Configuration.Provider {

  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
      androidContext(this@App)
      workManagerFactory()
      modules(
        listOf(
          retrofitModule,
          apiModule,
          repositoryModule,
          viewModelModule,
          fcmModule,
        )
      )
    }

    // Start scheduling
    FcmScheduler.schedulePeriodically(this)
  }

  // Custom config for WorkManager for Koin
  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder().setWorkerFactory(getKoin().get())
      .setMinimumLoggingLevel(android.util.Log.INFO).build()
}