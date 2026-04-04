package eu.homeanthill.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

import eu.homeanthill.BuildConfig
import eu.homeanthill.api.model.Profile
import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.ProfileRepository

class MainViewModel(
  private val loginRepository: LoginRepository,
  private val fcmTokenRepository: FCMTokenRepository,
  private val profileRepository: ProfileRepository,
) : ViewModel() {
  companion object {
    private const val TAG = "MainViewModel"
    private const val LOAD_DELAY_MS = 500L
    private const val FCM_REGISTER_DELAY_MS = 250L
  }

  sealed class MainUiState {
    data class Idle(val profile: Profile?) : MainUiState()
    data object Loading : MainUiState()
    data class Error(val errorMessage: String) : MainUiState()
  }

  private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.Idle(null))
  val mainUiState: StateFlow<MainUiState> = _mainUiState

  init {
    init()
  }

  private fun init() {
    viewModelScope.launch {
      _mainUiState.emit(MainUiState.Loading)
      delay(LOAD_DELAY_MS)

      try {
        var fcmToken: String? = loginRepository.getFCMToken()
        if (BuildConfig.DEBUG) Log.d(TAG, "init - fcmToken = $fcmToken")
        if (fcmToken == null) {
          // register this device — throws IOException on failure
          fcmToken = registerDeviceToFirebase()
          if (BuildConfig.DEBUG) Log.d(TAG, "init - registeredFCMToken = $fcmToken")
        }
        val response = profileRepository.repoGetProfile()
        val profile = response.copy(fcmToken = fcmToken)
        if (BuildConfig.DEBUG) Log.d(TAG, "init - profile response = $profile")
        // save profile
        loginRepository.setLoggedProfile(profile = profile)
        _mainUiState.emit(MainUiState.Idle(profile))
      } catch (err: IOException) {
        if (BuildConfig.DEBUG) Log.d(TAG, "init - error = $err")
        _mainUiState.emit(MainUiState.Error(err.message.toString()))
      }
    }
  }

  private suspend fun registerDeviceToFirebase(): String {
    // Get new FCM registration token
    val token = Firebase.messaging.getToken().await()
    if (BuildConfig.DEBUG) Log.d(TAG, "fcm token = $token")

    val body = mapOf(
      "fcmToken" to token,
    )
    delay(FCM_REGISTER_DELAY_MS)
    fcmTokenRepository.repoPostFCMToken(body)
    loginRepository.setFCMToken(token)
    return token
  }
}