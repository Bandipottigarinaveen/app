package com.simats.echohealth

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.simats.echohealth.Responses.RequestOtpRequest
import com.simats.echohealth.Responses.RequestOtpResponse
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.NetworkUtils
import com.simats.echohealth.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpRequestPage : AppCompatActivity() {
    companion object {
        private const val TAG = "OTPRequestPage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otprequestpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

		val emailInput = findViewById<EditText>(R.id.emailInput)
		val resetButton = findViewById<Button>(R.id.resetpassword)
		val backToLogin = findViewById<TextView>(R.id.backToLogin)

		backToLogin.setOnClickListener {
			// Clear any existing email data and go back to login
			val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
			editor.clear()
			editor.apply()
			
			startActivity(Intent(this, LoginpageActivity::class.java))
			finish()
		}

		resetButton.setOnClickListener {
			val email = emailInput.text.toString().trim()
			Log.d(TAG, "Reset button clicked with email: $email")
			
			if (email.isEmpty()) {
				emailInput.error = "Please enter your email"
				return@setOnClickListener
			}
			if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				emailInput.error = "Enter a valid email"
				return@setOnClickListener
			}
			
			// Additional email validation
			if (email.length > 254) {
				emailInput.error = "Email is too long"
				return@setOnClickListener
			}
			
			if (!NetworkUtils.isNetworkAvailable(this)) {
				Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			// Show loading state
			resetButton.isEnabled = false
			resetButton.text = "Sending..."

			Log.d(TAG, "Sending OTP request for email: $email")
			Log.d(TAG, "Base URL: ${com.simats.echohealth.Retrofit.RetrofitClient.BASE_URL}")
			Log.d(TAG, "API endpoint: api/request-otp/")
			
			val api = RetrofitClient.getClient().create(ApiService::class.java)
			val request = RequestOtpRequest(email)
			Log.d(TAG, "Request payload: $request")
			Log.d(TAG, "Request email field: ${request.email}")
			
			api.requestOtp(request).enqueue(object : Callback<RequestOtpResponse> {
				@SuppressLint("SuspiciousIndentation")
                override fun onResponse(
					call: Call<RequestOtpResponse>,
					response: Response<RequestOtpResponse>
				) {
					Log.d(TAG, "OTP request response received")
					Log.d(TAG, "Response code: ${response.code()}")
					Log.d(TAG, "Response headers: ${response.headers()}")
					Log.d(TAG, "Response body: ${response.body()}")
					Log.d(TAG, "Error body: ${response.errorBody()?.string()}")
					Log.d(TAG, "Response URL: ${response.raw().request.url}")
					Log.d(TAG, "Response method: ${response.raw().request.method}")
					
					// Reset button state
					resetButton.isEnabled = true
					resetButton.text = "Reset Password"
					
										when (response.code()) {
						200, 201 -> {
							// Store email in SharedPreferences for later use
							val prefs = getSharedPreferences("OTPFlow", MODE_PRIVATE)
							val editor = prefs.edit()
							editor.putString("reset_email", email)
							val success = editor.commit()
							
							Log.d(TAG, "Email stored in SharedPreferences: $email")
							Log.d(TAG, "Storage success: $success")
							
							// Verify the email was stored correctly
							val storedEmail = prefs.getString("reset_email", "")
							Log.d(TAG, "Verification - Stored email: $storedEmail")
							
							// Only navigate if email was stored successfully
							if (success && storedEmail == email) {
								Toast.makeText(
									this@OtpRequestPage,
									response.body()?.message ?: "OTP sent successfully",
									Toast.LENGTH_SHORT
								).show()
								val intent = Intent(this@OtpRequestPage, OtpVerification::class.java)
								Log.d(TAG, "Navigating to OTP verification page with email: $email")
								startActivity(intent)
							} else {
								Log.e(TAG, "Failed to store email properly. Success: $success, Stored: $storedEmail, Original: $email")
								Toast.makeText(
									this@OtpRequestPage,
									"Failed to process request. Please try again.",
									Toast.LENGTH_LONG
								).show()
							}
						}
						400 -> {
							val errorBody = response.errorBody()?.string() ?: "Bad Request"
							Log.e(TAG, "OTP request failed - Bad Request: $errorBody")
							
							// Check if the error message indicates email not found
							if (errorBody.contains("not found", ignoreCase = true) || 
								errorBody.contains("email", ignoreCase = true)) {
								Toast.makeText(
									this@OtpRequestPage,
									"Email not found. Please check your email address or create an account.",
									Toast.LENGTH_LONG
								).show()
							} else {
								Toast.makeText(
									this@OtpRequestPage,
									"Invalid email format or missing fields",
									Toast.LENGTH_LONG
								).show()
							}
						}
						404 -> {
							val errorBody = response.errorBody()?.string() ?: "Not Found"
							Log.e(TAG, "OTP request failed - Not Found: $errorBody")
							Toast.makeText(
								this@OtpRequestPage,
								"OTP service not found. Please try again later.",
								Toast.LENGTH_LONG
							).show()
						}
						500 -> {
							val errorBody = response.errorBody()?.string() ?: "Server Error"
							Log.e(TAG, "OTP request failed - Server Error: $errorBody")
							Toast.makeText(
								this@OtpRequestPage,
								"Server error. Please try again later.",
								Toast.LENGTH_LONG
							).show()
						}
						else -> {
							val errorBody = response.errorBody()?.string() ?: "Unknown error"
							Log.e(TAG, "OTP request failed: ${response.code()} - $errorBody")
							Toast.makeText(
								this@OtpRequestPage,
								"Failed to send OTP: ${response.code()} - $errorBody",
								Toast.LENGTH_LONG
							).show()
							
							// Log additional response details for debugging
							Log.e(TAG, "Response headers: ${response.headers()}")
							Log.e(TAG, "Response raw: ${response.raw()}")
						}
					}
				}

				override fun onFailure(call: Call<RequestOtpResponse>, t: Throwable) {
					Log.e(TAG, "OTP request network failure", t)
					
					// Reset button state
					resetButton.isEnabled = true
					resetButton.text = "Reset Password"
					
					// Provide more specific error messages
					val errorMessage = when {
						t is java.net.UnknownHostException -> "Server not reachable. Please check your internet connection."
						t is java.net.SocketTimeoutException -> "Request timed out. Please try again."
						t is java.net.ConnectException -> "Connection failed. Please check your internet connection."
						else -> "Network error: ${t.localizedMessage}"
					}
					
					Toast.makeText(this@OtpRequestPage, errorMessage, Toast.LENGTH_LONG).show()
				}
			})
		}
    }
    
    override fun onBackPressed() {
        // Clear any existing email data and go back to login
        val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
        
        super.onBackPressed()
    }
}