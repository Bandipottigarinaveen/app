package com.simats.echohealth.Responses

data class OralCancerDetectResponse(
    val id: Int? = null,
    val prediction: String?,
    val risk_level: String?,
    val prediction_percentage: Double?,
    val mode: String? = null,
    val created_at: String? = null,
    val user: Int? = null,
    // Legacy fields for backward compatibility
    val success: Boolean = true,
    val message: String? = null,
    val confidence: Double? = null,
    val recommendations: List<String>? = null,
    val warning_signs: List<String>? = null,
    val next_steps: List<String>? = null
)
