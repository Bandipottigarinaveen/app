package com.simats.echohealth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.simats.echohealth.Responses.LoginRequest
import com.simats.echohealth.Responses.LoginResponse
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginpageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.loginpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize buttons and views
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val forgotpassBtn = findViewById<TextView>(R.id.forgotPassword)
        val signUpText = findViewById<TextView>(R.id.signIn)
        val passwordField = findViewById<EditText>(R.id.password)
        val emailField = findViewById<EditText>(R.id.email)
        
        // Password visibility toggle functionality
        passwordField.setOnTouchListener { _, event ->
            val drawableEnd = 2 // Index for drawableEnd
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                if (event.rawX >= (passwordField.right - passwordField.compoundDrawables[drawableEnd].bounds.width())) {
                    // Eye icon was clicked
                    if (passwordField.transformationMethod is PasswordTransformationMethod) {
                        // Show password - use open eye
                        passwordField.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(
                            passwordField.compoundDrawables[0], // drawableStart
                            passwordField.compoundDrawables[1], // drawableTop
                            getDrawable(R.drawable.eye), // drawableEnd - open eye
                            passwordField.compoundDrawables[3]  // drawableBottom
                        )
                    } else {
                        // Hide password - use closed eye
                        passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(
                            passwordField.compoundDrawables[0], // drawableStart
                            passwordField.compoundDrawables[1], // drawableTop
                            getDrawable(R.drawable.eye_closed), // drawableEnd - closed eye
                            passwordField.compoundDrawables[3]  // drawableBottom
                        )
                    }
                    // Move cursor to end
                    passwordField.setSelection(passwordField.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
        
        // Navigate to createaccount when Sign Up is clicked
        signUpText.setOnClickListener {
            val intent = Intent(this, CreateAccount::class.java)
            startActivity(intent)
        }
        
        // Navigate to dashboard when Login is clicked
        loginBtn.setOnClickListener {
            Log.d("LoginPage", "Login button clicked")
            
            // Basic validation
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            
            Log.d("LoginPage", "Email: $email, Password length: ${password.length}")
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Check network
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call login API
            performLogin(email, password)
        }
        
        // Navigate to forgot password when Forgot Password is clicked
        forgotpassBtn.setOnClickListener {
            val intent = Intent(this, ForgotPasswordPage::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(email: String, password: String) {
        val request = LoginRequest(email = email, password = password)
        val api = RetrofitClient.getClient().create(ApiService::class.java)

        api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("LoginPage", "Login success. Body token: ${body?.token}")
                    Log.d("LoginPage", "Login headers: ${response.headers()}")
                    // Persist token if provided in body
                    var storedToken: String? = null
                    body?.token?.let { token ->
                        getSharedPreferences("AuthPrefs", MODE_PRIVATE).edit()
                            .putString("auth_token", token)
                            .apply()
                        storedToken = token
                        Log.d("LoginPage", "Stored auth token (body): $token")
                    }
                    // Fallback: check Authorization header (e.g., "Bearer <token>")
                    if (storedToken.isNullOrBlank()) {
                        val authHeader = response.headers()["Authorization"]
                        val headerToken = authHeader
                            ?.removePrefix("Bearer ")
                            ?.removePrefix("Token ")
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                        headerToken?.let { token ->
                            getSharedPreferences("AuthPrefs", MODE_PRIVATE).edit()
                                .putString("auth_token", token)
                                .apply()
                            storedToken = token
                            Log.d("LoginPage", "Stored auth token (header): $token")
                        }
                    }
                    // Final check
                    val saved = getSharedPreferences("AuthPrefs", MODE_PRIVATE).getString("auth_token", null)
                    Log.d("LoginPage", "AuthPrefs.auth_token present: ${!saved.isNullOrBlank()}")
                    Toast.makeText(this@LoginpageActivity, body?.message ?: "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginpageActivity, Dashboard::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    Toast.makeText(this@LoginpageActivity, "Login failed: ${response.code()} ${errorMsg ?: ""}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginpageActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
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
}