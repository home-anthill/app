package eu.homeanthill

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

import eu.homeanthill.BuildConfig
import eu.homeanthill.di.apiModule
import eu.homeanthill.di.repositoryModule

import eu.homeanthill.di.retrofitModule
import eu.homeanthill.di.viewModelModule

class App : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
      androidContext(this@App)
      modules(
        listOf(
          retrofitModule,
          apiModule,
          repositoryModule,
          viewModelModule,
        )
      )
    }
  }
}