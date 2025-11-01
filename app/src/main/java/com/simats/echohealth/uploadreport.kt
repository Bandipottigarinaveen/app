package com.simats.echohealth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.cardview.widget.CardView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UploadReport : AppCompatActivity() {
    
    private lateinit var backButton: ImageView
    private lateinit var takePhotoButton: CardView
    private lateinit var chooseFileButton: CardView
    private lateinit var continueButton: Button
    private lateinit var reportPreviewText: TextView
    private lateinit var imagePreview: ImageView
    
    private var selectedImageUri: Uri? = null
    private var selectedFilePath: String? = null
    
    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1003
        private const val MEDIA_PERMISSION_REQUEST_CODE = 1004
    }
    
    // Camera launcher removed - using simple onActivityResult instead
    
    // File picker result launcher
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedUri ->
            selectedImageUri = selectedUri
            // For content URIs, do NOT treat the URI string as a filesystem path
            selectedFilePath = null
            displayImagePreview(selectedUri)
            updateReportPreview("File selected successfully")
            enableContinueButton()
            Log.d("UploadReport", "File selected URI: ${selectedUri}")
        } ?: run {
            Log.d("UploadReport", "No file selected")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("UploadReport", "UploadReport onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d("UploadReport", "Edge to edge enabled")
            
            setContentView(R.layout.activity_uploadreport)
            Log.d("UploadReport", "UploadReport layout set successfully")
            
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            Log.d("UploadReport", "Window insets setup successful")
        } catch (e: Exception) {
            Log.e("UploadReport", "Error in onCreate setup: ${e.message}")
            Log.e("UploadReport", "Stack trace: ${e.stackTraceToString()}")
        }

        // Initialize UI elements
        initializeViews()
        
        // Setup functionality
        setupBackNavigation()
        setupTakePhotoButton()
        setupChooseFileButton()
        setupContinueButton()
        
        Log.d("UploadReport", "UploadReport onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d("UploadReport", "Initializing views...")
            
            backButton = findViewById<ImageView>(R.id.backButton)
            takePhotoButton = findViewById<CardView>(R.id.takePhotoButton)
            chooseFileButton = findViewById<CardView>(R.id.chooseFileButton)
            continueButton = findViewById<Button>(R.id.continueButton)
            reportPreviewText = findViewById<TextView>(R.id.reportPreviewText)
            imagePreview = findViewById<ImageView>(R.id.imagePreview)
            
            Log.d("UploadReport", "Views initialized successfully")
            Log.d("UploadReport", "Back button: ${if (backButton != null) "FOUND" else "NOT FOUND"}")
            Log.d("UploadReport", "Take photo button: ${if (takePhotoButton != null) "FOUND" else "NOT FOUND"}")
            Log.d("UploadReport", "Choose file button: ${if (chooseFileButton != null) "FOUND" else "NOT FOUND"}")
            Log.d("UploadReport", "Continue button: ${if (continueButton != null) "FOUND" else "NOT FOUND"}")
            
        } catch (e: Exception) {
            Log.e("UploadReport", "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupBackNavigation() {
        try {
            Log.d("UploadReport", "Setting up back navigation")
            
            if (backButton != null) {
                backButton.setOnClickListener {
                    Log.d("UploadReport", "Back button clicked - finishing activity")
                    finish()
                }
                
                Log.d("UploadReport", "Back navigation setup successful")
            } else {
                Log.e("UploadReport", "Back button not found")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error setting up back navigation: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupTakePhotoButton() {
        try {
            Log.d("UploadReport", "Setting up take photo button")
            
            if (takePhotoButton != null) {
                takePhotoButton.setOnClickListener {
                    Log.d("UploadReport", "Take photo button clicked")
                    checkCameraPermissionAndTakePhoto()
                }
                
                Log.d("UploadReport", "Take photo button setup successful")
            } else {
                Log.e("UploadReport", "Take photo button not found")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error setting up take photo button: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupChooseFileButton() {
        try {
            Log.d("UploadReport", "Setting up choose file button")
            
            if (chooseFileButton != null) {
                chooseFileButton.setOnClickListener {
                    Log.d("UploadReport", "Choose file button clicked")
                    checkStoragePermissionAndPickFile()
                }
                
                Log.d("UploadReport", "Choose file button setup successful")
            } else {
                Log.e("UploadReport", "Choose file button not found")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error setting up choose file button: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupContinueButton() {
        try {
            Log.d("UploadReport", "Setting up continue button")
            
            if (continueButton != null) {
                continueButton.setOnClickListener {
                    Log.d("UploadReport", "Continue button clicked")
                    processUpload()
                }
                
                // Initially disable continue button
                continueButton.isEnabled = false
                continueButton.alpha = 0.5f
                
                // Keep disabled until a file is chosen/captured
                
                Log.d("UploadReport", "Continue button setup successful")
                Log.d("UploadReport", "Continue button enabled: ${continueButton.isEnabled}")
                Log.d("UploadReport", "Continue button alpha: ${continueButton.alpha}")
            } else {
                Log.e("UploadReport", "Continue button not found")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error setting up continue button: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun checkCameraPermissionAndTakePhoto() {
        try {
            // Check if camera permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d("UploadReport", "Camera permission already granted")
                takePhoto()
            } else {
                Log.d("UploadReport", "Requesting camera permission")
                // Request camera permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error checking camera permission: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun checkStoragePermissionAndPickFile() {
        try {
            // For Android 13+ (API 33+), use media permissions
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("UploadReport", "Media permission already granted")
                    pickFile()
                } else {
                    Log.d("UploadReport", "Requesting media permission")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), MEDIA_PERMISSION_REQUEST_CODE)
                }
            } else {
                // For older Android versions, use storage permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("UploadReport", "Storage permission already granted")
                    pickFile()
                } else {
                    Log.d("UploadReport", "Requesting storage permission")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
                }
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error checking storage permission: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun takePhoto() {
        try {
            Log.d("UploadReport", "Taking photo...")
            
            // Use simple MediaStore camera intent
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            
            // Check if camera app is available
            if (intent.resolveActivity(packageManager) != null) {
                Log.d("UploadReport", "Camera app found, launching...")
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Log.e("UploadReport", "No camera app found")
                Toast.makeText(this, "No camera app found on device", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Log.e("UploadReport", "Error taking photo: ${e.message}")
            Log.e("UploadReport", "Stack trace: ${e.stackTraceToString()}")
            e.printStackTrace()
            Toast.makeText(this, "Error opening camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun pickFile() {
        try {
            Log.d("UploadReport", "Picking file...")
            
            // Use MIME type that supports images and PDFs
            filePickerLauncher.launch("*/*")
            
            Log.d("UploadReport", "File picker launched successfully")
        } catch (e: Exception) {
            Log.e("UploadReport", "Error picking file: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error opening file picker", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun displayImagePreview(uri: Uri) {
        try {
            if (imagePreview != null) {
                imagePreview.setImageURI(uri)
                imagePreview.visibility = View.VISIBLE
                reportPreviewText.visibility = View.GONE
                Log.d("UploadReport", "Image preview displayed: $uri")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error displaying image preview: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun updateReportPreview(message: String) {
        try {
            if (reportPreviewText != null) {
                reportPreviewText.text = message
                Log.d("UploadReport", "Report preview updated: $message")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error updating report preview: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun enableContinueButton() {
        try {
            if (continueButton != null) {
                continueButton.isEnabled = true
                continueButton.alpha = 1.0f
                Log.d("UploadReport", "Continue button enabled")
                Log.d("UploadReport", "Continue button enabled state: ${continueButton.isEnabled}")
                Log.d("UploadReport", "Continue button alpha: ${continueButton.alpha}")
            } else {
                Log.e("UploadReport", "Continue button is null in enableContinueButton")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error enabling continue button: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun processUpload() {
        try {
            Log.d("UploadReport", "Processing upload...")
            
            if (selectedImageUri != null || (selectedFilePath != null && selectedFilePath!!.startsWith("/"))) {
                // Here you would typically upload the file to your server
                Toast.makeText(this, "Uploading report...", Toast.LENGTH_SHORT).show()
                
                // Simulate upload process
                continueButton.postDelayed({
                    Toast.makeText(this, "Report uploaded successfully!", Toast.LENGTH_LONG).show()
                    Log.d("UploadReport", "Upload completed successfully")
                    
                    // Navigate to retakeupload activity
                    try {
                        Log.d("UploadReport", "Creating intent for retakeupload")
                        val intent = Intent(this, com.simats.echohealth.RetakeUpload::class.java)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        
                        // Pass image data to RetakeUpload
                        selectedImageUri?.let { 
                            intent.putExtra("imageUri", it.toString())
                            Log.d("UploadReport", "Passing image URI: ${it.toString()}")
                        }
                        selectedFilePath?.let { path ->
                            // Only pass a real filesystem path
                            if (path.startsWith("/")) {
                                intent.putExtra("imagePath", path)
                                Log.d("UploadReport", "Passing filesystem path: $path")
                            } else {
                                Log.d("UploadReport", "Skipping non-filesystem path: $path")
                            }
                        }
                        
                        Log.d("UploadReport", "Intent created successfully")
                        
                        Log.d("UploadReport", "Starting retakeupload activity")
                        startActivity(intent)
                        Log.d("UploadReport", "Successfully navigated to retakeupload")
                        
                    } catch (e: Exception) {
                        Log.e("UploadReport", "Error navigating to retakeupload: ${e.message}")
                        Log.e("UploadReport", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                    
                }, 2000) // 2 second delay to simulate upload
                
            } else {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
                Log.d("UploadReport", "No file selected for upload - imageUri=${selectedImageUri} path=${selectedFilePath}")
            }
        } catch (e: Exception) {
            Log.e("UploadReport", "Error processing upload: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error uploading report", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("UploadReport", "Camera permission granted")
                    takePhoto()
                } else {
                    Log.d("UploadReport", "Camera permission denied")
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("UploadReport", "Storage permission granted")
                    pickFile()
                } else {
                    Log.d("UploadReport", "Storage permission denied")
                    Toast.makeText(this, "Storage permission is required to select files", Toast.LENGTH_LONG).show()
                }
            }
            MEDIA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("UploadReport", "Media permission granted")
                    pickFile()
                } else {
                    Log.d("UploadReport", "Media permission denied")
                    Toast.makeText(this, "Media permission is required to select files", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Handle camera result
                val imageBitmap = data?.extras?.get("data") as? android.graphics.Bitmap
                if (imageBitmap != null) {
                    // Save bitmap to file
                    try {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val imageFileName = "JPEG_${timeStamp}_"
                        val imageFile = File.createTempFile(imageFileName, ".jpg", cacheDir)
                        
                        val outputStream = java.io.FileOutputStream(imageFile)
                        imageBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                        outputStream.close()
                        
                        selectedImageUri = android.net.Uri.fromFile(imageFile)
                        selectedFilePath = selectedImageUri.toString()
                        
                        displayImagePreview(selectedImageUri!!)
                        updateReportPreview("Photo captured successfully")
                        enableContinueButton()
                        
                        Log.d("UploadReport", "Camera photo saved: $selectedFilePath")
                        Toast.makeText(this, "Photo captured successfully!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("UploadReport", "Error saving photo: ${e.message}")
                        Toast.makeText(this, "Error saving photo", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("UploadReport", "No image data in camera result")
                    Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("UploadReport", "Camera cancelled")
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("UploadReport", "UploadReport onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("UploadReport", "UploadReport onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("UploadReport", "UploadReport onDestroy called")
    }
}