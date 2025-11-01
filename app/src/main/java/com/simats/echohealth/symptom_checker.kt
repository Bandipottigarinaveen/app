package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.simats.echohealth.Responses.SymptomCheckRequest
import com.simats.echohealth.Responses.SymptomCheckResponse
import com.simats.echohealth.Responses.SymptomFeatures
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.NetworkUtils
import com.simats.echohealth.Retrofit.RetrofitClient
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SymptomChecker : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SymptomChecker"
    }
    
    // UI Elements
    private lateinit var backArrow: ImageView
    private lateinit var analyzeButton: Button
    
    // Age Input
    private lateinit var editTextAge: EditText
    
    // Gender Radio Groups
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var radioGenderMale: RadioButton
    private lateinit var radioGenderFemale: RadioButton
    private lateinit var radioGenderOther: RadioButton
    
    // Risk Factors Radio Groups
    private lateinit var radioGroupTobacco: RadioGroup
    private lateinit var radioTobaccoYes: RadioButton
    private lateinit var radioTobaccoNo: RadioButton
    
    private lateinit var radioGroupAlcohol: RadioGroup
    private lateinit var radioAlcoholYes: RadioButton
    private lateinit var radioAlcoholNo: RadioButton
    
    private lateinit var radioGroupHPV: RadioGroup
    private lateinit var radioHPVYes: RadioButton
    private lateinit var radioHPVNo: RadioButton
    
    private lateinit var radioGroupBetel: RadioGroup
    private lateinit var radioBetelYes: RadioButton
    private lateinit var radioBetelNo: RadioButton
    
    private lateinit var radioGroupHygiene: RadioGroup
    private lateinit var radioHygieneYes: RadioButton
    private lateinit var radioHygieneNo: RadioButton
    
    // Symptoms Radio Groups
    private lateinit var radioGroupLesions: RadioGroup
    private lateinit var radioLesionsYes: RadioButton
    private lateinit var radioLesionsNo: RadioButton
    
    private lateinit var radioGroupBleeding: RadioGroup
    private lateinit var radioBleedingYes: RadioButton
    private lateinit var radioBleedingNo: RadioButton
    
    private lateinit var radioGroupSwallowing: RadioGroup
    private lateinit var radioSwallowingYes: RadioButton
    private lateinit var radioSwallowingNo: RadioButton
    
    private lateinit var radioGroupPatches: RadioGroup
    private lateinit var radioPatchesYes: RadioButton
    private lateinit var radioPatchesNo: RadioButton
    
    private lateinit var radioGroupDiagnosis: RadioGroup
    private lateinit var radioDiagnosisYes: RadioButton
    private lateinit var radioDiagnosisNo: RadioButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "SymptomChecker activity created")
        
        try {
        enableEdgeToEdge()
        setContentView(R.layout.activity_symptom_checker)
            Log.d(TAG, "SymptomChecker layout set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate setup: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
        }
        
        initializeViews()
        setupNavigation()
        setupAnalyzeButton()
        
        Log.d(TAG, "SymptomChecker onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d(TAG, "Initializing views")
            
            // Navigation
            backArrow = findViewById(R.id.backArrow)
            analyzeButton = findViewById(R.id.btnAnalyze)
            
            // Age Input
            editTextAge = findViewById(R.id.editTextAge)
            
            // Gender Radio Groups
            radioGroupGender = findViewById(R.id.radioGroupGender)
            radioGenderMale = findViewById(R.id.radioGenderMale)
            radioGenderFemale = findViewById(R.id.radioGenderFemale)
            radioGenderOther = findViewById(R.id.radioGenderOther)
            
            // Risk Factors Radio Groups
            radioGroupTobacco = findViewById(R.id.radioGroupTobacco)
            radioTobaccoYes = findViewById(R.id.radioTobaccoYes)
            radioTobaccoNo = findViewById(R.id.radioTobaccoNo)
            
            radioGroupAlcohol = findViewById(R.id.radioGroupAlcohol)
            radioAlcoholYes = findViewById(R.id.radioAlcoholYes)
            radioAlcoholNo = findViewById(R.id.radioAlcoholNo)
            
            radioGroupHPV = findViewById(R.id.radioGroupHPV)
            radioHPVYes = findViewById(R.id.radioHPVYes)
            radioHPVNo = findViewById(R.id.radioHPVNo)
            
            radioGroupBetel = findViewById(R.id.radioGroupBetel)
            radioBetelYes = findViewById(R.id.radioBetelYes)
            radioBetelNo = findViewById(R.id.radioBetelNo)
            
            radioGroupHygiene = findViewById(R.id.radioGroupHygiene)
            radioHygieneYes = findViewById(R.id.radioHygieneYes)
            radioHygieneNo = findViewById(R.id.radioHygieneNo)
            
            // Symptoms Radio Groups
            radioGroupLesions = findViewById(R.id.radioGroupLesions)
            radioLesionsYes = findViewById(R.id.radioLesionsYes)
            radioLesionsNo = findViewById(R.id.radioLesionsNo)
            
            radioGroupBleeding = findViewById(R.id.radioGroupBleeding)
            radioBleedingYes = findViewById(R.id.radioBleedingYes)
            radioBleedingNo = findViewById(R.id.radioBleedingNo)
            
            radioGroupSwallowing = findViewById(R.id.radioGroupSwallowing)
            radioSwallowingYes = findViewById(R.id.radioSwallowingYes)
            radioSwallowingNo = findViewById(R.id.radioSwallowingNo)
            
            radioGroupPatches = findViewById(R.id.radioGroupPatches)
            radioPatchesYes = findViewById(R.id.radioPatchesYes)
            radioPatchesNo = findViewById(R.id.radioPatchesNo)
            
            radioGroupDiagnosis = findViewById(R.id.radioGroupDiagnosis)
            radioDiagnosisYes = findViewById(R.id.radioDiagnosisYes)
            radioDiagnosisNo = findViewById(R.id.radioDiagnosisNo)
            
            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupNavigation() {
        try {
            Log.d(TAG, "Setting up navigation")
            
            backArrow.setOnClickListener {
                Log.d(TAG, "Back arrow clicked")
                onBackPressed()
            }
            
            Log.d(TAG, "Navigation setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupAnalyzeButton() {
        try {
            Log.d(TAG, "Setting up analyze button")
            
            analyzeButton.setOnClickListener {
                Log.d(TAG, "Analyze button clicked")
                performRiskAssessment()
            }
            
            Log.d(TAG, "Analyze button setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up analyze button: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun performRiskAssessment() {
        try {
            Log.d(TAG, "Performing risk assessment")
            
            // Validate that all questions are answered
            if (!validateAllQuestionsAnswered()) {
                Toast.makeText(this, "Please answer all questions before analyzing", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Check network connectivity
            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection. Using offline assessment.", Toast.LENGTH_SHORT).show()
                performOfflineAssessment()
                return
            }
            
            // Disable button to prevent multiple submissions
            analyzeButton.isEnabled = false
            analyzeButton.text = "Analyzing..."
            
            // Create API request
            val request = createSymptomCheckRequest()
            Log.d(TAG, "Symptom check request: $request")
            
            // Try with auth first, fallback to no auth
            val api = RetrofitClient.getClient().create(ApiService::class.java)
            val token = com.simats.echohealth.auth.AuthManager.getAuthToken(this)
            val call = if (!token.isNullOrBlank()) {
                Log.d(TAG, "Using authenticated API call")
                api.symptomCheck("Bearer $token", request)
            } else {
                Log.d(TAG, "No auth token, trying without authentication")
                api.symptomCheck(request)
            }
            
            call.enqueue(object : Callback<SymptomCheckResponse> {
                override fun onResponse(call: Call<SymptomCheckResponse>, response: Response<SymptomCheckResponse>) {
                    Log.d(TAG, "Symptom check response received")
                    Log.d(TAG, "Response code: ${response.code()}")
                    Log.d(TAG, "Response body: ${response.body()}")
                    
                    // Re-enable button
                    analyzeButton.isEnabled = true
                    analyzeButton.text = "Analyze"
                    
                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!
                        Log.d(TAG, "API assessment completed - Score: ${apiResponse.risk_score}, Level: ${apiResponse.risk_level}")

                        // Map varying backend fields to a consistent intent contract
                        val probability0to1: Double? = when {
                            apiResponse.probability != null -> apiResponse.probability
                            apiResponse.prediction_percentage != null -> (apiResponse.prediction_percentage / 100.0).coerceIn(0.0, 1.0)
                            else -> null
                        }
                        val riskScoreForDisplay: String = when {
                            apiResponse.risk_score != null -> apiResponse.risk_score.toString()
                            probability0to1 != null -> String.format(Locale.getDefault(), "%.1f", (probability0to1 * 10.0))
                            !apiResponse.risk_level.isNullOrBlank() -> when (apiResponse.risk_level.trim().lowercase(Locale.getDefault())) {
                                "high", "high risk" -> "8.5"
                                "moderate", "medium", "moderate risk", "medium risk" -> "5.5"
                                "low", "low risk" -> "2.5"
                                "very low", "very low risk" -> "1.0"
                                else -> "N/A"
                            }
                            else -> "N/A"
                        }
                        val riskLevelForDisplay: String = apiResponse.risk_level ?: run {
                            val pct = when {
                                apiResponse.probability != null -> apiResponse.probability * 100.0
                                apiResponse.prediction_percentage != null -> apiResponse.prediction_percentage
                                else -> null
                            }
                            when {
                                pct == null -> "Unknown"
                                pct >= 80 -> "High Risk"
                                pct >= 40 -> "Moderate Risk"
                                pct >= 10 -> "Low Risk"
                                else -> "Very Low Risk"
                            }
                        }

                        val intent = Intent(this@SymptomChecker, Results::class.java).apply {
                            putExtra("riskScore", riskScoreForDisplay)
                            putExtra("riskLevel", riskLevelForDisplay)
                            probability0to1?.let { putExtra("probability", it) }
                            putExtra("recommendations", (apiResponse.recommendations ?: emptyList()).toTypedArray())
                            putExtra("warningSigns", (apiResponse.warning_signs ?: emptyList()).toTypedArray())
                            putExtra("nextSteps", (apiResponse.next_steps ?: emptyList()).toTypedArray())
                            putExtra("message", apiResponse.message ?: "")
                            putExtra("assessmentType", "AI-Powered Oral Cancer Risk Assessment")
                            putExtra("isApiResult", true)
                        }
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "API call failed: ${response.code()}")
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                        Log.e(TAG, "Error details: $errorBody")
                        if (response.code() == 401) {
                            Toast.makeText(this@SymptomChecker, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
                            // Re-enable button and do not fallback to offline in auth error
                            analyzeButton.isEnabled = true
                            analyzeButton.text = "Analyze"
                            return
                        }
                        
                        Toast.makeText(this@SymptomChecker, "API assessment failed. Using offline assessment.", Toast.LENGTH_LONG).show()
                        performOfflineAssessment()
                    }
                }
                
                override fun onFailure(call: Call<SymptomCheckResponse>, t: Throwable) {
                    Log.e(TAG, "Symptom check network failure", t)
                    
                    // Re-enable button
                    analyzeButton.isEnabled = true
                    analyzeButton.text = "Analyze"
                    
                    Toast.makeText(this@SymptomChecker, "Network error. Using offline assessment.", Toast.LENGTH_LONG).show()
                    performOfflineAssessment()
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Error performing risk assessment: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error performing risk assessment", Toast.LENGTH_SHORT).show()
            
            // Re-enable button
            analyzeButton.isEnabled = true
            analyzeButton.text = "Analyze"
        }
    }
    
    private fun validateAllQuestionsAnswered(): Boolean {
        try {
            // Check age input
            val ageText = editTextAge.text.toString().trim()
            if (ageText.isEmpty()) {
                Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show()
                return false
            }
            
            val age = ageText.toIntOrNull()
            if (age == null || age <= 0 || age > 120) {
                Toast.makeText(this, "Please enter a valid age (1-120)", Toast.LENGTH_SHORT).show()
                return false
            }
            
            // Check if all radio groups have a selection
            val genderSelected = radioGroupGender.checkedRadioButtonId != -1
            val tobaccoSelected = radioGroupTobacco.checkedRadioButtonId != -1
            val alcoholSelected = radioGroupAlcohol.checkedRadioButtonId != -1
            val hpvSelected = radioGroupHPV.checkedRadioButtonId != -1
            val betelSelected = radioGroupBetel.checkedRadioButtonId != -1
            val hygieneSelected = radioGroupHygiene.checkedRadioButtonId != -1
            val lesionsSelected = radioGroupLesions.checkedRadioButtonId != -1
            val bleedingSelected = radioGroupBleeding.checkedRadioButtonId != -1
            val swallowingSelected = radioGroupSwallowing.checkedRadioButtonId != -1
            val patchesSelected = radioGroupPatches.checkedRadioButtonId != -1
            val diagnosisSelected = radioGroupDiagnosis.checkedRadioButtonId != -1
            
            return genderSelected && tobaccoSelected && alcoholSelected && 
                   hpvSelected && betelSelected && hygieneSelected && lesionsSelected && 
                   bleedingSelected && swallowingSelected && patchesSelected && diagnosisSelected
                   
        } catch (e: Exception) {
            Log.e(TAG, "Error validating questions: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    private fun calculateRiskScore(): Int {
        try {
            var riskScore = 0
            
            // Age factor (40+ is higher risk)
            val ageText = editTextAge.text.toString().trim()
            val age = ageText.toIntOrNull() ?: 0
            
            if (age >= 40) {
                riskScore += 15
                Log.d(TAG, "Age $age (40+) selected: +15 points")
            } else {
                Log.d(TAG, "Age $age (under 40) selected: +0 points")
            }
            
            // Gender factor (males have higher risk, other gender has moderate risk)
            when {
                radioGenderMale.isChecked -> {
                    riskScore += 10
                    Log.d(TAG, "Male selected: +10 points")
                }
                radioGenderFemale.isChecked -> {
                    riskScore += 5
                    Log.d(TAG, "Female selected: +5 points")
                }
                radioGenderOther.isChecked -> {
                    riskScore += 7
                    Log.d(TAG, "Other gender selected: +7 points")
                }
            }
            
            // Risk factors
            if (radioTobaccoYes.isChecked) {
                riskScore += 25
                Log.d(TAG, "Tobacco use selected: +25 points")
            }
            
            if (radioAlcoholYes.isChecked) {
                riskScore += 20
                Log.d(TAG, "Alcohol consumption selected: +20 points")
            }
            
            if (radioHPVYes.isChecked) {
                riskScore += 30
                Log.d(TAG, "HPV infection selected: +30 points")
            }
            
            if (radioBetelYes.isChecked) {
                riskScore += 25
                Log.d(TAG, "Betel quid use selected: +25 points")
            }
            
            if (radioHygieneYes.isChecked) {
                riskScore += 10
                Log.d(TAG, "Poor oral hygiene selected: +10 points")
            }
            
            // Symptoms (higher weight for symptoms)
            if (radioLesionsYes.isChecked) {
                riskScore += 35
                Log.d(TAG, "Oral lesions selected: +35 points")
            }
            
            if (radioBleedingYes.isChecked) {
                riskScore += 40
                Log.d(TAG, "Unexplained bleeding selected: +40 points")
            }
            
            if (radioSwallowingYes.isChecked) {
                riskScore += 30
                Log.d(TAG, "Difficulty swallowing selected: +30 points")
            }
            
            if (radioPatchesYes.isChecked) {
                riskScore += 35
                Log.d(TAG, "White/red patches selected: +35 points")
            }
            
            if (radioDiagnosisYes.isChecked) {
                riskScore += 50
                Log.d(TAG, "Oral cancer diagnosis selected: +50 points")
            }
            
            Log.d(TAG, "Total risk score calculated: $riskScore")
            return riskScore
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating risk score: ${e.message}")
            e.printStackTrace()
            return 0
        }
    }
    
    private fun determineRiskLevel(riskScore: Int): String {
        return when {
            riskScore >= 150 -> "High Risk"
            riskScore >= 80 -> "Moderate Risk"
            riskScore >= 40 -> "Low Risk"
            else -> "Very Low Risk"
        }
    }
    
    private fun generateRecommendations(riskScore: Int): List<String> {
        val recommendations = mutableListOf<String>()
        
        when {
            riskScore >= 150 -> {
                recommendations.add("Immediate medical consultation recommended")
                recommendations.add("Schedule an appointment with an oral specialist")
                recommendations.add("Consider biopsy for suspicious lesions")
                recommendations.add("Stop tobacco and alcohol use immediately")
                recommendations.add("Regular monitoring every 3 months")
            }
            riskScore >= 80 -> {
                recommendations.add("Schedule a dental checkup within 2 weeks")
                recommendations.add("Consider specialist consultation")
                recommendations.add("Reduce or eliminate tobacco/alcohol use")
                recommendations.add("Improve oral hygiene practices")
                recommendations.add("Monitor symptoms closely")
            }
            riskScore >= 40 -> {
                recommendations.add("Regular dental checkups every 6 months")
                recommendations.add("Maintain good oral hygiene")
                recommendations.add("Limit alcohol consumption")
                recommendations.add("Avoid tobacco products")
                recommendations.add("Monitor for new symptoms")
            }
            else -> {
                recommendations.add("Continue regular dental checkups")
                recommendations.add("Maintain good oral hygiene")
                recommendations.add("Avoid tobacco and excessive alcohol")
                recommendations.add("Stay informed about oral health")
                recommendations.add("Report any new symptoms promptly")
            }
        }
        
        return recommendations
    }
    
    private fun createSymptomCheckRequest(): SymptomCheckRequest {
        val ageText = editTextAge.text.toString().trim()
        val age = ageText.toIntOrNull() ?: 0
        
        val gender = when {
            radioGenderMale.isChecked -> "Male"
            radioGenderFemale.isChecked -> "Female"
            radioGenderOther.isChecked -> "Other"
            else -> "Unknown"
        }
        
        fun yn(checked: Boolean): Int = if (checked) 1 else 0
        
        val features = SymptomFeatures(
            Age = age,
            Gender = gender,
            Tobacco_Use = yn(radioTobaccoYes.isChecked),
            Alcohol_Consumption = yn(radioAlcoholYes.isChecked),
            HPV_Infection = yn(radioHPVYes.isChecked),
            Betel_Quid_Use = yn(radioBetelYes.isChecked),
            Poor_Oral_Hygiene = yn(radioHygieneYes.isChecked),
            Oral_Lesions = yn(radioLesionsYes.isChecked),
            Unexplained_Bleeding = yn(radioBleedingYes.isChecked),
            Difficulty_Swallowing = yn(radioSwallowingYes.isChecked),
            White_or_Red_Patches_in_Mouth = yn(radioPatchesYes.isChecked),
            Oral_Cancer_Diagnosis = yn(radioDiagnosisYes.isChecked)
        )
        
        return SymptomCheckRequest(
            mode = "symptoms",
            features = features
        )
    }
    
    private fun performOfflineAssessment() {
        try {
            Log.d(TAG, "Performing offline risk assessment")
            
            // Calculate risk score using local logic
            val riskScore = calculateRiskScore()
            val riskLevel = determineRiskLevel(riskScore)
            val recommendations = generateRecommendations(riskScore)
            
            Log.d(TAG, "Offline assessment completed - Score: $riskScore, Level: $riskLevel")
            
            // Navigate to results with offline data
            navigateToResults(riskScore, riskLevel, recommendations)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error performing offline assessment: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error performing assessment", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToResultsFromAPI(apiResponse: SymptomCheckResponse) {
        try {
            Log.d(TAG, "Navigating to results with API data")
            
            val intent = Intent(this, Results::class.java).apply {
                putExtra("riskScore", apiResponse.risk_score)
                putExtra("riskLevel", apiResponse.risk_level)
                putExtra("probability", apiResponse.probability)
                putExtra("recommendations", (apiResponse.recommendations ?: emptyList()).toTypedArray())
                putExtra("warningSigns", (apiResponse.warning_signs ?: emptyList()).toTypedArray())
                putExtra("nextSteps", (apiResponse.next_steps ?: emptyList()).toTypedArray())
                putExtra("message", apiResponse.message)
                putExtra("assessmentType", "AI-Powered Oral Cancer Risk Assessment")
                putExtra("mode", "symptoms")
                putExtra("isApiResult", true)
            }
            
            // Results page will handle saving to both SharedPreferences and database
            Log.d(TAG, "API symptoms entry completed - Results page will save data")

            startActivity(intent)
            Log.d(TAG, "Successfully navigated to results with API data")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to results: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error displaying results", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToResults(riskScore: Int, riskLevel: String, recommendations: List<String>) {
        try {
            Log.d(TAG, "Navigating to results with score: $riskScore, level: $riskLevel")
            
            val intent = Intent(this, Results::class.java).apply {
                putExtra("riskScore", riskScore)
                putExtra("riskLevel", riskLevel)
                putExtra("recommendations", recommendations.toTypedArray())
                putExtra("assessmentType", "Offline Oral Cancer Risk Assessment")
                putExtra("isApiResult", false)
            }
            
            // Results page will handle saving to both SharedPreferences and database
            Log.d(TAG, "Offline symptoms entry completed - Results page will save data")

            startActivity(intent)
            Log.d(TAG, "Successfully navigated to results")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to results: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error displaying results", Toast.LENGTH_SHORT).show()
        }
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
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SymptomChecker onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "SymptomChecker onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SymptomChecker onDestroy called")
    }
}