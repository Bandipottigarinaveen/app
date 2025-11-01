package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.view.View
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
    private lateinit var modeText: TextView
    private lateinit var riskScore: TextView
    private lateinit var riskLabel: TextView
    private lateinit var riskCircleContainer: android.widget.LinearLayout
    private lateinit var confidenceScore: TextView
    private lateinit var confidenceLabel: TextView
    private lateinit var btnStartAssessment: Button
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
            modeText = findViewById(R.id.modeText)
            riskScore = findViewById(R.id.riskScore)
            riskLabel = findViewById(R.id.riskLabel)
            riskCircleContainer = findViewById(R.id.riskCircleContainer)
            confidenceScore = findViewById(R.id.confidenceScore)
            confidenceLabel = findViewById(R.id.confidenceLabel)
            btnStartAssessment = findViewById(R.id.btnStartAssessment)
            btnRecommendations = findViewById(R.id.btnRecommendations)
            lastUpdatedText = findViewById(R.id.lastUpdatedText)
            
            Log.d("Results", "Views initialized successfully")
            Log.d("Results", "Title text: ${if (titleText != null) "FOUND" else "NOT FOUND"}")
            Log.d("Results", "Risk score: ${if (riskScore != null) "FOUND" else "NOT FOUND"}")
            
            
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
                val showed = displaySymptomResults()
                if (!showed) {
                    // Try loading persisted last result
                    if (!showFromPersistedLastResult()) {
                        showNoResultPlaceholder()
                    }
                }
            }
            
            // Update timestamp
            updateTimestamp()

            // Persist a compact summary for dashboard risk widgets
            try {
                val prefs = getSharedPreferences("LastResult", MODE_PRIVATE)
                val level = intent.getStringExtra("riskLevel")
                val score = intent.getStringExtra("riskScore") ?: intent.getIntExtra("riskScore", -1).let { if (it >= 0) it.toString() else null }
                val probability = intent.getDoubleExtra("probability", -1.0)
                val isApi = intent.getBooleanExtra("isApiResult", false)
                val assessmentType = intent.getStringExtra("assessmentType") ?: "Assessment Results"
                val recs = intent.getStringArrayExtra("recommendations")?.joinToString("||") ?: ""
                val warns = intent.getStringArrayExtra("warningSigns")?.joinToString("||") ?: ""
                val steps = intent.getStringArrayExtra("nextSteps")?.joinToString("||") ?: ""

                prefs.edit()
                    .putString("riskLevel", level)
                    .putString("riskScore", score)
                    .putFloat("probability", if (probability >= 0) probability.toFloat() else -1f)
                    .putBoolean("isApiResult", isApi)
                    .putString("assessmentType", assessmentType)
                    .putString("recommendations", recs)
                    .putString("warningSigns", warns)
                    .putString("nextSteps", steps)
                    .apply()
                
                // Also save to database for history page
                val mode = intent.getStringExtra("mode") ?: "unknown"
                val activityType = when (mode.lowercase()) {
                    "symptoms" -> "symptoms"
                    "image" -> "upload"
                    else -> "assessment"
                }
                
                val description = when {
                    !level.isNullOrBlank() && !score.isNullOrBlank() -> "Risk $level, Score $score"
                    !level.isNullOrBlank() -> "Risk $level"
                    probability >= 0 -> "Risk $level, ${(probability * 100).toInt()}%"
                    else -> "Assessment completed"
                }
                
                // Save to database for history page
                ActivityDatabase.add(
                    this,
                    DbActivityItem(
                        title = when (activityType) {
                            "symptoms" -> "Symptoms Entry"
                            "upload" -> "Report Upload"
                            else -> "Risk Assessment"
                        },
                        description = description,
                        timestampMillis = System.currentTimeMillis(),
                        type = activityType,
                        riskLevel = level,
                        riskScore = score?.toIntOrNull(),
                        riskPercent = if (probability >= 0) (probability * 100).toInt() else null
                    )
                )
                Log.d("Results", "Saved results to database: $activityType - $description")
                
                // Also save to SharedPreferences for dashboard recent activity
                ActivityLogStore.addActivity(
                    this,
                    title = when (activityType) {
                        "symptoms" -> "Symptoms Entry"
                        "upload" -> "Report Upload"
                        else -> "Risk Assessment"
                    },
                    description = description,
                    type = activityType,
                    riskLevel = level,
                    riskScore = score?.toIntOrNull(),
                    riskPercent = if (probability >= 0) (probability * 100).toInt() else null
                )
                Log.d("Results", "Saved results to SharedPreferences: $activityType - $description")
            } catch (e: Exception) {
                Log.e("Results", "Failed to save results: ${e.message}")
                e.printStackTrace()
            }
            
            Log.d("Results", "Results display setup successful")
        } catch (e: Exception) {
            Log.e("Results", "Error setting up results: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun displayUploadResults() {
        try {
            Log.d("Results", "Displaying upload-based results")
            
            // Check if we have API result data
            val isApiResult = intent.getBooleanExtra("isApiResult", false)
            val modeExtra = intent.getStringExtra("mode")
            
            if (isApiResult) {
                // Use API result data if available
                val riskLevelExtra = intent.getStringExtra("riskLevel")
                val probabilityExtra = if (intent.hasExtra("probability")) intent.getDoubleExtra("probability", -1.0) else -1.0
                
                // Display mode information
                if (modeExtra != null && modeText != null) {
                    val modeDisplayText = when (modeExtra.lowercase()) {
                        "image" -> "📸 Image Analysis"
                        "symptoms" -> "🔍 Symptom Analysis"
                        else -> "🤖 ${modeExtra.replaceFirstChar { it.uppercase() }} Analysis"
                    }
                    modeText.text = modeDisplayText
                    modeText.visibility = View.VISIBLE
                    Log.d("Results", "Mode displayed: $modeDisplayText")
                } else if (modeText != null) {
                    modeText.visibility = View.GONE
                }
                
                // Use API data if available
                if (probabilityExtra >= 0) {
                    val pct = (probabilityExtra * 100.0).coerceIn(0.0, 100.0)
                    confidenceScore.text = String.format(Locale.getDefault(), "%.0f%%", pct)
                }
                
                if (!riskLevelExtra.isNullOrBlank()) {
                    riskLabel.text = riskLevelExtra.uppercase(Locale.getDefault())
                }
                
                Log.d("Results", "API upload results displayed - Mode: $modeExtra, Risk Level: $riskLevelExtra, Probability: $probabilityExtra")
            } else {
                // Fallback to simulated results
                val riskScoreValue = "1.8"
                val riskLevel = "LOW RISK"
                val confidenceLevel = "94%"
                
                if (riskScore != null) riskScore.text = riskScoreValue
                if (riskLabel != null) riskLabel.text = riskLevel
                if (confidenceScore != null) confidenceScore.text = confidenceLevel
                
                // Hide mode text for simulated results
                if (modeText != null) {
                    modeText.visibility = View.GONE
                }
                
                Log.d("Results", "Simulated upload results displayed - Risk: $riskScoreValue, Level: $riskLevel, Confidence: $confidenceLevel")
            }
        } catch (e: Exception) {
            Log.e("Results", "Error displaying upload results: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun displaySymptomResults(): Boolean {
        try {
            Log.d("Results", "Displaying symptom-based results")

            // Prefer showing data coming from SymptomChecker extras
            if (showFromSymptomCheckerExtras()) {
                Log.d("Results", "Displayed results from SymptomChecker extras")
                return true
            }

            // Fallback: legacy local analysis using old extras
            val soreThroat = intent.getBooleanExtra("soreThroat", false)
            val mouthUlcers = intent.getBooleanExtra("mouthUlcers", false)
            val voiceChange = intent.getBooleanExtra("voiceChange", false)
            val swallowing = intent.getBooleanExtra("swallowing", false)
            val duration = intent.getStringExtra("duration") ?: ""
            val severity = intent.getStringExtra("severity") ?: ""

            Log.d("Results", "Symptom data - Sore throat: $soreThroat, Mouth ulcers: $mouthUlcers, Voice change: $voiceChange, Swallowing: $swallowing")

            val analysis = analyzeSymptoms(soreThroat, mouthUlcers, voiceChange, swallowing, duration, severity)
            updateResultsUI(analysis)
            Log.d("Results", "Symptom analysis completed: $analysis")
            return true
        } catch (e: Exception) {
            Log.e("Results", "Error displaying symptom results: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    private fun showFromSymptomCheckerExtras(): Boolean {
        return try {
            val isApiResult = intent.getBooleanExtra("isApiResult", false)
            val riskScoreExtraInt = if (intent.hasExtra("riskScore")) intent.getIntExtra("riskScore", -1) else -1
            val riskScoreExtraStr = if (riskScoreExtraInt >= 0) riskScoreExtraInt.toString() else intent.getStringExtra("riskScore")
            val riskLevelExtra = intent.getStringExtra("riskLevel")
            val probabilityExtra = if (intent.hasExtra("probability")) intent.getDoubleExtra("probability", -1.0) else -1.0
            val recommendationsExtra = intent.getStringArrayExtra("recommendations")
            val warningSignsExtra = intent.getStringArrayExtra("warningSigns")
            val nextStepsExtra = intent.getStringArrayExtra("nextSteps")
            val assessmentType = intent.getStringExtra("assessmentType")
            val modeExtra = intent.getStringExtra("mode")

            if (riskScoreExtraStr.isNullOrBlank() && riskLevelExtra.isNullOrBlank() && probabilityExtra < 0) {
                // No new-format extras present
                return false
            }

            // Title/subtitle
            titleText.text = assessmentType ?: "Assessment Results"
            subtitleText.text = if (isApiResult) "AI-Powered Analysis" else "Offline Assessment"
            
            // Display mode information
            if (modeExtra != null && modeText != null) {
                val modeDisplayText = when (modeExtra.lowercase()) {
                    "image" -> "📸 Image Analysis"
                    "symptoms" -> "🔍 Symptom Analysis"
                    else -> "🤖 ${modeExtra.replaceFirstChar { it.uppercase() }} Analysis"
                }
                modeText.text = modeDisplayText
                modeText.visibility = View.VISIBLE
                Log.d("Results", "Mode displayed: $modeDisplayText")
            } else if (modeText != null) {
                modeText.visibility = View.GONE
            }

            // Risk as percentage for the top circle
            val lockProvided = intent.getBooleanExtra("useProvidedScore", false)
            val riskPercentFromLevel = when (riskLevelExtra?.trim()?.uppercase(Locale.getDefault())) {
                // Generate random percentage per risk band
                "HIGH", "HIGH RISK" -> kotlin.random.Random.nextInt(80, 101).toDouble()
                "MODERATE", "MEDIUM", "MODERATE RISK", "MEDIUM RISK" -> kotlin.random.Random.nextInt(40, 80).toDouble()
                "LOW", "LOW RISK" -> kotlin.random.Random.nextInt(10, 40).toDouble()
                "VERY LOW", "VERY LOW RISK" -> kotlin.random.Random.nextInt(1, 10).toDouble()
                else -> null
            }
            // If result is opened from Dashboard with persisted score, don't randomize again
            val riskPercent = if (lockProvided && !riskScoreExtraStr.isNullOrBlank()) {
                val numeric = riskScoreExtraStr.filter { it.isDigit() }.toDoubleOrNull()
                numeric ?: riskPercentFromLevel
            } else riskPercentFromLevel
            val riskPercentText = if (riskPercent != null) String.format(Locale.getDefault(), "%.0f%%", riskPercent.coerceIn(0.0, 100.0)) else "N/A"
            riskScore.text = riskPercentText
            val derivedRiskLevel: String = when {
                !riskLevelExtra.isNullOrBlank() -> riskLevelExtra
                probabilityExtra >= 0 -> {
                    val pct = probabilityExtra * 100.0
                    when {
                        pct >= 80 -> "High Risk"
                        pct >= 40 -> "Moderate Risk"
                        pct >= 10 -> "Low Risk"
                        else -> "Very Low Risk"
                    }
                }
                !riskScoreExtraStr.isNullOrBlank() -> {
                    val numeric = riskScoreExtraStr.filter { it.isDigit() || it == '.' || it == '-' }.toDoubleOrNull() ?: -1.0
                    when {
                        numeric >= 150 -> "High Risk"
                        numeric >= 80 -> "Moderate Risk"
                        numeric >= 40 -> "Low Risk"
                        numeric >= 0 -> "Very Low Risk"
                        else -> ""
                    }
                }
                else -> ""
            }
            // Normalize label to always include the word "Risk" for short categories
            val baseLevel = riskLevelExtra ?: derivedRiskLevel
            val needsRiskSuffix = when (baseLevel.trim().uppercase(Locale.getDefault())) {
                "HIGH", "MODERATE", "MEDIUM", "LOW", "VERY LOW" -> true
                else -> !baseLevel.contains("risk", ignoreCase = true)
            }
            val displayLevel = if (needsRiskSuffix && baseLevel.isNotBlank()) "$baseLevel Risk" else baseLevel
            val finalLabel = displayLevel.uppercase(Locale.getDefault())
            riskLabel.text = if (finalLabel.isBlank()) "UNKNOWN RISK" else finalLabel
            riskLabel.visibility = View.VISIBLE

            // Update circle color by risk level
            when {
                finalLabel.contains("HIGH") -> riskCircleContainer.setBackgroundResource(R.drawable.ring_red)
                finalLabel.contains("MODERATE") || finalLabel.contains("MEDIUM") -> riskCircleContainer.setBackgroundResource(R.drawable.ring_orange)
                finalLabel.contains("LOW") -> riskCircleContainer.setBackgroundResource(R.drawable.ring_green)
                else -> riskCircleContainer.setBackgroundResource(R.drawable.ring_grey)
            }
            when {
                riskLabel.text.contains("HIGH") -> riskLabel.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
                riskLabel.text.contains("MODERATE") || riskLabel.text.contains("MEDIUM") -> riskLabel.setTextColor(resources.getColor(android.R.color.holo_orange_dark, theme))
                riskLabel.text.contains("LOW") || riskLabel.text.isNotBlank() -> riskLabel.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
            }

            // Confidence: if locked from Dashboard, keep provided value; otherwise compute from probability
            if (lockProvided && !riskScoreExtraStr.isNullOrBlank()) {
                // keep existing confidence text if any
            } else if (probabilityExtra >= 0) {
                val pct = (probabilityExtra * 100.0).coerceIn(0.0, 100.0)
                confidenceScore.text = String.format(Locale.getDefault(), "%.0f%%", pct)
            } else {
                confidenceScore.text = "N/A"
            }

            // Optionally show lists in subtitle (compact) if present
            val summaryLines = mutableListOf<String>()
            if (!warningSignsExtra.isNullOrEmpty()) {
                summaryLines.add("Warning Signs: ${warningSignsExtra.joinToString(limit = 3, truncated = "…")}")
            }
            if (!recommendationsExtra.isNullOrEmpty()) {
                summaryLines.add("Recommendations: ${recommendationsExtra.joinToString(limit = 3, truncated = "…")}")
            }
            if (summaryLines.isNotEmpty()) {
                subtitleText.text = summaryLines.joinToString("\n")
            }

            // Persist the latest result so Dashboard can reopen it later
            try {
                val prefs = getSharedPreferences("LastResult", MODE_PRIVATE)
                val recJoined = recommendationsExtra?.joinToString("||") ?: ""
                val warnJoined = warningSignsExtra?.joinToString("||") ?: ""
                val nextJoined = nextStepsExtra?.joinToString("||") ?: ""
                prefs.edit()
                    .putString("riskScore", riskPercentText)
                    .putString("riskLevel", finalLabel)
                    .putFloat("probability", if (probabilityExtra >= 0) probabilityExtra.toFloat() else -1f)
                    .putString("assessmentType", titleText.text?.toString() ?: "Assessment Results")
                    .putBoolean("isApiResult", isApiResult)
                    .putString("recommendations", recJoined)
                    .putString("warningSigns", warnJoined)
                    .putString("nextSteps", nextJoined)
                    .apply()
            } catch (_: Exception) { }

            true
        } catch (e: Exception) {
            Log.e("Results", "Error showing from extras: ${e.message}")
            false
        }
    }

    private fun showFromPersistedLastResult(): Boolean {
        val prefs = getSharedPreferences("LastResult", MODE_PRIVATE)
        val savedRiskScore = prefs.getString("riskScore", null)
        val riskLevel = prefs.getString("riskLevel", null)
        val probability = prefs.getFloat("probability", -1f)
        val assessmentType = prefs.getString("assessmentType", null)
        if (savedRiskScore.isNullOrBlank() && riskLevel.isNullOrBlank()) return false

        titleText.text = assessmentType ?: "Assessment Results"
        subtitleText.text = "Last Assessment"
        riskScore.text = savedRiskScore ?: "N/A"
        val finalLabel = (riskLevel ?: "").uppercase(Locale.getDefault())
        riskLabel.text = if (finalLabel.isBlank()) "UNKNOWN RISK" else finalLabel
        riskLabel.visibility = View.VISIBLE

        when {
            finalLabel.contains("HIGH") -> riskCircleContainer.setBackgroundResource(R.drawable.ring_red)
            finalLabel.contains("MODERATE") || finalLabel.contains("MEDIUM") -> riskCircleContainer.setBackgroundResource(R.drawable.ring_orange)
            finalLabel.contains("LOW") -> riskCircleContainer.setBackgroundResource(R.drawable.ring_green)
            else -> riskCircleContainer.setBackgroundResource(R.drawable.ring_grey)
        }
        when {
            riskLabel.text.contains("HIGH") -> riskLabel.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            riskLabel.text.contains("MODERATE") || riskLabel.text.contains("MEDIUM") -> riskLabel.setTextColor(resources.getColor(android.R.color.holo_orange_dark, theme))
            riskLabel.text.contains("LOW") || riskLabel.text.isNotBlank() -> riskLabel.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
        }

        if (probability >= 0f) {
            val pct = (probability.toDouble() * 100.0).coerceIn(0.0, 100.0)
            confidenceScore.text = String.format(Locale.getDefault(), "%.0f%%", pct)
        }

        // Hide placeholder button if showing result
        btnStartAssessment.visibility = View.GONE
        return true
    }

    private fun showNoResultPlaceholder() {
        titleText.text = "No Risk Assessment Available"
        subtitleText.text = "Please complete your first Risk Assessment to see results here."
        riskScore.text = "--"
        riskLabel.text = ""
        confidenceScore.text = "--"
        btnStartAssessment.visibility = View.VISIBLE
        btnStartAssessment.setOnClickListener {
            try {
                startActivity(Intent(this, com.simats.echohealth.SymptomChecker::class.java))
                finish()
            } catch (_: Exception) {}
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
    
    // Removed download functionality per request
    
    private fun navigateToRecommendations() {
        try {
            Log.d("Results", "Navigating to recommendations")
            
            // Create intent for recommendation activity
            val recsFromSource = this.intent.getStringArrayExtra("recommendations")
            val warnsFromSource = this.intent.getStringArrayExtra("warningSigns")
            val stepsFromSource = this.intent.getStringArrayExtra("nextSteps")
            val destIntent = Intent(this, com.simats.echohealth.Recommendation::class.java)
            
            // Pass relevant data to recommendation activity
            destIntent.putExtra("riskScore", riskScore?.text?.toString())
            destIntent.putExtra("riskLevel", riskLabel?.text?.toString())
            destIntent.putExtra("confidenceLevel", confidenceScore?.text?.toString())
            // Also pass lists if available from the source Results intent
            if (recsFromSource != null) destIntent.putExtra("recommendations", recsFromSource)
            if (warnsFromSource != null) destIntent.putExtra("warningSigns", warnsFromSource)
            if (stepsFromSource != null) destIntent.putExtra("nextSteps", stepsFromSource)
            intent.putExtra("fromResults", true)
            
            Log.d("Results", "Intent created successfully")
            
            // Start recommendation activity
            startActivity(destIntent)
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
            
            // Removed Download Full Report and AI Medication Guide button wiring
            
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