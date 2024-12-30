package eu.homeanthill.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import eu.homeanthill.api.model.LoggedUser
import eu.homeanthill.repository.FCMTokenRepository
import eu.homeanthill.repository.LoginRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

class HomeViewModel(
    private val loginRepository: LoginRepository,
    private val fcmTokenRepository: FCMTokenRepository
) : ViewModel() {
    private val TAG = "HomeViewModel"

    sealed class HomeUiState {
        data class Idle(val apiToken: String, val fcmToken: String) : HomeUiState()
        data object Loading : HomeUiState()
        data class Error(val errorMessage: String) : HomeUiState()
    }

    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle("",""))
    val homeUiState: StateFlow<HomeUiState> = _homeUiState

    init {
        init()
    }

    private fun init() {
        viewModelScope.launch {
            _homeUiState.emit(HomeUiState.Loading)
            delay(500)

            val loggedUser: LoggedUser = loginRepository.getLoggedUser()
            if (loggedUser.apiToken == null) {
                _homeUiState.emit(HomeUiState.Error("Missing API Token"))
                return@launch
            }
            if  (loggedUser.fcmToken != null) {
                _homeUiState.emit(HomeUiState.Idle(loggedUser.apiToken, loggedUser.fcmToken))
                return@launch
            }

            // register this device
            delay(250)
            val fcmToken: String? = registerDeviceToFirebase(loggedUser.apiToken)
            if (fcmToken == null) {
                _homeUiState.emit(HomeUiState.Error("Cannot generate FCM Token"))
                return@launch
            }

            _homeUiState.emit(HomeUiState.Idle(loggedUser.apiToken, fcmToken))
        }
    }

    private suspend fun registerDeviceToFirebase(apiToken: String): String? {
        // Get new FCM registration token
        val token = Firebase.messaging.getToken().await()
        Log.d(TAG, "fcm token = $token")

        val body = mapOf(
            "apiToken" to apiToken,
            "fcmToken" to token,
        )
        try {
            delay(250)
            val result = fcmTokenRepository.repoPostFCMToken(body)
            Log.d(TAG, "registerToFirebase - result = $result")
            loginRepository.setFCMToken(token)
            return token
        } catch (err: IOException) {
            _homeUiState.emit(HomeUiState.Error(err.message.toString()))
            return null
        }
    }
}