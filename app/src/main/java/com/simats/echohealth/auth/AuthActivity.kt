package com.simats.echohealth.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.simats.echohealth.LoginpageActivity
import com.simats.echohealth.Dashboard

/**
 * Authentication Activity
 * Handles app launch behavior and authentication flow
 */
class AuthActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("AuthActivity", "🚀 App launched - checking authentication status")
        
        // Initialize AuthManager
        AuthManager.initialize(this)
        
        // Check if user is logged in
        if (AuthManager.isUserLoggedIn(this)) {
            Log.d("AuthActivity", "✅ User is logged in - navigating to Dashboard")
            navigateToDashboard()
        } else {
            Log.d("AuthActivity", "❌ User not logged in - navigating to Login")
            navigateToLogin()
        }
    }
    
    private fun navigateToDashboard() {
        try {
            val userInfo = AuthManager.getUserInfo(this)
            Log.d("AuthActivity", "👤 User info: ${userInfo?.email}")
            
            val intent = Intent(this, Dashboard::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("AuthActivity", "❌ Error navigating to dashboard: ${e.message}")
            navigateToLogin()
        }
    }
    
    private fun navigateToLogin() {
        try {
            val intent = Intent(this, LoginpageActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("AuthActivity", "❌ Error navigating to login: ${e.message}")
            finish()
        }
    }
}
