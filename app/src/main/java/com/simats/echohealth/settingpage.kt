package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.simats.echohealth.auth.AuthManager

class SettingPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupBackNavigation()
        setupNavigation()
        setupLogout()
        setupSwitches()
    }
    
    private fun setupBackNavigation() {
        val backArrow = findViewById<android.widget.ImageView>(R.id.btnBackFromSettings)
        backArrow?.setOnClickListener {
            finish()
        }
    }
    
    private fun setupNavigation() {
        // Edit Profile
        val editProfileLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToEditProfile)
        editProfileLayout?.setOnClickListener {
            try {
                android.util.Log.d("Settings", "Navigating to Edit Profile")
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
                android.util.Log.d("Settings", "Successfully navigated to Edit Profile")
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Error navigating to Edit Profile: ${e.message}")
                showNavigationError()
            }
        }
        
        
        
        
        // Terms of Service navigation
        val termsLayout = findViewById<LinearLayout>(R.id.btn_terms)
        termsLayout?.setOnClickListener {
            try {
                android.util.Log.d("Settings", "Navigating to Terms of Service")
                val intent = Intent(this, Terms::class.java)
                startActivity(intent)
                android.util.Log.d("Settings", "Successfully navigated to Terms")
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Error navigating to Terms: ${e.message}")
                showNavigationError()
            }
        }
        
        // Privacy Policy navigation
        val privacyPolicyLayout = findViewById<LinearLayout>(R.id.btn_privacy)
        privacyPolicyLayout?.setOnClickListener {
            try {
                android.util.Log.d("Settings", "Navigating to Privacy Policy")
                val intent = Intent(this, PrivacyPolicy::class.java)
                startActivity(intent)
                android.util.Log.d("Settings", "Successfully navigated to Privacy Policy")
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Error navigating to Privacy Policy: ${e.message}")
                showNavigationError()
            }
        }
        
        // Help & Support
        val helpSupportLayout = findViewById<LinearLayout>(R.id.btn_help)
        helpSupportLayout?.setOnClickListener {
            try {
                android.util.Log.d("Settings", "Navigating to Help & Support")
                val intent = Intent(this, HelpSupport::class.java)
                startActivity(intent)
                android.util.Log.d("Settings", "Successfully navigated to Help & Support")
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Error navigating to Help & Support: ${e.message}")
                showNavigationError()
            }
        }
        
        // About App navigation
        val aboutAppLayout = findViewById<LinearLayout>(R.id.btn_about)
        aboutAppLayout?.setOnClickListener {
            try {
                android.util.Log.d("Settings", "Navigating to About App")
                val intent = Intent(this, AboutApp::class.java)
                startActivity(intent)
                android.util.Log.d("Settings", "Successfully navigated to About App")
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Error navigating to About App: ${e.message}")
                showNavigationError()
            }
        }
    }
    
    private fun setupSwitches() {
        // No switches remaining after removing Health Data and Notifications sections
    }
    
    private fun showFeatureInfo(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    private fun showNavigationError() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings_navigation_error))
            .setMessage(getString(R.string.settings_navigation_error_message))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    private fun setupLogout() {
        val logoutLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToLogout)
        logoutLayout?.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.logout_confirm)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.logout_cancel), null)
            .show()
    }
    
    private fun performLogout() {
        try {
            android.util.Log.d("Settings", "🚪 Performing logout")
            
            // Clear user session using AuthManager
            val sessionCleared = AuthManager.clearUserSession(this)
            
            // Clear activity history from both SharedPreferences and local DB
            try {
                ActivityLogStore.clearAllActivities(this)
                ActivityDatabase.clear(this)
                android.util.Log.d("Settings", "✅ Activity history cleared from both sources")
            } catch (e: Exception) {
                android.util.Log.e("Settings", "❌ Failed to clear activity history: ${e.message}")
            }
            
            if (sessionCleared) {
                android.util.Log.d("Settings", "✅ User session cleared successfully")
                
                // Show logout confirmation
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                
                // Navigate to login screen
                val intent = Intent(this, LoginpageActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                finish()
            } else {
                android.util.Log.e("Settings", "❌ Failed to clear user session")
                Toast.makeText(this, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("Settings", "❌ Error during logout: ${e.message}")
            Toast.makeText(this, "Logout error. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}