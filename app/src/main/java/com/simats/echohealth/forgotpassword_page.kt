package com.simats.echohealth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


class ForgotPasswordPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otprequestpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Forgot password works even if not logged in

        // Initialize buttons
        val resetPasswordButton = findViewById<Button>(R.id.resetpassword)
        val backToLoginText = findViewById<TextView>(R.id.backToLogin)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        
        // Request OTP then navigate
        resetPasswordButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Persist email for later screens
            getSharedPreferences("ResetFlow", MODE_PRIVATE).edit().putString("email", email).apply()
            requestOtp(email)
        }
        
        // Navigate back to login page when Back to Login is clicked
        backToLoginText.setOnClickListener {
            val intent = Intent(this, LoginpageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun requestOtp(email: String) {
        // Save email for later screens
        getSharedPreferences("ResetFlow", MODE_PRIVATE).edit().putString("email", email).apply()

        // Call backend
        val api = com.simats.echohealth.Retrofit.RetrofitClient
            .getClient()
            .create(com.simats.echohealth.Retrofit.ApiService::class.java)

        val body = com.simats.echohealth.Responses.RequestOtpRequest(email)
        api.requestOtp(body).enqueue(object : retrofit2.Callback<com.simats.echohealth.Responses.RequestOtpResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.simats.echohealth.Responses.RequestOtpResponse>,
                response: retrofit2.Response<com.simats.echohealth.Responses.RequestOtpResponse>
            ) {
                if (response.isSuccessful) {
                    // Navigate to OTP verification screen
                    val intent = Intent(this@ForgotPasswordPage, resetpasswordpage::class.java)
                    startActivity(intent)
                } else {
                    android.widget.Toast.makeText(
                        this@ForgotPasswordPage,
                        "Failed to request OTP: ${response.code()}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.simats.echohealth.Responses.RequestOtpResponse>,
                t: Throwable
            ) {
                android.widget.Toast.makeText(
                    this@ForgotPasswordPage,
                    "Network error: ${t.localizedMessage}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}