package eu.homeanthill.alarm.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.homeanthill.alarm.R

import eu.homeanthill.alarm.data.LoginRepository
import eu.homeanthill.alarm.data.Result

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(context: Context, apiToken: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(context, apiToken)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(apiToken = result.data.apiToken))
        } else {
            _loginResult.value = LoginResult(error = R.string.init_failed)
        }
    }

    fun loginDataChanged(apiToken: String) {
        if (apiToken.isNotEmpty() && !apiToken.equals("")) {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }
}