package com.simats.echohealth.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes chat messages and accepts user input.
 * Uses Kotlin coroutines and StateFlow for lifecycle-aware updates.
 */
class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(authToken: String?, text: String) {
        if (text.isBlank() || _isLoading.value) return

        val userItem = ChatMessage(id = System.nanoTime(), text = text, isUser = true)
        val typingItem = ChatMessage(id = Long.MAX_VALUE, text = "Typing...", isUser = false)
        _messages.value = _messages.value + userItem + typingItem

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.sendMessage(authToken, text)
                val replyText = if (response.isSuccessful) {
                    response.body()?.response?.takeIf { !it.isNullOrBlank() } ?: "(no response)"
                } else {
                    "Error: ${response.code()}"
                }
                replaceTyping(replyText)
            } catch (e: Exception) {
                replaceTyping("Error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun replaceTyping(newText: String) {
        val current = _messages.value.toMutableList()
        val index = current.indexOfLast { it.id == Long.MAX_VALUE }
        if (index >= 0) {
            current[index] = current[index].copy(text = newText)
        }
        _messages.value = current
    }
}


