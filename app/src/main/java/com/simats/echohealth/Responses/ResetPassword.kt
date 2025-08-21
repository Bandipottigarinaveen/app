package com.simats.echohealth.Responses

data class ResetRequest(
    val email: String,
    val password: String,
    val confirm_password: String
)

data class ResetResponse(
    val message: String
)
