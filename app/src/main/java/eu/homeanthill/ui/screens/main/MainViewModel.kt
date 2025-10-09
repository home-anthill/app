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
      delay(500)

      var fcmToken: String? = loginRepository.getFCMToken()
      Log.d(TAG, "init - fcmToken = $fcmToken")
      if (fcmToken === null) {
        // register this device
        val registeredFCMToken: String? = registerDeviceToFirebase()
        Log.d(TAG, "init - registeredFCMToken = $registeredFCMToken")
        if (registeredFCMToken == null) {
          _mainUiState.emit(MainUiState.Error("Cannot generate FCM Token"))
          return@launch
        }
        fcmToken = registeredFCMToken
      }
      try {
        val response = profileRepository.repoGetProfile()
        response.fcmToken = fcmToken
        Log.d(TAG, "init - profile response = $response")
        // save profile
        loginRepository.setLoggedProfile(profile = response)
        _mainUiState.emit(MainUiState.Idle(response))
      } catch (err: IOException) {
        Log.d(TAG, "init - error = $err")
        _mainUiState.emit(MainUiState.Error(err.message.toString()))
      }
    }
  }

  private suspend fun registerDeviceToFirebase(): String? {
    // Get new FCM registration token
    val token = Firebase.messaging.getToken().await()
    Log.d(TAG, "fcm token = $token")

    val body = mapOf(
      "fcmToken" to token,
    )
    try {
      delay(250)
      fcmTokenRepository.repoPostFCMToken(body)
      loginRepository.setFCMToken(token)
      return token
    } catch (err: IOException) {
      _mainUiState.emit(MainUiState.Error(err.message.toString()))
      return null
    }
  }
}