package com.simats.echohealth

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.echohealth.Retrofit.NetworkUtils

class ServerTestActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var testButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_test)
        
        statusText = findViewById(R.id.statusText)
        testButton = findViewById(R.id.testButton)

    }


}
