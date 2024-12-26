package eu.homeanthill.alarm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.Observer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import eu.homeanthill.alarm.databinding.ActivityMainBinding
import eu.homeanthill.alarm.ui.login.LoggedInUserView
import eu.homeanthill.alarm.ui.login.LoginViewModel
import eu.homeanthill.alarm.ui.login.LoginViewModelFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        val sharedPreference = this.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        if (sharedPreference.contains("apiToken")) {
            // Navigate to the Alarm activity
            startActivity(Intent(this, AlarmActivity::class.java))
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiTokenEditText = binding.apitoken
        val loginButton = binding.init
        val loadingProgressBar = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]

        loginViewModel.loginFormState.observe(this@MainActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both apiToken / password is valid
            loginButton.isEnabled = loginState.isDataValid
        })

        loginViewModel.loginResult.observe(this@MainActivity, Observer {
            val loginResult = it ?: return@Observer

            loadingProgressBar.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            // Navigate to the Alarm activity
            startActivity(Intent(this, AlarmActivity::class.java))
        })

        apiTokenEditText.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    apiTokenEditText.text.toString(),
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                val mContext: Context = this@MainActivity
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> loginViewModel.login(
                        mContext,
                        apiTokenEditText.text.toString(),
                    )
                }
                false
            }

            loginButton.setOnClickListener {
                loadingProgressBar.visibility = View.VISIBLE
                val mContext: Context = this@MainActivity
                loginViewModel.login(mContext, apiTokenEditText.text.toString())
            }
        }

    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val apiToken = model.apiToken
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext, "$welcome $apiToken", Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}


/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}