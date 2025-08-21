package com.simats.echohealth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Terms : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terms)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupBackNavigation()
        setupButtonFunctionality()
    }
    
    private fun setupBackNavigation() {
        val backButton = findViewById<android.widget.ImageView>(R.id.back_button)
        backButton?.setOnClickListener {
            finish()
        }
    }
    
    private fun setupButtonFunctionality() {
        // Accept Button
        val acceptButton = findViewById<android.widget.Button>(R.id.accept_button)
        acceptButton?.setOnClickListener {
            // Show success message and navigate back
            android.widget.Toast.makeText(this, "Terms accepted successfully!", android.widget.Toast.LENGTH_SHORT).show()
            finish()
        }
        
        // Decline Button
        val declineButton = findViewById<android.widget.Button>(R.id.decline_button)
        declineButton?.setOnClickListener {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Decline Terms")
                .setMessage("Are you sure you want to decline the terms? You may not be able to use certain features.")
                .setPositiveButton("Yes, Decline") { _, _ ->
                    android.widget.Toast.makeText(this, "Terms declined", android.widget.Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}