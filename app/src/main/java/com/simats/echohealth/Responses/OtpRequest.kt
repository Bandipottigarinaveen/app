package com.simats.echohealth.Responses
import  android.os.Message
data class RequestOtpRequest(
	val email: String
)

data class RequestOtpResponse(
	val message: String
)


