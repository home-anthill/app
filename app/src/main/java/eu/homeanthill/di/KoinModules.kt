package eu.homeanthill.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import eu.homeanthill.api.requests.FCMTokenServices
import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.ui.screens.home.HomeViewModel
import eu.homeanthill.ui.screens.login.LoginViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
}

val repositoryModule = module {
    single { FCMTokenRepository(get()) }
    factory { LoginRepository(androidContext(), get()) }
}

val apiModule = module {
    single { get<Retrofit>().create(FCMTokenServices::class.java) }
}

val retrofitModule = module {
    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }

    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun provideRetrofit(factory: Gson, okHttpClient: OkHttpClient): Retrofit {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("http://192.168.1.111:8082/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(factory))

        return retrofitBuilder.build()
    }

    single { provideGson() }
    single { provideHttpLoggingInterceptor() }
    single { provideOkHttpClient(get()) }
    single { provideRetrofit(get(), get()) }
}