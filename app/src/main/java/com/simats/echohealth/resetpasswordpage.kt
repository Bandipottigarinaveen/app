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
		
		// Get email from SharedPreferences (stored when OTP was requested)
		val email = getSharedPreferences("OTPFlow", MODE_PRIVATE).getString("reset_email", "")
		Log.d(TAG, "Email retrieved from SharedPreferences: $email")
		
		if (email.isNullOrEmpty()) {
			Log.e(TAG, "No email found in SharedPreferences")
			Toast.makeText(this, "Email not found. Please start over from the beginning.", Toast.LENGTH_LONG).show()
			// Navigate back to OTP request page
			val intent = Intent(this, otprequestpage::class.java)
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
			editor.apply()
			
			val intent = Intent(this, otprequestpage::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			startActivity(intent)
			finish()
		}

		resetPasswordButton.setOnClickListener {
			Log.d(TAG, "Reset password button clicked")
			Log.d(TAG, "Using email: $email")
			
			val newPassword = newPasswordEditText.text.toString()
			val confirmPassword = confirmPasswordEditText.text.toString()

			if (newPassword.isEmpty()) {
				newPasswordEditText.error = "Please enter a new password"
				return@setOnClickListener
			}
			if (confirmPassword.isEmpty()) {
				confirmPasswordEditText.error = "Please confirm your password"
				return@setOnClickListener
			}
			if (newPassword != confirmPassword) {
				confirmPasswordEditText.error = "Passwords don't match"
				return@setOnClickListener
			}
			if (newPassword.length < 8) {
				newPasswordEditText.error = "Password must be at least 8 characters"
				return@setOnClickListener
			}
			if (email.isEmpty()) {
				Toast.makeText(this, "Email not found. Please try again.", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			if (!NetworkUtils.isNetworkAvailable(this)) {
				Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			val api = RetrofitClient.getClient().create(ApiService::class.java)
			val resetRequest = ResetRequest(email, newPassword, confirmPassword)
			Log.d(TAG, "Sending reset password request: $resetRequest")
			
			api.resetPassword(resetRequest).enqueue(object : Callback<ResetResponse> {
				override fun onResponse(call: Call<ResetResponse>, response: Response<ResetResponse>) {
					Log.d(TAG, "Reset password response received")
					Log.d(TAG, "Response code: ${response.code()}")
					Log.d(TAG, "Response body: ${response.body()}")
					
					if (response.isSuccessful && response.body() != null) {
						Toast.makeText(this@resetpasswordpage, response.body()!!.message, Toast.LENGTH_LONG).show()
						val intent = Intent(this@resetpasswordpage, SuccessfulChangedPassword::class.java)
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
						startActivity(intent)
						finish()
						// Clear the email from SharedPreferences after successful reset
						val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
						editor.remove("reset_email")
						editor.apply()
						Log.d(TAG, "Email cleared from SharedPreferences after successful reset.")
					} else {
						Log.e(TAG, "Reset password failed: ${response.code()}")
						Toast.makeText(this@resetpasswordpage, "Reset failed. Please try again.", Toast.LENGTH_SHORT).show()
					}
				}

				override fun onFailure(call: Call<ResetResponse>, t: Throwable) {
					Log.e(TAG, "Reset password network failure", t)
					Toast.makeText(this@resetpasswordpage, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
				}
			})
		}
    }
    
    override fun onBackPressed() {
        // Clear the email and go back to OTP request page
        val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
        editor.remove("reset_email")
        editor.apply()
        
        val intent = Intent(this, otprequestpage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}