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
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.simats.echohealth.utils.ProfileManager
import java.io.File
import java.io.FileOutputStream
import android.view.View
import android.view.ViewOutlineProvider
import android.graphics.Outline

class Profile : AppCompatActivity() {
    
    companion object {
        private const val TAG = "Profile"
        private const val CAMERA_PERMISSION_REQUEST = 200
        private const val STORAGE_PERMISSION_REQUEST = 201
    }
    
    // Profile data variables
    private var userName: String = "John Smith"
    private var userEmail: String = "johnsmith@email.com"
    private var userFullName: String = "John Smith"
    private var userPhone: String = "+1 (555) 123-4567"
    private var userDOB: String = "September 15, 1990"
    private var userUsername: String = "johnsmith"
    
    // UI refs
    private var profilePictureView: ImageView? = null
    
    // Selection state
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                profilePictureView?.setImageBitmap(imageBitmap)
                selectedImageFile = saveBitmapToFile(imageBitmap)
                // Persist photo path in local profile (store file path as photo)
                ProfileManager.saveProfileData(this, photo = selectedImageFile?.absolutePath)
                Toast.makeText(this, "Photo updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imageUri = data?.data
            if (imageUri != null) {
                try {
                    selectedImageUri = imageUri
                    profilePictureView?.setImageURI(imageUri)
                    // Persist uri string in local profile
                    ProfileManager.saveProfileData(this, photo = imageUri.toString())
                    Toast.makeText(this, "Photo updated", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image from gallery: ${e.message}")
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
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
            
            // Setup profile picture click to change photo inline
            profilePictureView = findViewById(R.id.profilePicture)
            profilePictureView?.let { makeCircular(it) }
            profilePictureView?.setOnClickListener {
                Log.d(TAG, "Profile picture clicked")
                showPhotoSelectionDialog()
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
            
            // Load profile data from ProfileManager
            val local = ProfileManager.getProfileData(this)
            userFullName = if (local.fullName.isNotEmpty()) local.fullName else "John Smith"
            userEmail = if (local.email.isNotEmpty()) local.email else "johnsmith@email.com"
            userPhone = if (local.phoneNumber.isNotEmpty()) local.phoneNumber else "+1 (555) 123-4567"
            userDOB = if (local.dateOfBirth.isNotEmpty()) local.dateOfBirth else "September 15, 1990"
            userUsername = if (local.username.isNotEmpty()) local.username else "johnsmith"
            // Load photo if available
            local.photo?.let { photoStr ->
                try {
                    if (photoStr.startsWith("content:") || photoStr.startsWith("file:")) {
                        profilePictureView?.setImageURI(Uri.parse(photoStr))
                    } else {
                        // assume it's a file path
                        profilePictureView?.setImageURI(Uri.fromFile(File(photoStr)))
                    }
                } catch (_: Exception) {}
            }
            
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

    private fun makeCircular(imageView: ImageView) {
        try {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val size = kotlin.math.min(view.width, view.height)
                    val left = (view.width - size) / 2
                    val top = (view.height - size) / 2
                    outline.setOval(left, top, left + size, top + size)
                }
            }
            imageView.post { imageView.invalidateOutline() }
        } catch (_: Exception) {}
    }

    private fun showPhotoSelectionDialog() {
        try {
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Change Profile Photo")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> checkStoragePermissionAndOpenGallery()
                    else -> {}
                }
            }
            builder.setCancelable(true)
            builder.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing photo selection: ${e.message}")
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
        }
    }

    private fun checkStoragePermissionAndOpenGallery() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                openGallery()
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking storage permission: ${e.message}")
        }
    }

    private fun openCamera() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
        }
    }

    private fun openGallery() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                try {
                    val photoPickerIntent = Intent(MediaStore.ACTION_PICK_IMAGES)
                    galleryLauncher.launch(photoPickerIntent)
                    return
                } catch (_: Exception) {}
            }
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        return try {
            val file = File(cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap: ${e.message}")
            null
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