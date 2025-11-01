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
import com.simats.echohealth.Retrofit.RetrofitClient
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Responses.ProfileRequest
import com.simats.echohealth.Responses.ProfileResponse
import com.simats.echohealth.auth.AuthManager
import com.simats.echohealth.utils.ProfileManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

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
    
    // API service
    private lateinit var apiService: ApiService
    
    // Photo handling
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null
    
    // Photo selection result launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Camera launcher result: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            Log.d(TAG, "Camera bitmap received: ${imageBitmap != null}")
            if (imageBitmap != null) {
                profileImage.setImageBitmap(imageBitmap)
                // Save bitmap to file for API upload
                selectedImageFile = saveBitmapToFile(imageBitmap)
                Log.d(TAG, "Camera image saved to file: ${selectedImageFile?.absolutePath}")
                Toast.makeText(this, "Photo captured successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Log.w(TAG, "No bitmap received from camera")
                Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Camera capture cancelled or failed")
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Gallery launcher result: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imageUri = data?.data
            Log.d(TAG, "Selected image URI: $imageUri")
            if (imageUri != null) {
                try {
                    selectedImageUri = imageUri
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    profileImage.setImageBitmap(bitmap)
                    // Convert URI to file for API upload
                    selectedImageFile = uriToFile(imageUri)
                    Log.d(TAG, "Image loaded successfully, file: ${selectedImageFile?.absolutePath}")
                    Toast.makeText(this, "Photo selected successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image from gallery: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w(TAG, "No image URI received from gallery")
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Gallery selection cancelled or failed")
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
        
        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService::class.java)
        
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
            
            // Camera Icon Click
            val cameraIcon = findViewById<ImageView>(R.id.cameraIcon)
            cameraIcon?.setOnClickListener {
                Log.d(TAG, "Camera icon clicked")
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
            builder.setTitle("Change Profile Photo")
            builder.setMessage("How would you like to update your profile photo?")
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
            builder.setCancelable(true)
            builder.show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing photo selection dialog: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error opening photo selection", Toast.LENGTH_SHORT).show()
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
            // For Android 13+ (API 33+), we don't need READ_EXTERNAL_STORAGE for gallery access
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
            
            // Try to use the new photo picker for Android 13+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                try {
                    val photoPickerIntent = Intent(MediaStore.ACTION_PICK_IMAGES)
                    galleryLauncher.launch(photoPickerIntent)
                    return
                } catch (e: Exception) {
                    Log.w(TAG, "Photo picker not available, falling back to gallery: ${e.message}")
                }
            }
            
            // Fallback to traditional gallery
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
            
            // Check if user is authenticated
            val authToken = AuthManager.getAuthToken(this)
            if (authToken != null) {
                // Load from API
                loadProfileFromAPI(authToken)
            } else {
                // Load from local storage as fallback
                loadProfileFromLocalStorage()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading current profile data: ${e.message}")
            e.printStackTrace()
            // Fallback to local storage
            loadProfileFromLocalStorage()
        }
    }
    
    private fun loadProfileFromAPI(authToken: String) {
        try {
            Log.d(TAG, "Loading profile from API")
            
            val call = apiService.getProfile("Bearer $authToken")
            call.enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                    try {
                        if (response.isSuccessful) {
                            val profileResponse = response.body()
                            if (profileResponse != null) {
                                Log.d(TAG, "Profile loaded from API successfully")
                                Log.d(TAG, "Profile data: $profileResponse")
                                
                                // Update UI with API data
                                updateUIWithProfileData(profileResponse)
                                
                                // Save to local storage for offline access
                                saveProfileDataLocally(profileResponse)
                            } else {
                                Log.e(TAG, "Empty profile response from API")
                                loadProfileFromLocalStorage()
                            }
                        } else {
                            Log.e(TAG, "Failed to load profile from API: ${response.code()}")
                            loadProfileFromLocalStorage()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing profile API response: ${e.message}")
                        loadProfileFromLocalStorage()
                    }
                }
                
                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e(TAG, "Profile API call failed: ${t.message}")
                    loadProfileFromLocalStorage()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error calling profile API: ${e.message}")
            loadProfileFromLocalStorage()
        }
    }
    
    private fun loadProfileFromLocalStorage() {
        try {
            Log.d(TAG, "Loading profile from local storage")
            
            // Load current profile data using ProfileManager
            val local = ProfileManager.getProfileData(this)
            val profileResponse = ProfileResponse(
                photo = local.photo,
                fullName = if (local.fullName.isNotEmpty()) local.fullName else "Sarah Johnson",
                username = if (local.username.isNotEmpty()) local.username else "sarahjohnson",
                email = if (local.email.isNotEmpty()) local.email else "sarah.johnson@example.com",
                dateOfBirth = if (local.dateOfBirth.isNotEmpty()) local.dateOfBirth else "05/12/1990",
                phoneNumber = if (local.phoneNumber.isNotEmpty()) local.phoneNumber else "+1 (555) 123-4567"
            )
            
            // Update UI with local data
            updateUIWithProfileData(profileResponse)
            
            Log.d(TAG, "Current profile data loaded from local storage: ${profileResponse.fullName}, ${profileResponse.email}, ${profileResponse.phoneNumber}, ${profileResponse.dateOfBirth}, ${profileResponse.username}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile from local storage: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun updateUIWithProfileData(profileResponse: ProfileResponse) {
        try {
            Log.d(TAG, "Updating UI with profile data")
            
            // Set form data
            fullNameEdit.setText(profileResponse.fullName)
            emailEdit.setText(profileResponse.email)
            phoneEdit.setText(profileResponse.phoneNumber)
            dobEdit.setText(profileResponse.dateOfBirth)
            usernameEdit.setText(profileResponse.username)
            
            // Load profile photo if available
            profileResponse.photo?.let { photoUrl ->
                Log.d(TAG, "Loading profile photo: $photoUrl")
                // You can add image loading logic here using Glide or Picasso
                // For now, we'll just log it
            }
            
            Log.d(TAG, "UI updated with profile data successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI with profile data: ${e.message}")
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
            
            if (dob.isEmpty()) {
                Toast.makeText(this, "Please enter your date of birth", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Check authentication
            val authToken = AuthManager.getAuthToken(this)
            if (authToken == null) {
                Toast.makeText(this, "Please login to update your profile", Toast.LENGTH_LONG).show()
                return
            }
            
            // Show loading state
            saveChangesButton.isEnabled = false
            saveChangesButton.text = "Saving..."
            
            // Create profile request
            val profileRequest = ProfileRequest(
                fullName = fullName,
                username = username,
                email = email,
                dateOfBirth = dob,
                phoneNumber = phone
            )
            
            // Call API to update profile
            updateProfileAPI(authToken, profileRequest)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile changes: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error saving profile changes", Toast.LENGTH_SHORT).show()
            resetSaveButton()
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
    
    private fun updateProfileAPI(authToken: String, profileRequest: ProfileRequest) {
        try {
            Log.d(TAG, "Calling update profile API")
            
            val call = apiService.updateProfile("Bearer $authToken", profileRequest)
            call.enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                    try {
                        if (response.isSuccessful) {
                            val profileResponse = response.body()
                            if (profileResponse != null) {
                                Log.d(TAG, "Profile updated successfully")
                                Log.d(TAG, "Response: $profileResponse")
                                
                                // Save to local storage
                                saveProfileDataLocally(profileResponse)
                                
                                // Upload photo if selected
                                if (selectedImageFile != null) {
                                    uploadProfilePhoto(authToken)
                                } else {
                                    // Navigate back if no photo to upload
                                    Toast.makeText(this@EditProfile, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                    onBackPressed()
                                }
                            } else {
                                Log.e(TAG, "Empty response body")
                                Toast.makeText(this@EditProfile, "Profile update failed", Toast.LENGTH_SHORT).show()
                                resetSaveButton()
                            }
                        } else {
                            Log.e(TAG, "Profile update failed with code: ${response.code()}")
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Error body: $errorBody")
                            
                            val errorMessage = when (response.code()) {
                                400 -> "Invalid profile data. Please check your information."
                                401 -> "Authentication failed. Please login again."
                                403 -> "Access denied. Please check your permissions."
                                500 -> "Server error. Please try again later."
                                else -> "Profile update failed. Please try again."
                            }
                            
                            Toast.makeText(this@EditProfile, errorMessage, Toast.LENGTH_LONG).show()
                            resetSaveButton()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing profile update response: ${e.message}")
                        Toast.makeText(this@EditProfile, "Error processing response", Toast.LENGTH_SHORT).show()
                        resetSaveButton()
                    }
                }
                
                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e(TAG, "Profile update API call failed: ${t.message}")
                    Toast.makeText(this@EditProfile, "Network error. Please check your connection.", Toast.LENGTH_LONG).show()
                    resetSaveButton()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error calling update profile API: ${e.message}")
            Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
            resetSaveButton()
        }
    }
    
    private fun uploadProfilePhoto(authToken: String) {
        try {
            Log.d(TAG, "Uploading profile photo")
            
            if (selectedImageFile == null || !selectedImageFile!!.exists()) {
                Log.e(TAG, "No valid image file for upload")
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                onBackPressed()
                return
            }
            
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val requestFile = selectedImageFile!!.asRequestBody(mediaType)
            val photoPart = MultipartBody.Part.createFormData("photo", "profile_photo.jpg", requestFile)
            
            val call = apiService.uploadProfilePhoto("Bearer $authToken", photoPart)
            call.enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                    try {
                        if (response.isSuccessful) {
                            val profileResponse = response.body()
                            if (profileResponse != null) {
                                Log.d(TAG, "Profile photo uploaded successfully")
                                Log.d(TAG, "Photo URL: ${profileResponse.photo}")
                                
                                // Update local storage with photo URL
                                ProfileManager.saveProfileData(
                                    context = this@EditProfile,
                                    photo = profileResponse.photo
                                )
                            }
                        } else {
                            Log.e(TAG, "Photo upload failed with code: ${response.code()}")
                        }
                        
                        // Navigate back regardless of photo upload result
                        Toast.makeText(this@EditProfile, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing photo upload response: ${e.message}")
                        Toast.makeText(this@EditProfile, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                }
                
                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e(TAG, "Photo upload API call failed: ${t.message}")
                    Toast.makeText(this@EditProfile, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile photo: ${e.message}")
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
    }
    
    private fun saveProfileDataLocally(profileResponse: ProfileResponse) {
        try {
            Log.d(TAG, "Saving profile data locally")
            
            ProfileManager.saveProfileData(
                context = this,
                photo = profileResponse.photo,
                fullName = profileResponse.fullName,
                username = profileResponse.username,
                email = profileResponse.email,
                dateOfBirth = profileResponse.dateOfBirth,
                phoneNumber = profileResponse.phoneNumber
            )
            
            Log.d(TAG, "Profile data saved locally successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile data locally: ${e.message}")
        }
    }
    
    private fun resetSaveButton() {
        saveChangesButton.isEnabled = true
        saveChangesButton.text = "Save Changes"
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        return try {
            val file = File(cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d(TAG, "Bitmap saved to file: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to file: ${e.message}")
            null
        }
    }
    
    private fun uriToFile(uri: Uri): File? {
        return try {
            Log.d(TAG, "Converting URI to File: $uri")
            
            when (uri.scheme) {
                "file" -> {
                    val file = File(uri.path!!)
                    Log.d(TAG, "File URI - path: ${file.absolutePath}")
                    file
                }
                "content" -> {
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        Log.e(TAG, "Failed to open input stream for content URI")
                        return null
                    }
                    
                    val file = File(cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
                    inputStream.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    Log.d(TAG, "File created successfully - size: ${file.length()} bytes")
                    file
                }
                else -> {
                    Log.e(TAG, "Unsupported URI scheme: ${uri.scheme}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to File: ${e.message}")
            null
        }
    }
}