package eu.homeanthill.ui.screens.profile

import java.io.IOException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import eu.homeanthill.BuildConfig
import eu.homeanthill.api.model.ProfileAPITokenResponse
import eu.homeanthill.api.model.Profile
import eu.homeanthill.repository.LoginRepository
import eu.homeanthill.repository.ProfileRepository

class ProfileViewModel(
  private val loginRepository: LoginRepository,
  private val profileRepository: ProfileRepository,
) : ViewModel() {
  companion object {
    private const val TAG = "ProfileViewModel"
    private const val LOAD_DELAY_MS = 250L
  }

  sealed class ProfileUiState {
    data class Idle(val profile: Profile?) : ProfileUiState()
    data object Loading : ProfileUiState()
    data class Error(val errorMessage: String) : ProfileUiState()
  }

  sealed class ApiTokenUiState {
    data class Idle(val apiToken: String) : ApiTokenUiState()
    data object Loading : ApiTokenUiState()
    data class Error(val errorMessage: String) : ApiTokenUiState()
  }

  private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle(null))
  val profileUiState: StateFlow<ProfileUiState> = _profileUiState
  private val _apiTokenUiState =
    MutableStateFlow<ApiTokenUiState>(ApiTokenUiState.Idle("********-****-****-****-************"))
  val apiTokenUiState: StateFlow<ApiTokenUiState> = _apiTokenUiState

  init {
    init()
  }

  fun regenApiToken(id: String?) {
    if (id == null) {
      return
    }
    viewModelScope.launch {
      _apiTokenUiState.emit(ApiTokenUiState.Loading)
      delay(LOAD_DELAY_MS)
      try {
        val response: ProfileAPITokenResponse = profileRepository.repoPostRegenAPIToken(id)
        _apiTokenUiState.emit(ApiTokenUiState.Idle(response.apiToken))
      } catch (err: IOException) {
        _apiTokenUiState.emit(ApiTokenUiState.Error(err.message.toString()))
      }
    }
  }

  fun logout() {
    loginRepository.logoutAndRedirect()
  }

  private fun init() {
    viewModelScope.launch {
      _profileUiState.emit(ProfileUiState.Loading)
      delay(LOAD_DELAY_MS)
      try {
        val response = profileRepository.repoGetProfile()
        if (BuildConfig.DEBUG) Log.d(TAG, "init - profile response = $response")
        _profileUiState.emit(ProfileUiState.Idle(response))
      } catch (err: IOException) {
        _profileUiState.emit(ProfileUiState.Error(err.message.toString()))
      }
    }
  }
}