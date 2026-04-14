package eu.homeanthill.di

import java.net.CookieManager
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.JavaNetCookieJar
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import eu.homeanthill.BuildConfig
import eu.homeanthill.FcmTokenWorker
import eu.homeanthill.api.AppAuthenticator
import eu.homeanthill.api.AuthInterceptor
import eu.homeanthill.api.SendRefreshTokenCookieInterceptor
import eu.homeanthill.api.SendSavedCookiesInterceptor
import eu.homeanthill.api.requests.DevicesServices
import eu.homeanthill.api.requests.FCMTokenServices
import eu.homeanthill.api.requests.HomesServices
import eu.homeanthill.api.requests.OnlineServices
import eu.homeanthill.api.requests.ProfileServices
import eu.homeanthill.api.requests.RefreshTokenServices
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.HomesRepository
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.OnlineRepository
import eu.homeanthill.repository.ProfileRepository
import eu.homeanthill.repository.RefreshTokenRepository
import eu.homeanthill.ui.screens.devices.deviceslist.DevicesListViewModel
import eu.homeanthill.ui.screens.devices.editdevice.EditDeviceViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.sensorValues.SensorFeatureValuesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.controllerValues.ControllerFeatureValuesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.FeaturesViewModel
import eu.homeanthill.ui.screens.devices.featurevalues.onlineValues.OnlineFeatureValuesViewModel
import eu.homeanthill.ui.screens.main.MainViewModel
import eu.homeanthill.ui.screens.homes.rooms.RoomsViewModel
import eu.homeanthill.ui.screens.homes.homeslist.HomesListViewModel
import eu.homeanthill.ui.screens.profile.ProfileViewModel

val viewModelModule = module {
  viewModel {
    MainViewModel(
      loginRepository = get(),
      profileRepository = get(),
      fcmTokenRepository = get()
    )
  }
  viewModel { ProfileViewModel(loginRepository = get(), profileRepository = get()) }
  viewModel { HomesListViewModel(homesRepository = get()) }
  viewModel { RoomsViewModel(homesRepository = get()) }
  viewModel { DevicesListViewModel(devicesRepository = get(), homesRepository = get()) }
  viewModel { EditDeviceViewModel(homesRepository = get(), devicesRepository = get()) }
  viewModel { SensorFeatureValuesViewModel() }
  viewModel { ControllerFeatureValuesViewModel(devicesRepository = get()) }
  viewModel { OnlineFeatureValuesViewModel(onlineRepository = get()) }
  viewModel { FeaturesViewModel(devicesRepository = get()) }
}

val repositoryModule = module {
  single { LoginRepository(context = androidContext()) }
  single { RefreshTokenRepository(refreshTokenService = get(), loginRepository = get()) }
  single { FCMTokenRepository(fcmTokenService = get()) }
  single { ProfileRepository(profileService = get()) }
  single { HomesRepository(homesService = get()) }
  single { DevicesRepository(devicesService = get()) }
  single { OnlineRepository(onlineService = get()) }
}

val fcmModule = module {
  // Declare worker. Koin will inject Context, WorkerParameters and repositories
  worker {
    FcmTokenWorker(
      appContext = get(),
      workerParams = get(),
      loginRepository = get(),
      fcmTokenRepository = get()
    )
  }
}

val apiModule = module {
  single { get<Retrofit>().create(FCMTokenServices::class.java) }
  single { get<Retrofit>().create(ProfileServices::class.java) }
  single { get<Retrofit>().create(HomesServices::class.java) }
  single { get<Retrofit>().create(DevicesServices::class.java) }
  single { get<Retrofit>().create(OnlineServices::class.java) }
  // RefreshTokenServices uses the dedicated refresh Retrofit instance (no AppAuthenticator)
  // to avoid infinite recursion when AppAuthenticator calls the refresh endpoint.
  single { get<Retrofit>(named("refresh")).create(RefreshTokenServices::class.java) }
}

val retrofitModule = module {
  single {
    GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
  }

  single {
    HttpLoggingInterceptor().setLevel(
      if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
      else HttpLoggingInterceptor.Level.NONE
    )
  }

  single { AuthInterceptor(loginRepository = get()) }

  single { AppAuthenticator(loginRepository = get(), refreshTokenRepository = get()) }

  single { SendSavedCookiesInterceptor(context = androidContext()) }

  single { SendRefreshTokenCookieInterceptor(context = androidContext()) }

  // Main OkHttpClient: full interceptor chain + AppAuthenticator for 401 handling.
  single {
    OkHttpClient()
      .newBuilder()
      .cookieJar(JavaNetCookieJar(CookieManager()))
      .addInterceptor(get<HttpLoggingInterceptor>())
      .addInterceptor(get<SendSavedCookiesInterceptor>())
      .addInterceptor(get<AuthInterceptor>())
      .authenticator(get<AppAuthenticator>())
      .build()
  }

  // Dedicated OkHttpClient for the refresh endpoint: no AppAuthenticator (avoids recursion),
  // no AuthInterceptor (refresh endpoint does not require a Bearer token).
  single(named("refresh")) {
    OkHttpClient()
      .newBuilder()
      .addInterceptor(get<HttpLoggingInterceptor>())
      .addInterceptor(get<SendRefreshTokenCookieInterceptor>())
      .build()
  }

  // Main Retrofit instance used by all standard API services.
  single {
    Retrofit.Builder()
      .baseUrl(BuildConfig.API_BASE_URL)
      .client(get<OkHttpClient>())
      .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
      .build()
  }

  // Dedicated Retrofit instance for the refresh endpoint.
  single(named("refresh")) {
    Retrofit.Builder()
      .baseUrl(BuildConfig.API_BASE_URL)
      .client(get<OkHttpClient>(named("refresh")))
      .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
      .build()
  }
}
