package com.simats.echohealth.chat

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.echohealth.R
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.RetrofitClient
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Simple Activity demonstrating how to wire the chat UI to the ViewModel.
 * - RecyclerView shows conversation (user on right, bot on left)
 * - sendMessage() is invoked on ViewModel with the request body and token
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val input: EditText = findViewById(R.id.editTextMessage)
        val send: TextView = findViewById(R.id.buttonSend)

        recyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        adapter = ChatAdapter()
        recyclerView.adapter = adapter

        val api = RetrofitClient.getClient().create(ApiService::class.java)
        val repo = ChatRepository(api)
        val factory = ChatViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        lifecycleScope.launch {
            viewModel.messages.collectLatest { list ->
                adapter.submitList(list) {
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        send.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                val token = getSharedPreferences("AuthPrefs", MODE_PRIVATE).getString("auth_token", null)
                viewModel.sendMessage(token, text)
                input.text.clear()
            }
        }
    }
}


