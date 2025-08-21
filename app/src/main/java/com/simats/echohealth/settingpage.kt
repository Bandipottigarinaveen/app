package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

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
        
        // Google Fit Integration
        val googleFitLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToGoogleFit)
        googleFitLayout?.setOnClickListener {
            showFeatureInfo(getString(R.string.google_fit_integration), getString(R.string.google_fit_info))
        }
        
        // EHR Synchronization
        val ehrLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToEHR)
        ehrLayout?.setOnClickListener {
            showFeatureInfo(getString(R.string.ehr_synchronization), getString(R.string.ehr_info))
        }
        
        // HIPAA Compliance
        val hipaaLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToHIPAA)
        hipaaLayout?.setOnClickListener {
            showFeatureInfo(getString(R.string.hipaa_compliant), getString(R.string.hipaa_info))
        }
        
        // Data Export
        val dataExportLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToDataExport)
        dataExportLayout?.setOnClickListener {
            showFeatureInfo(getString(R.string.data_export), getString(R.string.data_export_info))
        }
        
        // Health Alerts
        val healthAlertsLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToHealthAlerts)
        healthAlertsLayout?.setOnClickListener {
            showFeatureInfo(getString(R.string.health_alerts), getString(R.string.health_alerts_config))
        }
        
        // Reminders
        val remindersLayout = findViewById<LinearLayout>(R.id.btnNavigateSettingsToReminders)
        remindersLayout?.setOnClickListener {
            showFeatureInfo(getString(R.string.reminders), getString(R.string.reminders_config))
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
        val switchGoogleFit = findViewById<android.widget.Switch>(R.id.switchGoogleFit)
        switchGoogleFit?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showFeatureInfo(getString(R.string.google_fit_connected), getString(R.string.google_fit_info))
            }
        }
        
        val switchEHR = findViewById<android.widget.Switch>(R.id.switchEHR)
        switchEHR?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showFeatureInfo(getString(R.string.ehr_connected), getString(R.string.ehr_info))
            }
        }
        
        val switchHealthAlerts = findViewById<android.widget.Switch>(R.id.switchHealthAlerts)
        switchHealthAlerts?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showFeatureInfo(getString(R.string.health_alerts_enabled), getString(R.string.health_alerts_info))
            }
        }
        
        val switchReminders = findViewById<android.widget.Switch>(R.id.switchReminders)
        switchReminders?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showFeatureInfo(getString(R.string.reminders_enabled), getString(R.string.reminders_info))
            }
        }
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
        // Clear user session data here
        // For now, just navigate back to login
        val intent = Intent(this, LoginpageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}