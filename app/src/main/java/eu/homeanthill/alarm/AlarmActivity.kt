package eu.homeanthill.alarm

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import eu.homeanthill.alarm.api.ApiInterface
import eu.homeanthill.alarm.api.FCMTokenResponse
import eu.homeanthill.alarm.api.RetrofitInstance
import retrofit2.Callback
import retrofit2.Response

class AlarmActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AlarmActivity"
    }

    private lateinit var apiInterface: ApiInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_alarm)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        intent.extras?.let {
//            for (key in it.keySet()) {
//                val value = intent.extras?.get(key)
//                Log.d(TAG, "Key: $key Value: $value")
//            }
//        }

        val sharedPreference = this.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
        val apiToken: String? = sharedPreference.getString("apiToken", null)
        val fcmToken: String? = sharedPreference.getString("fcmToken", null)
        Log.d(TAG, "preferences apiToken = $apiToken")
        Log.d(TAG, "preferences fcmToken = $fcmToken")

        // show API token
        findViewById<TextView>(R.id.apitoken).text = apiToken.let { apiToken } ?: ""
        // show FCM token
        findViewById<TextView>(R.id.fcmtoken).text = fcmToken.let { fcmToken } ?: ""

        // if apiToken is stored in preferences, but you don't have fcmToken,
        // it means that this device must be registered
        if (apiToken != null && fcmToken == null) {
            apiInterface = RetrofitInstance.getInstance().create(ApiInterface::class.java)
            registerToFirebase(apiInterface, apiToken)
        } else {
            Log.d(TAG, "already registered")
        }
    }

    private fun registerToFirebase(apiInterface: ApiInterface, apiToken: String) {
        Firebase.messaging.getToken().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token: String = task.result
            Log.d(TAG, "fcm token = $token")

            val body = mapOf(
                "apiToken" to apiToken,
                "fcmToken" to token,
            )

            apiInterface.postFCMToken(body).enqueue(object : Callback<FCMTokenResponse> {
                override fun onResponse(call: Call<FCMTokenResponse>, response: Response<FCMTokenResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val resp = response.body() as FCMTokenResponse
                        Log.d(TAG, "fcmtoken response = $resp")
                        val mContext: Context = this@AlarmActivity
                        val sharedPreference = mContext.getSharedPreferences("home-anthill", Context.MODE_PRIVATE)
                        val editor = sharedPreference.edit()
                        editor.putString("fcmToken", token)
                        editor.apply()

                        // show FCM token
                        findViewById<TextView>(R.id.fcmtoken).text = token
                    }
                }

                override fun onFailure(call: Call<FCMTokenResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })

            // Log and toast
            val msg = "FCM Registration token: $token"
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
    }
}