package com.simats.echohealth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class EditProfile : AppCompatActivity() {
    
    companion object {
        private const val TAG = "EditProfile"
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val STORAGE_PERMISSION_REQUEST = 101
    }
    
    // UI Elements
    private lateinit var backArrow: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var profilePictureContainer: ImageView
    private lateinit var fullNameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var dobEdit: EditText
    private lateinit var usernameEdit: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var cancelText: TextView
    
    // Photo selection result launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                profileImage.setImageBitmap(imageBitmap)
                Toast.makeText(this, "Photo captured successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    profileImage.setImageBitmap(bitmap)
                    Toast.makeText(this, "Photo selected successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image from gallery: ${e.message}")
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "EditProfile activity created")
        
        try {
        enableEdgeToEdge()
        setContentView(R.layout.activity_editprofie)
            Log.d(TAG, "EditProfile layout set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate setup: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
        }
        
        initializeViews()
        setupNavigation()
        setupPhotoSelection()
        setupFormFunctionality()
        loadCurrentProfileData()
        
        Log.d(TAG, "EditProfile onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d(TAG, "Initializing views")
            
            backArrow = findViewById(R.id.backArrow)
            profileImage = findViewById(R.id.profileImage)
            profilePictureContainer = findViewById(R.id.profilePictureContainer)
            fullNameEdit = findViewById(R.id.fullName)
            emailEdit = findViewById(R.id.email)
            phoneEdit = findViewById(R.id.phone)
            dobEdit = findViewById(R.id.dob)
            usernameEdit = findViewById(R.id.username)
            changePasswordButton = findViewById(R.id.changePassword)
            saveChangesButton = findViewById(R.id.saveChanges)
            cancelText = findViewById(R.id.cancel)
            
            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupNavigation() {
        try {
            Log.d(TAG, "Setting up navigation")
            
            // Back Arrow
            backArrow.setOnClickListener {
                Log.d(TAG, "Back arrow clicked")
                onBackPressed()
            }
            
            // Cancel Text
            cancelText.setOnClickListener {
                Log.d(TAG, "Cancel clicked")
                onBackPressed()
            }
            
            Log.d(TAG, "Navigation setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupPhotoSelection() {
        try {
            Log.d(TAG, "Setting up photo selection")
            
            // Profile Picture Container Click
            profilePictureContainer.setOnClickListener {
                Log.d(TAG, "Profile picture container clicked")
                showPhotoSelectionDialog()
            }
            
            // Profile Image Click (alternative)
            profileImage.setOnClickListener {
                Log.d(TAG, "Profile image clicked")
                showPhotoSelectionDialog()
            }
            
            Log.d(TAG, "Photo selection setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up photo selection: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun showPhotoSelectionDialog() {
        try {
            Log.d(TAG, "Showing photo selection dialog")
            
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Photo")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Log.d(TAG, "User selected Take Photo")
                        checkCameraPermissionAndOpenCamera()
                    }
                    1 -> {
                        Log.d(TAG, "User selected Choose from Gallery")
                        checkStoragePermissionAndOpenGallery()
                    }
                    2 -> {
                        Log.d(TAG, "User cancelled photo selection")
                    }
                }
            }
            builder.show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing photo selection dialog: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun checkCameraPermissionAndOpenCamera() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking camera permission: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun checkStoragePermissionAndOpenGallery() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking storage permission: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun openCamera() {
        try {
            Log.d(TAG, "Opening camera")
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openGallery() {
        try {
            Log.d(TAG, "Opening gallery")
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery: ${e.message}")
            Toast.makeText(this, "Error opening gallery", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Camera permission granted")
                    openCamera()
                } else {
                    Log.d(TAG, "Camera permission denied")
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Storage permission granted")
                    openGallery()
                } else {
                    Log.d(TAG, "Storage permission denied")
                    Toast.makeText(this, "Storage permission is required to select photos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupFormFunctionality() {
        try {
            Log.d(TAG, "Setting up form functionality")
            
            // Change Password Button
            changePasswordButton.setOnClickListener {
                Log.d(TAG, "Change password clicked")
                Toast.makeText(this, "Change password functionality coming soon!", Toast.LENGTH_SHORT).show()
            }
            
            // Save Changes Button
            saveChangesButton.setOnClickListener {
                Log.d(TAG, "Save changes clicked")
                saveProfileChanges()
            }
            
            Log.d(TAG, "Form functionality setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up form functionality: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun loadCurrentProfileData() {
        try {
            Log.d(TAG, "Loading current profile data")
            
            // Load current profile data from SharedPreferences
            val sharedPref = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
            val fullName = sharedPref.getString("full_name", "Sarah Johnson") ?: "Sarah Johnson"
            val email = sharedPref.getString("email", "sarah.johnson@example.com") ?: "sarah.johnson@example.com"
            val phone = sharedPref.getString("phone", "+1 (555) 123-4567") ?: "+1 (555) 123-4567"
            val dob = sharedPref.getString("dob", "05/12/1990") ?: "05/12/1990"
            val username = sharedPref.getString("username", "sarahjohnson") ?: "sarahjohnson"
            
            // Set form data
            fullNameEdit.setText(fullName)
            emailEdit.setText(email)
            phoneEdit.setText(phone)
            dobEdit.setText(dob)
            usernameEdit.setText(username)
            
            Log.d(TAG, "Current profile data loaded: $fullName, $email, $phone, $dob, $username")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading current profile data: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun saveProfileChanges() {
        try {
            Log.d(TAG, "Saving profile changes")
            
            // Get form data
            val fullName = fullNameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val dob = dobEdit.text.toString().trim()
            val username = usernameEdit.text.toString().trim()
            
            // Validate form data
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Save profile data to SharedPreferences or other storage
            saveProfileData(fullName, email, phone, dob, username)
            
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            
            // Navigate back to profile
            onBackPressed()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile changes: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error saving profile changes", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveProfileData(fullName: String, email: String, phone: String, dob: String, username: String) {
        try {
            Log.d(TAG, "Saving profile data: $fullName, $email, $phone, $dob, $username")
            
            // Save to SharedPreferences
            val sharedPref = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("full_name", fullName)
                putString("email", email)
                putString("phone", phone)
                putString("dob", dob)
                putString("username", username)
                apply()
            }
            
            Log.d(TAG, "Profile data saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile data: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override fun onBackPressed() {
        Log.d(TAG, "Back button pressed")
        // Navigate back to profile
        try {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating back to profile: ${e.message}")
            e.printStackTrace()
            super.onBackPressed()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "EditProfile onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "EditProfile onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "EditProfile onDestroy called")
    }
}