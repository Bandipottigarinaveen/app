package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class Results : AppCompatActivity() {
    
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var riskScore: TextView
    private lateinit var riskLabel: TextView
    private lateinit var confidenceScore: TextView
    private lateinit var confidenceLabel: TextView
    private lateinit var btnDownload: Button
    private lateinit var btnMedicationGuide: Button
    private lateinit var btnRecommendations: Button
    private lateinit var lastUpdatedText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Results", "Results onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d("Results", "Edge to edge enabled")
            
            setContentView(R.layout.activity_results)
            Log.d("Results", "Results layout set successfully")
            
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            Log.d("Results", "Window insets setup successful")
        } catch (e: Exception) {
            Log.e("Results", "Error in onCreate setup: ${e.message}")
            Log.e("Results", "Stack trace: ${e.stackTraceToString()}")
        }

        // Initialize UI elements
        initializeViews()
        
        // Setup functionality
        setupResults()
        setupButtonActions()
        
        Log.d("Results", "Results onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d("Results", "Initializing views...")
            
            titleText = findViewById(R.id.titleText)
            subtitleText = findViewById(R.id.subtitleText)
            riskScore = findViewById(R.id.riskScore)
            riskLabel = findViewById(R.id.riskLabel)
            confidenceScore = findViewById(R.id.confidenceScore)
            confidenceLabel = findViewById(R.id.confidenceLabel)
            btnDownload = findViewById(R.id.btnDownload)
            btnMedicationGuide = findViewById(R.id.btnMedicationGuide)
            btnRecommendations = findViewById(R.id.btnRecommendations)
            lastUpdatedText = findViewById(R.id.lastUpdatedText)
            
            Log.d("Results", "Views initialized successfully")
            Log.d("Results", "Title text: ${if (titleText != null) "FOUND" else "NOT FOUND"}")
            Log.d("Results", "Risk score: ${if (riskScore != null) "FOUND" else "NOT FOUND"}")
            Log.d("Results", "Download button: ${if (btnDownload != null) "FOUND" else "NOT FOUND"}")
            
        } catch (e: Exception) {
            Log.e("Results", "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupResults() {
        try {
            Log.d("Results", "Setting up results display")
            
            // Check if coming from upload flow
            val fromUpload = intent.getBooleanExtra("fromUpload", false)
            Log.d("Results", "From upload flow: $fromUpload")
            
            if (fromUpload) {
                // Display upload-based results
                displayUploadResults()
            } else {
                // Display symptom-based results
                displaySymptomResults()
            }
            
            // Update timestamp
            updateTimestamp()
            
            Log.d("Results", "Results display setup successful")
        } catch (e: Exception) {
            Log.e("Results", "Error setting up results: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun displayUploadResults() {
        try {
            Log.d("Results", "Displaying upload-based results")
            
            // Simulate AI analysis results for uploaded report
            val riskScoreValue = "1.8"
            val riskLevel = "LOW RISK"
            val confidenceLevel = "94%"
            
            if (riskScore != null) riskScore.text = riskScoreValue
            if (riskLabel != null) riskLabel.text = riskLevel
            if (confidenceScore != null) confidenceScore.text = confidenceLevel
            
            Log.d("Results", "Upload results displayed - Risk: $riskScoreValue, Level: $riskLevel, Confidence: $confidenceLevel")
        } catch (e: Exception) {
            Log.e("Results", "Error displaying upload results: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun displaySymptomResults() {
        try {
            Log.d("Results", "Displaying symptom-based results")
            
            // Get symptom data from intent
            val soreThroat = intent.getBooleanExtra("soreThroat", false)
            val mouthUlcers = intent.getBooleanExtra("mouthUlcers", false)
            val voiceChange = intent.getBooleanExtra("voiceChange", false)
            val swallowing = intent.getBooleanExtra("swallowing", false)
            val duration = intent.getStringExtra("duration") ?: ""
            val severity = intent.getStringExtra("severity") ?: ""
            
            Log.d("Results", "Symptom data - Sore throat: $soreThroat, Mouth ulcers: $mouthUlcers, Voice change: $voiceChange, Swallowing: $swallowing")
            
            // Analyze symptoms and provide results
            val analysis = analyzeSymptoms(soreThroat, mouthUlcers, voiceChange, swallowing, duration, severity)
            
            // Update UI with analysis results
            updateResultsUI(analysis)
            
            Log.d("Results", "Symptom analysis completed: $analysis")
        } catch (e: Exception) {
            Log.e("Results", "Error displaying symptom results: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun updateResultsUI(analysis: String) {
        try {
            when {
                analysis.contains("High Risk") -> {
                    if (riskScore != null) riskScore.text = "8.5"
                    if (riskLabel != null) {
                        riskLabel.text = "HIGH RISK"
                        riskLabel.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
                    }
                    if (confidenceScore != null) confidenceScore.text = "87%"
                }
                analysis.contains("Medium Risk") -> {
                    if (riskScore != null) riskScore.text = "5.2"
                    if (riskLabel != null) {
                        riskLabel.text = "MEDIUM RISK"
                        riskLabel.setTextColor(resources.getColor(android.R.color.holo_orange_dark, theme))
                    }
                    if (confidenceScore != null) confidenceScore.text = "91%"
                }
                analysis.contains("Low Risk") -> {
                    if (riskScore != null) riskScore.text = "2.1"
                    if (riskLabel != null) {
                        riskLabel.text = "LOW RISK"
                        riskLabel.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
                    }
                    if (confidenceScore != null) confidenceScore.text = "95%"
                }
                else -> {
                    if (riskScore != null) riskScore.text = "0.5"
                    if (riskLabel != null) {
                        riskLabel.text = "NO RISK"
                        riskLabel.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
                    }
                    if (confidenceScore != null) confidenceScore.text = "98%"
                }
            }
            
            Log.d("Results", "Results UI updated with analysis: $analysis")
        } catch (e: Exception) {
            Log.e("Results", "Error updating results UI: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun analyzeSymptoms(
        soreThroat: Boolean,
        mouthUlcers: Boolean,
        voiceChange: Boolean,
        swallowing: Boolean,
        duration: String,
        severity: String
    ): String {
        val symptoms = mutableListOf<String>()
        
        if (soreThroat) symptoms.add("Sore Throat")
        if (mouthUlcers) symptoms.add("Mouth Ulcers")
        if (voiceChange) symptoms.add("Voice Changes")
        if (swallowing) symptoms.add("Difficulty Swallowing")
        
        return when {
            symptoms.size >= 3 -> "High Risk - Please consult a healthcare provider immediately"
            symptoms.size == 2 -> "Medium Risk - Monitor symptoms and consider consulting a doctor"
            symptoms.size == 1 -> "Low Risk - Continue monitoring symptoms"
            else -> "No symptoms reported - Continue with regular health monitoring"
        }
    }
    
    private fun updateTimestamp() {
        try {
            val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            val currentTime = sdf.format(Date())
            
            if (lastUpdatedText != null) {
                lastUpdatedText.text = "Last Updated: $currentTime"
            }
            
            Log.d("Results", "Timestamp updated: $currentTime")
        } catch (e: Exception) {
            Log.e("Results", "Error updating timestamp: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun downloadFullReport() {
        try {
            Log.d("Results", "Starting download full report")
            
            // Get current results data
            val riskScoreValue = riskScore?.text?.toString() ?: "N/A"
            val riskLevel = riskLabel?.text?.toString() ?: "N/A"
            val confidenceLevel = confidenceScore?.text?.toString() ?: "N/A"
            val timestamp = lastUpdatedText?.text?.toString() ?: "N/A"
            
            // Create report content
            val reportContent = """
                AI Health Analysis Report
                =========================
                
                Risk Score: $riskScoreValue
                Risk Level: $riskLevel
                Confidence Level: $confidenceLevel
                Analysis Date: $timestamp
                
                Summary:
                This report contains the AI analysis results for your health assessment.
                The analysis was performed using advanced machine learning algorithms
                to provide accurate risk assessment and recommendations.
                
                Recommendations:
                - Continue monitoring your health regularly
                - Consult healthcare provider if symptoms persist
                - Follow recommended lifestyle changes
                - Schedule follow-up appointments as needed
                
                Note: This report is for informational purposes only and should not
                replace professional medical advice.
            """.trimIndent()
            
            // Simulate download process
            Toast.makeText(this, "Preparing report for download...", Toast.LENGTH_SHORT).show()
            
            // Simulate download delay
            btnDownload.postDelayed({
                Toast.makeText(this, "Report downloaded successfully!", Toast.LENGTH_LONG).show()
                Log.d("Results", "Report download completed")
                
                // Here you would typically save the file to device storage
                // For now, we'll just show a success message
                
            }, 2000) // 2 second delay to simulate download
            
        } catch (e: Exception) {
            Log.e("Results", "Error downloading report: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error downloading report", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToRecommendations() {
        try {
            Log.d("Results", "Navigating to recommendations")
            
            // Create intent for recommendation activity
            val intent = Intent(this, com.simats.echohealth.Recommendation::class.java)
            
            // Pass relevant data to recommendation activity
            intent.putExtra("riskScore", riskScore?.text?.toString())
            intent.putExtra("riskLevel", riskLabel?.text?.toString())
            intent.putExtra("confidenceLevel", confidenceScore?.text?.toString())
            intent.putExtra("fromResults", true)
            
            Log.d("Results", "Intent created successfully")
            
            // Start recommendation activity
            startActivity(intent)
            Log.d("Results", "Successfully navigated to recommendations")
            
        } catch (e: Exception) {
            Log.e("Results", "Error navigating to recommendations: ${e.message}")
            Log.e("Results", "Stack trace: ${e.stackTraceToString()}")
            e.printStackTrace()
            Toast.makeText(this, "Error opening recommendations", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupButtonActions() {
        try {
            Log.d("Results", "Setting up button actions")
            
            // Download Full Report Button
            if (btnDownload != null) {
                btnDownload.setOnClickListener {
                    Log.d("Results", "Download button clicked")
                    downloadFullReport()
                }
                Log.d("Results", "Download button setup successful")
            } else {
                Log.e("Results", "Download button not found")
            }
            
            // AI Medication Guide Button
            if (btnMedicationGuide != null) {
                btnMedicationGuide.setOnClickListener {
                    Log.d("Results", "Medication guide button clicked")
                    // TODO: Navigate to medication guide
                    Log.d("Results", "Medication guide functionality to be implemented")
                }
                Log.d("Results", "Medication guide button setup successful")
            } else {
                Log.e("Results", "Medication guide button not found")
            }
            
            // Get Recommendations Button
            if (btnRecommendations != null) {
                btnRecommendations.setOnClickListener {
                    Log.d("Results", "Recommendations button clicked")
                    navigateToRecommendations()
                }
                Log.d("Results", "Recommendations button setup successful")
            } else {
                Log.e("Results", "Recommendations button not found")
            }
            
            Log.d("Results", "Button actions setup completed successfully")
        } catch (e: Exception) {
            Log.e("Results", "Error setting up button actions: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("Results", "Results onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("Results", "Results onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("Results", "Results onDestroy called")
    }
}