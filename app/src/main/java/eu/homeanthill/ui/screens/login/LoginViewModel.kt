package eu.homeanthill.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.homeanthill.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {
    sealed class LoginUiState {
        data class IsLogged(val loggedIn: Boolean) : LoginUiState()
        data class Error(val errorMessage: String) : LoginUiState()
    }

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.IsLogged(false))
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    init {
        init()
    }

    fun login(apiToken: String) {
        loginRepository.login(apiToken)
    }

    private fun init() {
        viewModelScope.launch {
            val isLogged = loginRepository.isLoggedIn()
            Log.d("LoginViewModel", "isLogged = $isLogged")
            if (isLogged) {
                _loginUiState.emit(LoginUiState.IsLogged(true))
            } else {
                _loginUiState.emit(LoginUiState.IsLogged(false))
            }
        }
    }
}