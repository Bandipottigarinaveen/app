package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RetakeUpload : AppCompatActivity() {
    
    private lateinit var backIcon: ImageView
    private lateinit var btnRetake: Button
    private lateinit var btnContinue: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RetakeUpload", "RetakeUpload onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d("RetakeUpload", "Edge to edge enabled")
            
            setContentView(R.layout.activity_retakeupload)
            Log.d("RetakeUpload", "RetakeUpload layout set successfully")
            
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            Log.d("RetakeUpload", "Window insets setup successful")
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error in onCreate setup: ${e.message}")
            Log.e("RetakeUpload", "Stack trace: ${e.stackTraceToString()}")
        }

        // Initialize UI elements
        initializeViews()
        
        // Setup functionality
        setupNavigation()
        
        Log.d("RetakeUpload", "RetakeUpload onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d("RetakeUpload", "Initializing views...")
            
            backIcon = findViewById(R.id.backIcon)
            btnRetake = findViewById(R.id.btnRetake)
            btnContinue = findViewById(R.id.btnContinue)
            
            Log.d("RetakeUpload", "Views initialized successfully")
            Log.d("RetakeUpload", "Back icon: ${if (backIcon != null) "FOUND" else "NOT FOUND"}")
            Log.d("RetakeUpload", "Retake button: ${if (btnRetake != null) "FOUND" else "NOT FOUND"}")
            Log.d("RetakeUpload", "Continue button: ${if (btnContinue != null) "FOUND" else "NOT FOUND"}")
            
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupNavigation() {
        try {
            Log.d("RetakeUpload", "Setting up navigation")
            
            // Back Icon - Go back to previous screen
            if (backIcon != null) {
                backIcon.setOnClickListener {
                    Log.d("RetakeUpload", "Back icon clicked - finishing activity")
                    finish()
                }
                Log.d("RetakeUpload", "Back navigation setup successful")
            } else {
                Log.e("RetakeUpload", "Back icon not found")
            }
            
            // Retake/Reupload Button - Go back to upload report
            if (btnRetake != null) {
                btnRetake.setOnClickListener {
                    Log.d("RetakeUpload", "Retake button clicked - navigating to uploadreport")
                    try {
                        Log.d("RetakeUpload", "Creating intent for uploadreport")
                        val intent = Intent(this, com.simats.echohealth.UploadReport::class.java)
                        Log.d("RetakeUpload", "Intent created successfully")
                        
                        Log.d("RetakeUpload", "Starting uploadreport activity")
                        startActivity(intent)
                        Log.d("RetakeUpload", "Successfully navigated to uploadreport")
                        finish()
                        
                    } catch (e: Exception) {
                        Log.e("RetakeUpload", "Error navigating to uploadreport: ${e.message}")
                        Log.e("RetakeUpload", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
                Log.d("RetakeUpload", "Retake button navigation setup successful")
            } else {
                Log.e("RetakeUpload", "Retake button not found")
            }
            
            // Continue Button - Navigate to Results
            if (btnContinue != null) {
                btnContinue.setOnClickListener {
                    Log.d("RetakeUpload", "Continue button clicked - navigating to results")
                    try {
                        Log.d("RetakeUpload", "Creating intent for results")
                        val intent = Intent(this, com.simats.echohealth.Results::class.java)
                        intent.putExtra("fromUpload", true)
                        Log.d("RetakeUpload", "Intent created successfully")
                        
                        Log.d("RetakeUpload", "Starting results activity")
                        startActivity(intent)
                        Log.d("RetakeUpload", "Successfully navigated to results")
                        finish()
                        
                    } catch (e: Exception) {
                        Log.e("RetakeUpload", "Error navigating to results: ${e.message}")
                        Log.e("RetakeUpload", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
                Log.d("RetakeUpload", "Continue button navigation setup successful")
            } else {
                Log.e("RetakeUpload", "Continue button not found")
            }
            
            Log.d("RetakeUpload", "Navigation setup completed successfully")
        } catch (e: Exception) {
            Log.e("RetakeUpload", "Error setting up navigation: ${e.message}")
            Log.e("RetakeUpload", "Stack trace: ${e.stackTraceToString()}")
            e.printStackTrace()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("RetakeUpload", "RetakeUpload onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("RetakeUpload", "RetakeUpload onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("RetakeUpload", "RetakeUpload onDestroy called")
    }
}