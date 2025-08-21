package com.simats.echohealth.chat

import com.simats.echohealth.Responses.ChatRequest
import com.simats.echohealth.Responses.ChatResponse
import com.simats.echohealth.Retrofit.ApiService
import retrofit2.Response

/**
 * Repository responsible for calling the backend via Retrofit.
 * Keeps networking concerns out of the ViewModel/UI.
 */
class ChatRepository(private val apiService: ApiService) {

    /**
     * Sends a message to the backend chatbot endpoint.
     * Only uses provided request/response models; no API keys or SDKs.
     */
    suspend fun sendMessage(authToken: String?, message: String): Response<ChatResponse> {
        val authHeader = authToken?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }
        val body = ChatRequest(message)
        return apiService.chat(authHeader, body)
    }
}


