package eu.homeanthill.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import eu.homeanthill.repository.LoginRepository

class LoginViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {
    sealed class LoginUiState {
        data class HasJWT(val hasJWT: Boolean) : LoginUiState()
    }

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.HasJWT(false))
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    init {
        init()
    }

    private fun init() {
        viewModelScope.launch {
            val hasJWT = loginRepository.isLoggedIn()
            _loginUiState.emit(LoginUiState.HasJWT(hasJWT))
        }
    }
}