package com.simats.echohealth.Responses

data class SymptomCheckRequest(
    val mode: String = "symptoms",
    val features: SymptomFeatures
)

data class SymptomFeatures(
    val Age: Int,
    val Gender: String,
    val Tobacco_Use: Int,
    val Alcohol_Consumption: Int,
    val HPV_Infection: Int,
    val Betel_Quid_Use: Int,
    val Poor_Oral_Hygiene: Int,
    val Oral_Lesions: Int,
    val Unexplained_Bleeding: Int,
    val Difficulty_Swallowing: Int,
    val White_or_Red_Patches_in_Mouth: Int,
    val Oral_Cancer_Diagnosis: Int
)

// Broadened to support multiple backend response shapes
// Fields are optional and we derive display values in the UI layer

data class SymptomCheckResponse(
    val id: Int? = null,
    val risk_score: Int? = null,
    val risk_level: String? = null,
    val probability: Double? = null,              // 0..1 range if present
    val prediction_percentage: Double? = null,    // 0..100 range if present
    val recommendations: List<String>? = null,
    val warning_signs: List<String>? = null,
    val next_steps: List<String>? = null,
    val message: String? = null,
    val mode: String? = null,
    val created_at: String? = null
)
