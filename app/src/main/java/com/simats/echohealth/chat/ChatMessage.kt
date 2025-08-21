package com.simats.echohealth.chat

/**
 * UI model representing a single chat message
 */
data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean
)


