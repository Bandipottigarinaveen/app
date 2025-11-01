package com.simats.echohealth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.webkit.MimeTypeMap
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.echohealth.Retrofit.RetrofitClient
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Responses.OralCancerDetectRequest
import com.simats.echohealth.Responses.OralCancerDetectResponse
import com.simats.echohealth.utils.ImageUtils
import com.simats.echohealth.auth.AuthManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class RetakeUpload : AppCompatActivity() {
    
    private lateinit var backIcon: ImageView
    private lateinit var btnRetake: Button
    private lateinit var btnContinue: Button
    private lateinit var imagePreview: ImageView
    private lateinit var fallbackContent: LinearLayout
    
    private var selectedImageUri: Uri? = null
    private var selectedImagePath: String? = null
    private var selectedImageFile: File? = null
    private lateinit var apiService: ApiService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RetakeUpload", "RetakeUpload onCreate started")
        
        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_retakeupload)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error in onCreate setup: ${e.message}")
        }

        apiService = RetrofitClient.getClient().create(ApiService::class.java)
        getImageDataFromIntent()
        initializeViews()
        displayImagePreview()
        setupNavigation()
    }
    
    private fun getImageDataFromIntent() {
        try {
            val imageUriString = intent.getStringExtra("imageUri")
            val imagePath = intent.getStringExtra("imagePath")
            
            if (!imageUriString.isNullOrEmpty()) {
                selectedImageUri = Uri.parse(imageUriString)
                try {
                    if ("content" == selectedImageUri?.scheme) {
                        contentResolver.takePersistableUriPermission(
                            selectedImageUri!!,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                } catch (permErr: Exception) {
                    Log.w("RetakeUpload", "Could not persist URI permission: ${permErr.message}")
                }
                selectedImageFile = uriToFile(selectedImageUri!!)
                if (selectedImageFile == null) {
                    selectedImageFile = uriToFile(selectedImageUri!!)
                    if (selectedImageFile == null) {
                        Toast.makeText(this, "Failed to access image file. Please try again.", Toast.LENGTH_LONG).show()
                        return
                    }
                }
            }
            
            if (!imagePath.isNullOrEmpty()) {
                selectedImagePath = imagePath
                selectedImageFile = File(imagePath)
                if (!selectedImageFile!!.exists()) {
                    Toast.makeText(this, "Image file not found. Please try again.", Toast.LENGTH_LONG).show()
                    selectedImageFile = null
                    return
                }
            }
            
            if (selectedImageUri == null && selectedImagePath == null) {
                Toast.makeText(this, "No image data found. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error getting image data from intent: ${e.message}")
        }
    }
    
    private fun initializeViews() {
        try {
            backIcon = findViewById(R.id.backIcon)
            btnRetake = findViewById(R.id.btnRetake)
            btnContinue = findViewById(R.id.btnContinue)
            imagePreview = findViewById(R.id.imagePreview)
            fallbackContent = findViewById(R.id.fallbackContent)
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error initializing views: ${e.message}")
        }
    }
    
    private fun setupNavigation() {
        try {
            backIcon.setOnClickListener { finish() }
            btnRetake.setOnClickListener {
                try {
                    val intent = Intent(this, com.simats.echohealth.UploadReport::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e("RetakeUpload", "Error navigating to uploadreport: ${e.message}")
                }
            }
            btnContinue.setOnClickListener {
                callOralCancerDetectionAPI()
            }
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error setting up navigation: ${e.message}")
        }
    }
    
    override fun onResume() {
        super.onResume()
    }
    
    override fun onPause() {
        super.onPause()
    }
    
    private fun callOralCancerDetectionAPI() {
        try {
            if (!com.simats.echohealth.Retrofit.NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_LONG).show()
                return
            }
            
            btnContinue.isEnabled = false
            btnContinue.text = "Analyzing..."
            
            if (selectedImageFile == null) {
                Toast.makeText(this, "No image file available.", Toast.LENGTH_SHORT).show()
                resetContinueButton()
                return
            }
            
            if (!selectedImageFile!!.exists() || !selectedImageFile!!.canRead()) {
                if (selectedImageUri != null) {
                    selectedImageFile = uriToFile(selectedImageUri!!)
                    if (selectedImageFile == null || !selectedImageFile!!.exists() || !selectedImageFile!!.canRead()) {
                        Toast.makeText(this, "Image file is not accessible.", Toast.LENGTH_LONG).show()
                        resetContinueButton()
                        return
                    }
                } else {
                    Toast.makeText(this, "Image file is not accessible.", Toast.LENGTH_LONG).show()
                    resetContinueButton()
                    return
                }
            }
            
            val authToken = AuthManager.getAuthToken(this)
            if (authToken == null) {
                Toast.makeText(this, "Please login to use the photo analysis feature.", Toast.LENGTH_LONG).show()
                resetContinueButton()
                return
            }
            
            // Normalize/prepare image as clean JPEG to avoid backend decode errors
            val preparedFile = prepareImageFile(selectedImageFile!!)
            if (preparedFile == null || !preparedFile.exists() || preparedFile.length() == 0L) {
                Toast.makeText(this, "Could not process image. Please try another image.", Toast.LENGTH_LONG).show()
                resetContinueButton()
                return
            }
            // Enforce 10MB limit
            if (preparedFile.length() > 10L * 1024L * 1024L) {
                Toast.makeText(this, "File too large. Please select an image under 10MB.", Toast.LENGTH_LONG).show()
                resetContinueButton()
                return
            }
            // Explicitly check against tiny/corrupt files (<5KB)
            if (preparedFile.length() < 5L * 1024L) {
                Toast.makeText(this, "Image is too small or corrupt. Please choose a clearer image.", Toast.LENGTH_LONG).show()
                resetContinueButton()
                return
            }
            val mimeType = "image/jpeg"
            // Primary: MULTIPART file upload (backend says "image file is required")
            val media = mimeType.toMediaTypeOrNull()
            val reqBody = preparedFile.asRequestBody(media)
            val part = MultipartBody.Part.createFormData("image", "upload.jpg", reqBody)
            var inFlightCall: Call<OralCancerDetectResponse> = apiService.detectOralCancerMultipart(
                "Bearer $authToken",
                part
            )
            
            inFlightCall.enqueue(object : Callback<OralCancerDetectResponse> {
                override fun onResponse(call: Call<OralCancerDetectResponse>, response: Response<OralCancerDetectResponse>) {
                    try {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse != null) {
                                navigateToResultsWithAPIResponse(apiResponse)
                            } else {
                                Toast.makeText(this@RetakeUpload, "Analysis failed: Empty response", Toast.LENGTH_LONG).show()
                                resetContinueButton()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("RetakeUpload", "Detect ${response.code()}: ${errorBody ?: "(no body)"} | size=${preparedFile.length()} bytes | ct=$mimeType")
                            // Try to show server-provided message if available
                            var serverMessage: String? = null
                            try {
                                if (!errorBody.isNullOrEmpty()) {
                                    val json = org.json.JSONObject(errorBody)
                                    serverMessage = when {
                                        json.has("message") -> json.optString("message")
                                        json.has("error") -> json.optString("error")
                                        else -> null
                                    }
                                }
                            } catch (_: Exception) { }
                            // Provide clearer messaging for backend integration errors (e.g., expired Gemini API key)
                            val lower = errorBody?.lowercase() ?: ""
                            val backendConfigIssue = lower.contains("gemini") || lower.contains("api key") || lower.contains("generativelanguage.googleapis.com")
                            val errorMsg = when {
                                !serverMessage.isNullOrBlank() -> serverMessage!!
                                backendConfigIssue -> "Server configuration error. Please try again later."
                                response.code() == 400 -> "Invalid image input. Please select a clear JPG/PNG under 10MB and try again."
                                response.code() == 401 -> "Authentication failed. Please login again."
                                response.code() == 415 -> "Unsupported media type. Please use JPG or PNG."
                                else -> "Analysis failed (Error ${response.code()}). Please try again."
                            }
                            Toast.makeText(this@RetakeUpload, errorMsg, Toast.LENGTH_LONG).show()
                            resetContinueButton()
                            return
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RetakeUpload, "Error processing results", Toast.LENGTH_SHORT).show()
                        resetContinueButton()
                    }
                }
                
                override fun onFailure(call: Call<OralCancerDetectResponse>, t: Throwable) {
                    val errorMessage = when {
                        t.message?.contains("timeout", true) == true -> "Request timeout. Please try again."
                        t.message?.contains("network", true) == true -> "Network error. Check your connection."
                        else -> "Network error. Please try again."
                    }
                    Toast.makeText(this@RetakeUpload, errorMessage, Toast.LENGTH_LONG).show()
                    resetContinueButton()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting analysis", Toast.LENGTH_SHORT).show()
            resetContinueButton()
        }
    }

    private fun resolveMimeType(uri: Uri?, file: File): String {
        if (uri != null) {
            try {
                contentResolver.getType(uri)?.let { return it }
            } catch (_: Exception) {}
        }
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)?.lowercase()
            ?: file.extension.lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "image/jpeg"
        }
    }

    private fun buildPrefixedBase64(base64Raw: String, mimeType: String): String {
        val prefix = when (mimeType) {
            "image/jpeg" -> "data:image/jpeg;base64,"
            "image/jpg" -> "data:image/jpeg;base64,"
            "image/png" -> "data:image/png;base64,"
            else -> "data:image/jpeg;base64,"
        }
        return prefix + base64Raw
    }
    
    private fun navigateToResultsWithAPIResponse(apiResponse: OralCancerDetectResponse) {
        try {
            val intent = Intent(this, com.simats.echohealth.Results::class.java)
            intent.putExtra("fromUpload", true)
            intent.putExtra("isApiResult", true)
            apiResponse.prediction?.let { intent.putExtra("prediction", it) }
            val probability = apiResponse.prediction_percentage ?: apiResponse.confidence
            probability?.let { intent.putExtra("probability", it) }
            apiResponse.risk_level?.let { intent.putExtra("riskLevel", it) }
            apiResponse.recommendations?.let { intent.putExtra("recommendations", it.toTypedArray()) }
            apiResponse.warning_signs?.let { intent.putExtra("warningSigns", it.toTypedArray()) }
            apiResponse.next_steps?.let { intent.putExtra("nextSteps", it.toTypedArray()) }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening results", Toast.LENGTH_SHORT).show()
            resetContinueButton()
        }
    }
    
    private fun displayImagePreview() {
        try {
            if (selectedImageUri != null) {
                imagePreview.setImageURI(selectedImageUri)
                imagePreview.visibility = View.VISIBLE
                fallbackContent.visibility = View.GONE
            } else if (selectedImagePath != null) {
                val bitmap = BitmapFactory.decodeFile(selectedImagePath)
                if (bitmap != null) {
                    imagePreview.setImageBitmap(bitmap)
                    imagePreview.visibility = View.VISIBLE
                    fallbackContent.visibility = View.GONE
                } else {
                    showFallbackContent()
                }
            } else {
                showFallbackContent()
            }
        } catch (e: Exception) {
            showFallbackContent()
        }
    }
    
    private fun showFallbackContent() {
        imagePreview.visibility = View.GONE
        fallbackContent.visibility = View.VISIBLE
    }
    
    private fun resetContinueButton() {
        btnContinue.isEnabled = true
        btnContinue.text = "Continue"
    }
    
    private fun uriToFile(uri: Uri): File? {
        return try {
            when (uri.scheme) {
                "file" -> File(uri.path!!)
                "content" -> {
                    val mimeType = contentResolver.getType(uri)
                    val extension = when (mimeType) {
                        "image/jpeg", "image/jpg" -> ".jpg"
                        "image/png" -> ".png"
                        "image/webp" -> ".webp"
                        else -> ".jpg"
                    }
                    val file = createTempFile("image_", extension, cacheDir)
                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (file.length() == 0L) {
                        file.delete()
                        null
                    } else file
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error converting URI to File: ${e.message}")
            null
        }
    }

    private fun prepareImageFile(original: File): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(original.absolutePath) ?: return null
            val outFile = createTempFile("upload_", ".jpg", cacheDir)
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
            }
            outFile
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error preparing image: ${e.message}")
            null
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}