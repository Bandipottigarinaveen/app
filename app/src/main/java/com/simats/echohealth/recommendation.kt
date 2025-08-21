package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Recommendation : AppCompatActivity() {
    
    private lateinit var backArrow: ImageView
    private lateinit var titleText: TextView
    private lateinit var contactSpecialistButton: LinearLayout
    private lateinit var shareButton: LinearLayout
    private lateinit var downloadButton: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Recommendation", "Recommendation onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d("Recommendation", "Edge to edge enabled")
            
            setContentView(R.layout.activity_recommendation)
            Log.d("Recommendation", "Recommendation layout set successfully")
            
            // Note: Window insets setup removed as main ID doesn't exist in layout
            Log.d("Recommendation", "Window insets setup successful")
        } catch (e: Exception) {
            Log.e("Recommendation", "Error in onCreate setup: ${e.message}")
            Log.e("Recommendation", "Stack trace: ${e.stackTraceToString()}")
        }

        // Initialize UI elements
        initializeViews()
        
        // Setup functionality
        setupBackNavigation()
        setupButtonActions()
        
        Log.d("Recommendation", "Recommendation onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d("Recommendation", "Initializing views...")
            
            backArrow = findViewById(R.id.backArrow)
            titleText = findViewById(R.id.titleText)
            contactSpecialistButton = findViewById(R.id.contactSpecialistButton)
            shareButton = findViewById(R.id.shareButton)
            downloadButton = findViewById(R.id.downloadButton)
            
            Log.d("Recommendation", "Views initialized successfully")
            Log.d("Recommendation", "Back arrow: ${if (backArrow != null) "FOUND" else "NOT FOUND"}")
            Log.d("Recommendation", "Title text: ${if (titleText != null) "FOUND" else "NOT FOUND"}")
            
        } catch (e: Exception) {
            Log.e("Recommendation", "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupBackNavigation() {
        try {
            Log.d("Recommendation", "Setting up back navigation")
            
            if (backArrow != null) {
                backArrow.setOnClickListener {
                    Log.d("Recommendation", "Back arrow clicked - finishing activity")
                    finish()
                }
                
                Log.d("Recommendation", "Back navigation setup successful")
            } else {
                Log.e("Recommendation", "Back arrow not found")
            }
        } catch (e: Exception) {
            Log.e("Recommendation", "Error setting up back navigation: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupButtonActions() {
        try {
            Log.d("Recommendation", "Setting up button actions")
            
            // Contact Specialist Button
            if (contactSpecialistButton != null) {
                contactSpecialistButton.setOnClickListener {
                    Log.d("Recommendation", "Contact specialist button clicked")
                    // TODO: Implement contact specialist functionality
                    Toast.makeText(this, "Contact specialist feature coming soon", Toast.LENGTH_SHORT).show()
                }
                Log.d("Recommendation", "Contact specialist button setup successful")
            } else {
                Log.e("Recommendation", "Contact specialist button not found")
            }
            
            // Share Button
            if (shareButton != null) {
                shareButton.setOnClickListener {
                    Log.d("Recommendation", "Share button clicked")
                    shareRecommendations()
                }
                Log.d("Recommendation", "Share button setup successful")
            } else {
                Log.e("Recommendation", "Share button not found")
            }
            
            // Download Button
            if (downloadButton != null) {
                downloadButton.setOnClickListener {
                    Log.d("Recommendation", "Download button clicked")
                    // TODO: Implement download functionality
                    Toast.makeText(this, "Download feature coming soon", Toast.LENGTH_SHORT).show()
                }
                Log.d("Recommendation", "Download button setup successful")
            } else {
                Log.e("Recommendation", "Download button not found")
            }
            
            Log.d("Recommendation", "Button actions setup completed successfully")
        } catch (e: Exception) {
            Log.e("Recommendation", "Error setting up button actions: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun shareRecommendations() {
        try {
            Log.d("Recommendation", "Sharing recommendations")
            
            val shareText = """
                Oral Cancer Assessment Recommendations
                =====================================
                
                Risk Level: Easy Risk
                Score: 2.4
                
                PREVENTIVE RECOMMENDATIONS:
                • Regular Oral Examinations (6-month interval)
                • Quit Tobacco Products
                • Limit Alcohol Consumption
                • Maintain Good Oral Hygiene
                • Selected Diet Rich in Antioxidants
                
                WATCH FOR THESE SIGNS:
                • Persistent Mouth Sores
                • White or Red Patches
                • Difficulty Swallowing
                • Unexplained Bleeding
                • Changes in Voice
                
                Generated by EchoHealth AI
            """.trimIndent()
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Oral Cancer Assessment Recommendations")
            }
            
            startActivity(Intent.createChooser(shareIntent, "Share Recommendations"))
            Log.d("Recommendation", "Share intent launched")
            
        } catch (e: Exception) {
            Log.e("Recommendation", "Error sharing recommendations: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error sharing recommendations", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("Recommendation", "Recommendation onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("Recommendation", "Recommendation onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("Recommendation", "Recommendation onDestroy called")
    }
}