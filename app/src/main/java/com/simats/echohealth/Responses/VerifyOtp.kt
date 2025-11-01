package com.simats.echohealth.Responses

import  android.os.Message
data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val message: String,
    val token: String? = null
)
