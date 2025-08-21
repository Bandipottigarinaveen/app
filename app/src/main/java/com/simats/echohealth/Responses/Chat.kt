package com.simats.echohealth.Responses

data class ChatRequest(
	val message: String
)

data class ChatResponse(
	val response: String? = null
)


