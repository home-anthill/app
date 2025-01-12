package eu.homeanthill.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import eu.homeanthill.api.model.Profile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.ProfileRepository

class HomeViewModel(
    private val loginRepository: LoginRepository,
    private val fcmTokenRepository: FCMTokenRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "HomeViewModel"
    }

    sealed class HomeUiState {
        data class Idle(val profile: Profile?) : HomeUiState()
        data object Loading : HomeUiState()
        data class Error(val errorMessage: String) : HomeUiState()
    }

    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle(null))
    val homeUiState: StateFlow<HomeUiState> = _homeUiState

    init {
        init()
    }

    private fun init() {
        viewModelScope.launch {
            _homeUiState.emit(HomeUiState.Loading)
            delay(500)

            var fcmToken: String? = loginRepository.getFCMToken()
            Log.d(TAG, "init - fcmToken = $fcmToken")
            if (fcmToken === null) {
                // register this device
                val registeredFCMToken: String? = registerDeviceToFirebase()
                Log.d(TAG, "init - registeredFCMToken = $registeredFCMToken")
                if (registeredFCMToken == null) {
                    _homeUiState.emit(HomeUiState.Error("Cannot generate FCM Token"))
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
                _homeUiState.emit(HomeUiState.Idle(response))
            } catch (err: IOException) {
                _homeUiState.emit(HomeUiState.Error(err.message.toString()))
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
            _homeUiState.emit(HomeUiState.Error(err.message.toString()))
            return null
        }
    }
}