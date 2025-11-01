package com.simats.echohealth

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.simats.echohealth.Responses.ResetRequest
import com.simats.echohealth.Responses.ResetResponse
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.NetworkUtils
import com.simats.echohealth.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class resetpasswordpage : AppCompatActivity() {
    companion object {
        private const val TAG = "ResetPasswordPage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resetpasswordpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

		val newPasswordEditText = findViewById<EditText>(R.id.et_new_password)
		val confirmPasswordEditText = findViewById<EditText>(R.id.et_confirm_password)
		val resetPasswordButton = findViewById<Button>(R.id.btn_reset_password)
		val backToLoginText = findViewById<TextView>(R.id.tv_back_to_login)
		
		// Get email and token from SharedPreferences (stored during OTP flow)
		val prefs = getSharedPreferences("OTPFlow", MODE_PRIVATE)
		val email = prefs.getString("reset_email", "")
		val token = prefs.getString("reset_token", null)
		
		Log.d(TAG, "=== RESET PASSWORD PAGE DEBUG ===")
		Log.d(TAG, "Email from SharedPreferences: '$email'")
		Log.d(TAG, "Token from SharedPreferences: '$token'")
		Log.d(TAG, "Token present: ${!token.isNullOrEmpty()}")
		Log.d(TAG, "Token length: ${token?.length ?: 0}")
		Log.d(TAG, "All SharedPreferences keys: ${prefs.all.keys}")
		Log.d(TAG, "All SharedPreferences values: ${prefs.all}")
		
		// Check if token is available
		val hasValidToken = !token.isNullOrEmpty()
		Log.d(TAG, "Has valid token: $hasValidToken")
		
		if (!hasValidToken) {
			Log.w(TAG, "No token found in SharedPreferences - backend may not support token-based reset")
			// Show a warning to the user about the authentication requirement
			Toast.makeText(this, "Authentication required for password reset. Please ensure OTP verification completed successfully.", Toast.LENGTH_LONG).show()
		}
		
		if (email.isNullOrEmpty()) {
			Log.e(TAG, "No email found in SharedPreferences")
			Toast.makeText(this, "Email not found. Please start over from the beginning.", Toast.LENGTH_LONG).show()
			// Navigate back to OTP request page
			val intent = Intent(this, OtpRequestPage::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			startActivity(intent)
			finish()
			return
		}
		
		Log.d(TAG, "Email validation passed: $email")

		backToLoginText.setOnClickListener {
			// Clear the email from SharedPreferences and go back to OTP request page
			val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
			editor.remove("reset_email")
			editor.remove("reset_token")
			editor.commit()
			
			val intent = Intent(this, OtpRequestPage::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			startActivity(intent)
			finish()
		}

		resetPasswordButton.setOnClickListener {
			Log.d(TAG, "Reset password button clicked")
			Log.d(TAG, "Using email: $email")
			
			val newPassword = newPasswordEditText.text.toString().trim()
			val confirmPassword = confirmPasswordEditText.text.toString().trim()

			// Clear previous errors
			newPasswordEditText.error = null
			confirmPasswordEditText.error = null

			// Validate input
			if (newPassword.isEmpty()) {
				newPasswordEditText.error = "Please enter a new password"
				newPasswordEditText.requestFocus()
				return@setOnClickListener
			}
			if (confirmPassword.isEmpty()) {
				confirmPasswordEditText.error = "Please confirm your password"
				confirmPasswordEditText.requestFocus()
				return@setOnClickListener
			}
			if (newPassword.length < 8) {
				newPasswordEditText.error = "Password must be at least 8 characters"
				newPasswordEditText.requestFocus()
				return@setOnClickListener
			}
			
			// Additional password strength validation
			if (!newPassword.any { it.isDigit() }) {
				newPasswordEditText.error = "Password must contain at least one number"
				newPasswordEditText.requestFocus()
				return@setOnClickListener
			}
			
			if (!newPassword.any { it.isLetter() }) {
				newPasswordEditText.error = "Password must contain at least one letter"
				newPasswordEditText.requestFocus()
				return@setOnClickListener
			}
			if (newPassword != confirmPassword) {
				confirmPasswordEditText.error = "Passwords don't match"
				confirmPasswordEditText.requestFocus()
				return@setOnClickListener
			}
			if (email.isNullOrEmpty()) {
				Toast.makeText(this, "Email not found. Please try again.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			if (!NetworkUtils.isNetworkAvailable(this)) {
				Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			// Disable button to prevent multiple submissions
			resetPasswordButton.isEnabled = false
			resetPasswordButton.text = "Resetting..."

			val api = RetrofitClient.getClient().create(ApiService::class.java)
			val resetRequest = ResetRequest(email, newPassword, confirmPassword)
			Log.d(TAG, "Sending reset password request for email: $email")
			Log.d(TAG, "Password length: ${newPassword.length}")
			Log.d(TAG, "Confirm password length: ${confirmPassword.length}")
			Log.d(TAG, "Token present: ${!token.isNullOrEmpty()}")
			Log.d(TAG, "Token value: ${token?.take(10)}...")
			Log.d(TAG, "ResetRequest object: $resetRequest")
			
			// Use Bearer token format if available, otherwise try without authentication
			val call: Call<ResetResponse> = if (hasValidToken) {
				val authHeader = "Bearer $token"
				Log.d(TAG, "=== API CALL WITH TOKEN ===")
				Log.d(TAG, "Using Authorization header: Bearer ${token?.take(10)}...")
				Log.d(TAG, "Full token: $token")
				Log.d(TAG, "Request body: $resetRequest")
				api.resetPassword(authHeader, resetRequest)
			} else {
				Log.d(TAG, "=== API CALL WITHOUT TOKEN ===")
				Log.d(TAG, "No token available - calling reset password without authentication")
				Log.d(TAG, "Request body: $resetRequest")
				api.resetPassword(resetRequest)
			}
			call.enqueue(object : Callback<ResetResponse> {
				override fun onResponse(call: Call<ResetResponse>, response: Response<ResetResponse>) {
					Log.d(TAG, "=== RESET PASSWORD RESPONSE ===")
					Log.d(TAG, "Response code: ${response.code()}")
					Log.d(TAG, "Response body: ${response.body()}")
					Log.d(TAG, "Response headers: ${response.headers()}")
					Log.d(TAG, "Response is successful: ${response.isSuccessful}")
					Log.d(TAG, "Response error body: ${response.errorBody()?.string()}")
					
					// Re-enable button
					resetPasswordButton.isEnabled = true
					resetPasswordButton.text = "Reset Password"
					
					when (response.code()) {
						200, 201 -> {
							if (response.body() != null) {
								val message = response.body()!!.message
								Log.d(TAG, "Password reset successful: $message")
								Toast.makeText(this@resetpasswordpage, message, Toast.LENGTH_LONG).show()
								
								// Clear the email and token from SharedPreferences after successful reset
								val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
								editor.remove("reset_email")
								editor.remove("reset_token")
								editor.commit()
								Log.d(TAG, "Email and token cleared from SharedPreferences after successful reset.")
								
								// Navigate to success page
								val intent = Intent(this@resetpasswordpage, SuccessfulChangedPassword::class.java)
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
								startActivity(intent)
								finish()
							} else {
								Log.e(TAG, "Reset password unexpected empty body")
								Toast.makeText(this@resetpasswordpage, "Reset failed. Please try again.", Toast.LENGTH_SHORT).show()
							}
						}
						400 -> {
							Log.e(TAG, "Reset password bad request (400)")
							val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Bad request" }
							Log.e(TAG, "Error details: $errorBody")
							Toast.makeText(this@resetpasswordpage, "Invalid request. Please check your input.", Toast.LENGTH_LONG).show()
						}
						401 -> {
							Log.e(TAG, "Reset password unauthorized (401) - Token expired or invalid")
							if (hasValidToken) {
								Toast.makeText(this@resetpasswordpage, "Session expired. Please re-verify OTP.", Toast.LENGTH_LONG).show()
								val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
								editor.remove("reset_token")
								editor.commit()
								val intent = Intent(this@resetpasswordpage, OtpVerification::class.java)
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
								startActivity(intent)
								finish()
							} else {
								Toast.makeText(this@resetpasswordpage, "Password reset requires authentication. Please re-verify OTP.", Toast.LENGTH_LONG).show()
							}
						}
						403 -> {
							Log.e(TAG, "Reset password forbidden (403) - Backend rejected request")
							val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Forbidden" }
							Log.e(TAG, "403 Error details: $errorBody")
							
							if (hasValidToken) {
								Toast.makeText(this@resetpasswordpage, "Session expired or unauthorized. Please re-verify OTP.", Toast.LENGTH_LONG).show()
								val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
								editor.remove("reset_token")
								editor.commit()
								val intent = Intent(this@resetpasswordpage, OtpVerification::class.java)
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
								startActivity(intent)
								finish()
							} else {
								Toast.makeText(this@resetpasswordpage, "Password reset requires authentication. Please re-verify OTP to get a valid token.", Toast.LENGTH_LONG).show()
								val intent = Intent(this@resetpasswordpage, OtpVerification::class.java)
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
								startActivity(intent)
								finish()
							}
						}
						404 -> {
							Log.e(TAG, "Reset password user not found (404)")
							Toast.makeText(this@resetpasswordpage, "User not found. Please check your email.", Toast.LENGTH_LONG).show()
						}
						500 -> {
							Log.e(TAG, "Reset password server error (500)")
							Toast.makeText(this@resetpasswordpage, "Server error. Please try again later.", Toast.LENGTH_LONG).show()
						}
						else -> {
							Log.e(TAG, "Reset password failed: ${response.code()}")
							val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
							Log.e(TAG, "Error details: $errorBody")
							Toast.makeText(this@resetpasswordpage, "Reset failed. Please try again.", Toast.LENGTH_SHORT).show()
						}
					}
				}
					
				override fun onFailure(call: Call<ResetResponse>, t: Throwable) {
					Log.e(TAG, "Reset password network failure", t)
					
					// Re-enable button
					resetPasswordButton.isEnabled = true
					resetPasswordButton.text = "Reset Password"
					
					// Provide more specific error messages
					val errorMessage = when {
						t is java.net.UnknownHostException -> "Server not reachable. Please check your internet connection."
						t is java.net.SocketTimeoutException -> "Request timed out. Please try again."
						t is java.net.ConnectException -> "Connection failed. Please check your internet connection."
						t is java.net.SocketException -> "Network error. Please check your connection."
						else -> "Network error: ${t.localizedMessage}"
					}
					
					Toast.makeText(this@resetpasswordpage, errorMessage, Toast.LENGTH_LONG).show()
				}
			})
		}
    }
    
    override fun onBackPressed() {
        // Clear the email and go back to OTP request page
        val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
        editor.remove("reset_email")
        editor.remove("reset_token")
        editor.commit()
        
        val intent = Intent(this, OtpRequestPage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}

