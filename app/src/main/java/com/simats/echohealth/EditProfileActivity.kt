package com.simats.echohealth

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.simats.echohealth.utils.ProfileManager

class EditProfileActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "EditProfile"
    }
    
    // UI elements
    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etDOB: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnBack: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_profile)
        
        Log.d(TAG, "EditProfileActivity created")
        
        initializeViews()
        setupClickListeners()
        loadCurrentProfileData()
    }
    
    private fun initializeViews() {
        try {
            etFullName = findViewById(R.id.et_full_name)
            etUsername = findViewById(R.id.et_username)
            etEmail = findViewById(R.id.et_email)
            etDOB = findViewById(R.id.et_dob)
            etPhone = findViewById(R.id.et_phone)
            btnSaveProfile = findViewById(R.id.btn_save_profile)
            btnBack = findViewById(R.id.btn_back)
            
            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupClickListeners() {
        try {
            // Back button
            btnBack.setOnClickListener {
                Log.d(TAG, "Back button clicked")
                finish()
            }
            
            // Save profile button
            btnSaveProfile.setOnClickListener {
                Log.d(TAG, "Save profile button clicked")
                saveProfile()
            }
            
            Log.d(TAG, "Click listeners setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun loadCurrentProfileData() {
        try {
            val local = ProfileManager.getProfileData(this)
            if (local.fullName.isNotEmpty()) etFullName.setText(local.fullName)
            if (local.username.isNotEmpty()) etUsername.setText(local.username)
            if (local.email.isNotEmpty()) etEmail.setText(local.email)
            if (local.dateOfBirth.isNotEmpty()) etDOB.setText(local.dateOfBirth)
            if (local.phoneNumber.isNotEmpty()) etPhone.setText(local.phoneNumber)
            Log.d(TAG, "Loaded profile from ProfileManager")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading current profile data: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun saveProfile() {
        try {
            // Get values from input fields
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val dob = etDOB.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            
            // Basic validation
            if (fullName.isEmpty()) {
                etFullName.error = "Full name is required"
                return
            }
            
            if (username.isEmpty()) {
                etUsername.error = "Username is required"
                return
            }
            
            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return
            }
            
            if (dob.isEmpty()) {
                etDOB.error = "Date of birth is required"
                return
            }
            
            if (phone.isEmpty()) {
                etPhone.error = "Phone number is required"
                return
            }
            
            // Save profile data locally via ProfileManager
            ProfileManager.saveProfileData(
                context = this,
                fullName = fullName,
                username = username,
                email = email,
                dateOfBirth = dob,
                phoneNumber = phone
            )
            
            // Show success message
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_LONG).show()
            
            // Close the activity and return to profile
            finish()
            
            Log.d(TAG, "Profile saved successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveProfileData(fullName: String, username: String, email: String, dob: String, phone: String) {}
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "EditProfileActivity onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "EditProfileActivity onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "EditProfileActivity onDestroy called")
    }
}
