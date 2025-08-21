package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class Profile : AppCompatActivity() {
    
    companion object {
        private const val TAG = "Profile"
    }
    
    // Profile data variables
    private var userName: String = "John Smith"
    private var userEmail: String = "johnsmith@email.com"
    private var userFullName: String = "John Smith"
    private var userPhone: String = "+1 (555) 123-4567"
    private var userDOB: String = "September 15, 1990"
    private var userUsername: String = "johnsmith"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Profile activity created")
        
        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_profile)
            Log.d(TAG, "Profile layout set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate setup: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
        }
        
        setupNavigation()
        setupProfileFunctionality()
        loadProfileData()
        
        Log.d(TAG, "Profile onCreate completed successfully")
    }
    
    private fun setupNavigation() {
        try {
            Log.d(TAG, "Setting up navigation")
            
            // Back Arrow
            val backArrow = findViewById<ImageView>(R.id.backArrow)
            if (backArrow != null) {
                backArrow.setOnClickListener {
                    Log.d(TAG, "Back arrow clicked")
                    onBackPressed()
                }
            }
            
            // Edit Profile Button
            val editProfileButton = findViewById<MaterialButton>(R.id.btn_edit_profile)
            if (editProfileButton != null) {
                editProfileButton.setOnClickListener {
                    Log.d(TAG, "Edit Profile button clicked")
                    try {
                        val intent = Intent(this, EditProfileActivity::class.java)
                        startActivity(intent)
                        Log.d(TAG, "Successfully navigated to edit profile")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigating to edit profile: ${e.message}")
                        Toast.makeText(this, "Error opening edit profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            Log.d(TAG, "Navigation setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupProfileFunctionality() {
        try {
            Log.d(TAG, "Setting up profile functionality")
            
            // Setup profile picture click (for future enhancement)
            val profilePicture = findViewById<ImageView>(R.id.profilePicture)
            if (profilePicture != null) {
                profilePicture.setOnClickListener {
                    Log.d(TAG, "Profile picture clicked")
                    Toast.makeText(this, "Profile picture change coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
            
            Log.d(TAG, "Profile functionality setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up profile functionality: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun loadProfileData() {
        try {
            Log.d(TAG, "Loading profile data")
            
            // Load profile data from SharedPreferences
            val sharedPref = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
            userFullName = sharedPref.getString("full_name", "John Smith") ?: "John Smith"
            userEmail = sharedPref.getString("email", "johnsmith@email.com") ?: "johnsmith@email.com"
            userPhone = sharedPref.getString("phone", "+1 (555) 123-4567") ?: "+1 (555) 123-4567"
            userDOB = sharedPref.getString("dob", "September 15, 1990") ?: "September 15, 1990"
            userUsername = sharedPref.getString("username", "johnsmith") ?: "johnsmith"
            
            Log.d(TAG, "Profile data loaded: $userName, $userEmail, $userFullName, $userPhone, $userDOB, $userUsername")
            
            // Update UI with profile data
            updateProfileUI()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile data: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun updateProfileUI() {
        try {
            Log.d(TAG, "Updating profile UI")
            
            // Update full name
            val fullNameTextView = findViewById<TextView>(R.id.userFullName)
            if (fullNameTextView != null) {
                fullNameTextView.text = userFullName
            }
            
            // Update email
            val emailTextView = findViewById<TextView>(R.id.userEmail)
            if (emailTextView != null) {
                emailTextView.text = userEmail
            }
            
            // Update phone
            val phoneTextView = findViewById<TextView>(R.id.userPhone)
            if (phoneTextView != null) {
                phoneTextView.text = userPhone
            }
            
            // Update DOB
            val dobTextView = findViewById<TextView>(R.id.userDOB)
            if (dobTextView != null) {
                dobTextView.text = userDOB
            }
            
            // Update username
            val usernameTextView = findViewById<TextView>(R.id.userUsername)
            if (usernameTextView != null) {
                usernameTextView.text = userUsername
            }
            
            Log.d(TAG, "Profile UI updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile UI: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Profile onResume called")
        
        // Refresh profile data when returning to this activity
        loadProfileData()
    }
    
    override fun onBackPressed() {
        Log.d(TAG, "Back button pressed")
        // Navigate back to dashboard
        try {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating back to dashboard: ${e.message}")
            e.printStackTrace()
            super.onBackPressed()
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Profile onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Profile onDestroy called")
    }
}