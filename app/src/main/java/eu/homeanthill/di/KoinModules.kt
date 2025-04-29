package eu.homeanthill.di

import java.net.CookieManager
import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.JavaNetCookieJar
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import eu.homeanthill.BuildConfig
import eu.homeanthill.api.AuthInterceptor
import eu.homeanthill.api.SendSavedCookiesInterceptor
import eu.homeanthill.api.requests.DevicesServices
import eu.homeanthill.api.requests.FCMTokenServices
import eu.homeanthill.api.requests.HomesServices
import eu.homeanthill.api.requests.ProfileServices
import eu.homeanthill.repository.DevicesRepository
import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.HomesRepository
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.ProfileRepository
import eu.homeanthill.ui.screens.devices.deviceslist.DevicesListViewModel
import eu.homeanthill.ui.screens.devices.editdevice.EditDeviceViewModel
import eu.homeanthill.ui.screens.devices.sensor.SensorViewModel
import eu.homeanthill.ui.screens.home.HomeViewModel
import eu.homeanthill.ui.screens.homes.rooms.RoomsViewModel
import eu.homeanthill.ui.screens.homes.homeslist.HomesListViewModel
import eu.homeanthill.ui.screens.profile.ProfileViewModel

val viewModelModule = module {
    viewModel {
        HomeViewModel(
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
    viewModel { SensorViewModel(devicesRepository = get()) }
}

val repositoryModule = module {
    factory { LoginRepository(context = androidContext()) }
    single { FCMTokenRepository(fcmTokenService = get()) }
    single { ProfileRepository(profileService = get()) }
    single { HomesRepository(homesService = get()) }
    single { DevicesRepository(devicesService = get()) }
}

val apiModule = module {
    single { get<Retrofit>().create(FCMTokenServices::class.java) }
    single { get<Retrofit>().create(ProfileServices::class.java) }
    single { get<Retrofit>().create(HomesServices::class.java) }
    single { get<Retrofit>().create(DevicesServices::class.java) }
}

val retrofitModule = module {
    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }

    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
    }

    fun provideAuthInterceptor(loginRepository: LoginRepository): AuthInterceptor {
        return AuthInterceptor(loginRepository)
    }

    fun provideSendSavedCookiesInterceptor(context: Context): SendSavedCookiesInterceptor {
        return SendSavedCookiesInterceptor(context)
    }

    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        sendSavedCookiesInterceptor: SendSavedCookiesInterceptor,
    ): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .cookieJar(JavaNetCookieJar(CookieManager()))
            .addInterceptor(loggingInterceptor).addInterceptor(sendSavedCookiesInterceptor)
            .addInterceptor(authInterceptor)
//            .apply {
//                // TODO add a custom `authenticator()` to intercept 401 and force logout
//            }
            .build()
    }

    fun provideRetrofit(factory: Gson, okHttpClient: OkHttpClient): Retrofit {
        val retrofitBuilder =
            Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(factory))

        return retrofitBuilder.build()
    }

    single { provideGson() }
    single { provideHttpLoggingInterceptor() }
    single {
        provideOkHttpClient(
            loggingInterceptor = get(),
            authInterceptor = get(),
            sendSavedCookiesInterceptor = get(),
        )
    }
    single { provideRetrofit(factory = get(), okHttpClient = get()) }
    single { provideAuthInterceptor(loginRepository = get()) }
    single { provideSendSavedCookiesInterceptor(context = androidContext()) }
}