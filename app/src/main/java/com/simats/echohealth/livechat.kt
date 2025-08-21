package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*
import com.simats.echohealth.Responses.ChatRequest
import com.simats.echohealth.Responses.ChatResponse
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.NetworkUtils
import com.simats.echohealth.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LiveChat : AppCompatActivity() {
    
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var backButton: ImageView
    private lateinit var messagesLayout: LinearLayout
    private lateinit var messagesContainer: androidx.core.widget.NestedScrollView
    private var isLoading: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LiveChat", "LiveChat onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d("LiveChat", "Edge to edge enabled")
            
            setContentView(R.layout.activity_livechat)
            Log.d("LiveChat", "LiveChat layout set successfully")
            
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            Log.d("LiveChat", "Window insets setup successful")
        } catch (e: Exception) {
            Log.e("LiveChat", "Error in onCreate setup: ${e.message}")
            Log.e("LiveChat", "Stack trace: ${e.stackTraceToString()}")
        }

        // Initialize UI elements
        initializeViews()
        
        // Setup functionality
        setupBackNavigation()
        setupMessageInput()
        setupSendButton()
        
        Log.d("LiveChat", "LiveChat onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d("LiveChat", "Initializing views...")
            
            messageInput = findViewById(R.id.messageInput)
            sendButton = findViewById(R.id.sendButton)
            backButton = findViewById(R.id.backButton)
            messagesLayout = findViewById(R.id.messagesLayout)
            messagesContainer = findViewById(R.id.messagesContainer)
            
            Log.d("LiveChat", "Views initialized successfully")
            Log.d("LiveChat", "Message input: ${if (messageInput != null) "FOUND" else "NOT FOUND"}")
            Log.d("LiveChat", "Send button: ${if (sendButton != null) "FOUND" else "NOT FOUND"}")
            Log.d("LiveChat", "Back button: ${if (backButton != null) "FOUND" else "NOT FOUND"}")
            
        } catch (e: Exception) {
            Log.e("LiveChat", "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupBackNavigation() {
        try {
            Log.d("LiveChat", "Setting up back navigation")
            
            if (backButton != null) {
                backButton.setOnClickListener {
                    Log.d("LiveChat", "Back button clicked - finishing activity")
                    finish()
                }
                
                Log.d("LiveChat", "Back navigation setup successful")
            } else {
                Log.e("LiveChat", "Back button not found")
            }
        } catch (e: Exception) {
            Log.e("LiveChat", "Error setting up back navigation: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupMessageInput() {
        try {
            Log.d("LiveChat", "Setting up message input")
            
            if (messageInput != null) {
                // Add text change listener to enable/disable send button
                messageInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        updateSendButtonState()
                    }
                    
                    override fun afterTextChanged(s: Editable?) {}
                })
                
                // Handle enter key press
                messageInput.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendMessage()
                        return@setOnEditorActionListener true
                    }
                    false
                }
                
                Log.d("LiveChat", "Message input setup successful")
            } else {
                Log.e("LiveChat", "Message input not found")
            }
        } catch (e: Exception) {
            Log.e("LiveChat", "Error setting up message input: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupSendButton() {
        try {
            Log.d("LiveChat", "Setting up send button")
            
            if (sendButton != null) {
                sendButton.setOnClickListener {
                    Log.d("LiveChat", "Send button clicked")
                    sendMessage()
                }
                
                // Initially disable send button
                updateSendButtonState()
                
                Log.d("LiveChat", "Send button setup successful")
            } else {
                Log.e("LiveChat", "Send button not found")
            }
        } catch (e: Exception) {
            Log.e("LiveChat", "Error setting up send button: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun updateSendButtonState() {
        try {
            val message = messageInput.text.toString().trim()
            val hasMessage = message.isNotEmpty()
            
            sendButton.isEnabled = hasMessage
            sendButton.alpha = if (hasMessage) 1.0f else 0.5f
            
            Log.d("LiveChat", "Send button state updated - enabled: $hasMessage")
        } catch (e: Exception) {
            Log.e("LiveChat", "Error updating send button state: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun sendMessage() {
        try {
            val message = messageInput.text.toString().trim()
            
            if (message.isNotEmpty()) {
                if (isLoading) {
                    Log.d("LiveChat", "Request in-flight; ignoring new send")
                    return
                }
                if (!NetworkUtils.isNetworkAvailable(this)) {
                    Log.d("LiveChat", "No network available")
                    addAssistantMessage("No internet connection. Please check your network and try again.")
                    scrollToBottom()
                    return
                }
                Log.d("LiveChat", "Sending message: $message")
                
                // Add user message to chat
                addUserMessage(message)
                
                // Clear input
                messageInput.text.clear()
                
                // Call backend chat API
                callChatApi(message)
                
                Log.d("LiveChat", "Message sent successfully")
            } else {
                Log.d("LiveChat", "Attempted to send empty message")
            }
        } catch (e: Exception) {
            Log.e("LiveChat", "Error sending message: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun addUserMessage(message: String) {
        try {
            val timestamp = getCurrentTimestamp()
            
            // Create user message layout
            val messageLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.END
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.message_margin)
                }
                orientation = LinearLayout.HORIZONTAL
            }
            
            // Create message bubble
            val messageBubble = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = resources.getDimensionPixelSize(R.dimen.message_margin_large)
                }
                background = getDrawable(R.drawable.bg_card_rounded)
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.message_padding),
                    resources.getDimensionPixelSize(R.dimen.message_padding),
                    resources.getDimensionPixelSize(R.dimen.message_padding),
                    resources.getDimensionPixelSize(R.dimen.message_padding)
                )
                elevation = 1f
            }
            
            // Create message text
            val messageText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = message
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.black, theme))
            }
            
            // Create timestamp
            val timestampText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.END
                    topMargin = resources.getDimensionPixelSize(R.dimen.timestamp_margin)
                }
                text = timestamp
                textSize = 10f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            }
            
            // Add views to layout
            messageBubble.addView(messageText)
            messageLayout.addView(messageBubble)
            
            // Add to messages container
            messagesLayout.addView(messageLayout)
            messagesLayout.addView(timestampText)
            
            // Scroll to bottom
            scrollToBottom()
            
            Log.d("LiveChat", "User message added to chat")
        } catch (e: Exception) {
            Log.e("LiveChat", "Error adding user message: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun callChatApi(userMessage: String) {
        try {
            isLoading = true
            updateSendButtonState()
            addAssistantMessage("Typing...")
            val placeholderIndex = messagesLayout.childCount - 2
            
            var token = getSharedPreferences("AuthPrefs", MODE_PRIVATE).getString("auth_token", null)
            if (token.isNullOrBlank()) {
                // Fallback to reset flow token if available
                token = getSharedPreferences("ResetFlow", MODE_PRIVATE).getString("reset_token", null)
            }
            Log.d("LiveChat", "Auth token present: ${!token.isNullOrBlank()}")
            if (token.isNullOrBlank()) {
                android.widget.Toast.makeText(this, "You're not authenticated. Please login.", android.widget.Toast.LENGTH_LONG).show()
            }
            // Use coroutines + suspend API
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = RetrofitClient
                        .getClient()
                        .create(ApiService::class.java)
                    val authHeader = token?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }
                    val body = ChatRequest(userMessage)
                    val response = api.chat(authHeader, body)
                    runOnUiThread {
                        isLoading = false
                        updateSendButtonState()
                        if (response.isSuccessful) {
                            val reply = response.body()?.response ?: "(no response)"
                            replaceAssistantPlaceholder(placeholderIndex, reply.ifBlank { "(no response)" })
                        } else {
                            replaceAssistantPlaceholder(placeholderIndex, "Error: ${response.code()}")
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        isLoading = false
                        updateSendButtonState()
                        replaceAssistantPlaceholder(placeholderIndex, "Error: ${e.localizedMessage}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LiveChat", "Error calling chat API: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun replaceAssistantPlaceholder(index: Int, message: String) {
        try {
            if (index >= 0 && index < messagesLayout.childCount) {
                val messageLayout = messagesLayout.getChildAt(index) as? LinearLayout
                val bubble = messageLayout?.getChildAt(0) as? LinearLayout
                val textView = bubble?.getChildAt(0) as? TextView
                textView?.text = message
            }
            scrollToBottom()
        } catch (e: Exception) {
            Log.e("LiveChat", "Error replacing placeholder: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun addAssistantMessage(message: String) {
        try {
            val timestamp = getCurrentTimestamp()
            
            // Create assistant message layout
            val messageLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.message_margin)
                }
                orientation = LinearLayout.HORIZONTAL
            }
            
            // Create message bubble
            val messageBubble = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.message_margin_large)
                }
                background = getDrawable(R.drawable.bg_card_rounded)
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.message_padding),
                    resources.getDimensionPixelSize(R.dimen.message_padding),
                    resources.getDimensionPixelSize(R.dimen.message_padding),
                    resources.getDimensionPixelSize(R.dimen.message_padding)
                )
                elevation = 1f
            }
            
            // Create message text
            val messageText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = message
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.black, theme))
            }
            
            // Create timestamp
            val timestampText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = resources.getDimensionPixelSize(R.dimen.timestamp_margin)
                    topMargin = resources.getDimensionPixelSize(R.dimen.timestamp_margin)
                }
                text = timestamp
                textSize = 10f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            }
            
            // Add views to layout
            messageBubble.addView(messageText)
            messageLayout.addView(messageBubble)
            
            // Add to messages container
            messagesLayout.addView(messageLayout)
            messagesLayout.addView(timestampText)
            
            // Scroll to bottom
            scrollToBottom()
            
            Log.d("LiveChat", "Assistant message added to chat")
        } catch (e: Exception) {
            Log.e("LiveChat", "Error adding assistant message: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun scrollToBottom() {
        try {
            messagesContainer.post {
                messagesContainer.fullScroll(View.FOCUS_DOWN)
            }
            Log.d("LiveChat", "Scrolled to bottom")
        } catch (e: Exception) {
            Log.e("LiveChat", "Error scrolling to bottom: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("LiveChat", "LiveChat onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("LiveChat", "LiveChat onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("LiveChat", "LiveChat onDestroy called")
    }
}