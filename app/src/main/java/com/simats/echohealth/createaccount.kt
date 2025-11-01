package com.simats.echohealth

import android.Manifest
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.echohealth.Responses.SignupRequest
import com.simats.echohealth.Responses.SignupResponse
import com.simats.echohealth.Responses.LoginRequest
import com.simats.echohealth.Responses.LoginResponse
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.RetrofitClient
import com.simats.echohealth.auth.AuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccount : AppCompatActivity() {
    
    // UI Elements
    lateinit var signUpButton: Button
    lateinit var signInText: TextView
    lateinit var termsText: TextView
    lateinit var fullNameInput: EditText
    lateinit var emailInput: EditText
    lateinit var passwordInput: EditText
    lateinit var confirmPasswordInput: EditText
    lateinit var showPasswordIcon: ImageView
    lateinit var showConfirmPasswordIcon: ImageView
    
    companion object {
        private const val TAG = "CreateAccount"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_createaccount)
        
        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
        
        // Setup password visibility toggles
        setupPasswordVisibilityToggles()
    }
    
    private fun initializeViews() {
        signUpButton = findViewById(R.id.signUpButton)
        signInText = findViewById(R.id.signInText)
        termsText = findViewById(R.id.termsText)
        fullNameInput = findViewById(R.id.fullName)
        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        confirmPasswordInput = findViewById(R.id.confirmPassword)
        showPasswordIcon = findViewById(R.id.showPassword)
        showConfirmPasswordIcon = findViewById(R.id.showConfirmPassword)
    }
    
    private fun setupClickListeners() {
        // Sign Up button click listener
        signUpButton.setOnClickListener {
            performSignUp()
        }
        
        // Sign In text click listener
        signInText.setOnClickListener {
            navigateToLogin()
        }
        
        // Terms text click listener
        termsText.setOnClickListener {
            navigateToTerms()
        }
    }
    
    private fun setupPasswordVisibilityToggles() {
        // Password visibility toggle
        showPasswordIcon.setOnClickListener {
            togglePasswordVisibility(passwordInput, showPasswordIcon)
        }
        
        // Confirm password visibility toggle
        showConfirmPasswordIcon.setOnClickListener {
            togglePasswordVisibility(confirmPasswordInput, showConfirmPasswordIcon)
        }
    }
    
    private fun togglePasswordVisibility(editText: EditText, icon: ImageView) {
        if (editText.transformationMethod is PasswordTransformationMethod) {
            // Show password - use open eye
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.eye)
        } else {
            // Hide password - use closed eye
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.eye_closed)
        }
        // Move cursor to end
        editText.setSelection(editText.text.length)
    }
    
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun performSignUp() {
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()
        
        Log.d(TAG, "Signup attempt - Email: $email, Full Name: $fullName")
        
        // Validation
        if (!validateInputs(fullName, email, password, confirmPassword)) {
            return
        }
        
        // Check network availability
        if (!isNetworkAvailable()) {
            Toast.makeText(this@CreateAccount, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Proceed with signup
        processSignUp(fullName, email, password)
    }
    
    private fun validateInputs(fullName: String, email: String, password: String, confirmPassword: String): Boolean {
        // Check if fields are empty
        if (fullName.isEmpty()) {
            Toast.makeText(this@CreateAccount, "Please enter your full name", Toast.LENGTH_SHORT).show()
            fullNameInput.requestFocus()
            return false
        }
        
        if (email.isEmpty()) {
            Toast.makeText(this@CreateAccount, "Please enter your email address", Toast.LENGTH_SHORT).show()
            emailInput.requestFocus()
            return false
        }
        
        // Basic email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this@CreateAccount, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            emailInput.requestFocus()
            return false
        }
        
        if (password.isEmpty()) {
            Toast.makeText(this@CreateAccount, "Please enter a password", Toast.LENGTH_SHORT).show()
            passwordInput.requestFocus()
            return false
        }
        
        // Password strength validation
        if (password.length < 6) {
            Toast.makeText(this@CreateAccount, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            passwordInput.requestFocus()
            return false
        }
        
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this@CreateAccount, "Please confirm your password", Toast.LENGTH_SHORT).show()
            confirmPasswordInput.requestFocus()
            return false
        }
        
        if (password != confirmPassword) {
            Toast.makeText(this@CreateAccount, "Passwords do not match", Toast.LENGTH_SHORT).show()
            confirmPasswordInput.requestFocus()
            return false
        }
        
        return true
    }
    
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
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
    
    private fun processSignUp(fullName: String, email: String, password: String) {
        try {
            Log.d(TAG, "Processing signup for user: $fullName")
            signUpButton.isEnabled = false
            val retrofit = RetrofitClient.getClient()
            val api = retrofit.create(ApiService::class.java)
            val request = SignupRequest(username = fullName, email = email, password = password)

            api.register(request).enqueue(object : Callback<SignupResponse> {
                override fun onResponse(
                    call: Call<SignupResponse>,
                    response: Response<SignupResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@CreateAccount, body?.message ?: "Signed up successfully", Toast.LENGTH_SHORT).show()
                        
                        // Store auth token if provided by backend
                        body?.token?.let { token ->
                            val sessionStored = com.simats.echohealth.auth.AuthManager.storeUserSession(
                                context = this@CreateAccount,
                                token = token,
                                userId = null,
                                email = email
                            )
                            if (sessionStored) {
                                Log.d(TAG, "✅ User session stored after registration")
                            } else {
                                Log.e(TAG, "❌ Failed to store user session after registration")
                            }
                        } ?: run {
                            // No token from signup → auto-login with same credentials
                            Log.d(TAG, "No token in signup response; attempting auto-login")
                            autoLoginAndProceed(email, password, fullName)
                            return
                        }
                        
                        saveUserDataLocally(fullName, email)
                        val intent = Intent(this@CreateAccount, Dashboard::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = "Signup failed: ${response.code()} ${errorBody ?: ""}"
                        Log.w(TAG, errorMsg)
                        Toast.makeText(this@CreateAccount, errorMsg.trim(), Toast.LENGTH_LONG).show()
                    }
                    signUpButton.isEnabled = true
                }

                override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                    Log.e(TAG, "Signup API failure", t)
                    Toast.makeText(this@CreateAccount, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                    signUpButton.isEnabled = true
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Signup exception", e)
            Toast.makeText(this@CreateAccount, "Signup Error: ${e.message}", Toast.LENGTH_SHORT).show()
            signUpButton.isEnabled = true
        }
    }
    
    private fun autoLoginAndProceed(email: String, password: String, fullName: String) {
        try {
            val retrofit = RetrofitClient.getClient()
            val api = retrofit.create(ApiService::class.java)
            val loginReq = LoginRequest(email = email, password = password)

            api.login(loginReq).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        var authToken: String? = body?.token
                        if (authToken.isNullOrBlank()) {
                            val authHeader = response.headers()["Authorization"]
                            authToken = authHeader
                                ?.removePrefix("Bearer ")
                                ?.removePrefix("Token ")
                                ?.trim()
                                ?.takeIf { !it.isNullOrBlank() }
                        }

                        if (!authToken.isNullOrBlank()) {
                            val stored = AuthManager.storeUserSession(
                                context = this@CreateAccount,
                                token = authToken!!,
                                userId = null,
                                email = email
                            )
                            if (!stored) {
                                Log.e(TAG, "❌ Auto-login: failed to store session")
                            }
                            saveUserDataLocally(fullName, email)
                            val intent = Intent(this@CreateAccount, Dashboard::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@CreateAccount, "Auto-login failed: missing token", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@CreateAccount, "Auto-login failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                    signUpButton.isEnabled = true
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e(TAG, "Auto-login API failure", t)
                    Toast.makeText(this@CreateAccount, "Auto-login error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                    signUpButton.isEnabled = true
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Auto-login exception", e)
            Toast.makeText(this@CreateAccount, "Auto-login exception: ${e.message}", Toast.LENGTH_LONG).show()
            signUpButton.isEnabled = true
        }
    }

    private fun saveUserDataLocally(fullName: String, email: String) {
        try {
            // Save to SharedPreferences (you can implement your own Session management)
            val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("user_full_name", fullName)
                putString("user_email", email)
                putBoolean("is_logged_in", true)
                apply()
            }
            Log.d(TAG, "User data saved locally - Full Name: $fullName, Email: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data locally", e)
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this@CreateAccount, LoginpageActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToTerms() {
        val intent = Intent(this@CreateAccount, Terms::class.java)
        startActivity(intent)
    }
}