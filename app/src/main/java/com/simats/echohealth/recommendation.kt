package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Recommendation : AppCompatActivity() {
    
    private lateinit var backArrow: ImageView
    private lateinit var titleText: TextView
    private lateinit var riskScoreValue: TextView
    private lateinit var riskLevelValue: TextView
    // Removed contactSpecialistButton per request
    private lateinit var shareButton: LinearLayout
    // Removed downloadButton
    private lateinit var btnShareResult: MaterialButton
    private lateinit var preventiveItem1: TextView
    private lateinit var preventiveItem2: TextView
    private lateinit var preventiveItem3: TextView
    private lateinit var preventiveItem4: TextView
    private lateinit var preventiveItem5: TextView
    private lateinit var signItem1: TextView
    private lateinit var signItem2: TextView
    private lateinit var signItem3: TextView
    private lateinit var signItem4: TextView
    private lateinit var signItem5: TextView
    
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
        populateHeaderFromIntent()
        
        Log.d("Recommendation", "Recommendation onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d("Recommendation", "Initializing views...")
            
            backArrow = findViewById(R.id.backArrow)
            titleText = findViewById(R.id.titleText)
            riskScoreValue = findViewById(R.id.riskScoreValue)
            riskLevelValue = findViewById(R.id.riskLevelValue)
            // Removed contactSpecialistButton binding
            shareButton = findViewById(R.id.shareButton)
            btnShareResult = findViewById(R.id.btnShareResult)
            preventiveItem1 = findViewById(R.id.preventiveItem1)
            preventiveItem2 = findViewById(R.id.preventiveItem2)
            preventiveItem3 = findViewById(R.id.preventiveItem3)
            preventiveItem4 = findViewById(R.id.preventiveItem4)
            preventiveItem5 = findViewById(R.id.preventiveItem5)
            signItem1 = findViewById(R.id.signItem1)
            signItem2 = findViewById(R.id.signItem2)
            signItem3 = findViewById(R.id.signItem3)
            signItem4 = findViewById(R.id.signItem4)
            signItem5 = findViewById(R.id.signItem5)
            
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
            
            // Removed contact specialist button wiring
            
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
            
            // Share as PDF button
            btnShareResult.setOnClickListener {
                val riskLevel = intent.getStringExtra("riskLevel") ?: "Unknown"
                val score = intent.getStringExtra("riskScore") ?: "N/A"
                val infoText = when {
                    riskLevel.contains("HIGH", true) -> "High risk detected. Immediate specialist consultation recommended."
                    riskLevel.contains("MODERATE", true) || riskLevel.contains("MEDIUM", true) -> "Moderate risk. Schedule a dental/oral specialist visit within 2 weeks."
                    riskLevel.contains("LOW", true) -> "Low risk. Maintain good oral hygiene and regular checkups."
                    else -> "General guidance: maintain oral hygiene and monitor for new symptoms."
                }
                val mode = "symptoms" // adjust if mode is passed
                generatePdfAndShare(riskLevel, score, mode, infoText)
            }
            
            Log.d("Recommendation", "Button actions setup completed successfully")
        } catch (e: Exception) {
            Log.e("Recommendation", "Error setting up button actions: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun generatePdfAndShare(riskLevel: String, percentageText: String, mode: String, infoText: String) {
        val ts = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val data = com.simats.echohealth.utils.PdfGenerator.ReportData(
            title = "Oral Cancer Risk Assessment Report",
            riskLevel = riskLevel,
            percentage = percentageText,
            mode = mode,
            infoText = infoText,
            timestamp = ts
        )
        val uri = com.simats.echohealth.utils.PdfGenerator.generateReportPdf(this, data)
        if (uri == null) {
            Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
            return
        }
        val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(android.content.Intent.createChooser(share, "Share Result PDF"))
    }

    private fun populateHeaderFromIntent() {
        try {
            val riskLevel = intent.getStringExtra("riskLevel") ?: "Unknown"
            val score = intent.getStringExtra("riskScore") ?: "N/A"
            riskScoreValue.text = score
            riskLevelValue.text = "Risk Level: $riskLevel"

            // If Results page provided lists, use them first
            val providedRecs = intent.getStringArrayExtra("recommendations")
            val providedWarns = intent.getStringArrayExtra("warningSigns")
            if (!providedRecs.isNullOrEmpty() || !providedWarns.isNullOrEmpty()) {
                val recs = providedRecs ?: emptyArray()
                val warns = providedWarns ?: emptyArray()
                // Fill preventive recommendations
                val recTargets = listOf(preventiveItem1, preventiveItem2, preventiveItem3, preventiveItem4, preventiveItem5)
                recTargets.forEachIndexed { idx, tv -> tv.text = recs.getOrNull(idx) ?: tv.text }
                // Fill warning signs
                val warnTargets = listOf(signItem1, signItem2, signItem3, signItem4, signItem5)
                warnTargets.forEachIndexed { idx, tv -> tv.text = warns.getOrNull(idx) ?: tv.text }
                return
            }

            // Else adapt sections based on risk level
            when {
                riskLevel.contains("HIGH", ignoreCase = true) -> {
                    preventiveItem1.text = "Book immediate consultation with oral specialist"
                    preventiveItem2.text = "Arrange biopsy if lesions present"
                    preventiveItem3.text = "Stop tobacco and alcohol immediately"
                    preventiveItem4.text = "Weekly self-checks and 3‑month monitoring"
                    preventiveItem5.text = "Prioritize soft diet and oral hygiene"

                    signItem1.text = "Rapidly growing lumps or persistent ulcers"
                    signItem2.text = "White/red patches that do not heal"
                    signItem3.text = "Painful swallowing or constant sore throat"
                    signItem4.text = "Unexplained bleeding in mouth"
                    signItem5.text = "Numbness or changes in voice"
                }
                riskLevel.contains("MODERATE", ignoreCase = true) || riskLevel.contains("MEDIUM", ignoreCase = true) -> {
                    preventiveItem1.text = "Schedule dental/oral specialist visit within 2 weeks"
                    preventiveItem2.text = "Reduce or stop tobacco and alcohol"
                    preventiveItem3.text = "Improve daily brushing and flossing"
                    preventiveItem4.text = "Use antiseptic mouthwash as advised"
                    preventiveItem5.text = "Monitor nutrition and hydration"

                    signItem1.text = "Sores lasting > 2 weeks"
                    signItem2.text = "Patches that change color/size"
                    signItem3.text = "Intermittent pain when swallowing"
                    signItem4.text = "Bleeding after minor irritation"
                    signItem5.text = "Voice changes persisting > 2 weeks"
                }
                riskLevel.contains("LOW", ignoreCase = true) -> {
                    preventiveItem1.text = "Routine dental checkups every 6 months"
                    preventiveItem2.text = "Avoid tobacco; limit alcohol"
                    preventiveItem3.text = "Maintain excellent oral hygiene"
                    preventiveItem4.text = "Balanced diet rich in fruits/vegetables"
                    preventiveItem5.text = "Stay hydrated and manage stress"

                    signItem1.text = "New mouth sores not healing in 2 weeks"
                    signItem2.text = "Any new white/red patches"
                    signItem3.text = "Occasional swallowing discomfort"
                    signItem4.text = "Unexpected gum or mouth bleeding"
                    signItem5.text = "Hoarseness that does not resolve"
                }
                else -> {
                    preventiveItem1.text = "Maintain regular oral hygiene"
                    preventiveItem2.text = "Avoid tobacco and excess alcohol"
                    preventiveItem3.text = "Schedule periodic dental exams"
                    preventiveItem4.text = "Healthy diet and adequate sleep"
                    preventiveItem5.text = "Report new symptoms promptly"

                    signItem1.text = "Non-healing sores"
                    signItem2.text = "White/red patches"
                    signItem3.text = "Difficulty swallowing"
                    signItem4.text = "Unexplained bleeding"
                    signItem5.text = "Voice changes/persistent pain"
                }
            }
        } catch (e: Exception) {
            Log.e("Recommendation", "Error populating header: ${e.message}")
        }
    }
    
    private fun shareRecommendations() {
        try {
            Log.d("Recommendation", "Sharing recommendations")
            
            val riskLevel = intent.getStringExtra("riskLevel") ?: "Unknown"
            val score = intent.getStringExtra("riskScore") ?: "N/A"
            val recs = intent.getStringArrayExtra("recommendations") ?: emptyArray()
            val warns = intent.getStringArrayExtra("warningSigns") ?: emptyArray()

            val shareText = buildString {
                appendLine("Oral Cancer Assessment Recommendations")
                appendLine("=====================================")
                appendLine()
                appendLine("Risk Level: $riskLevel")
                appendLine("Score: $score")
                appendLine()
                appendLine("PREVENTIVE RECOMMENDATIONS:")
                recs.forEach { appendLine("• $it") }
                appendLine()
                appendLine("WATCH FOR THESE SIGNS:")
                warns.forEach { appendLine("• $it") }
                appendLine()
                appendLine("Generated by EchoHealth AI")
            }
            
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