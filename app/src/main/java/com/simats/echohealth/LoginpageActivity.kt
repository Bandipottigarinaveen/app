package com.simats.echohealth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class LoginpageActivity : AppCompatActivity() {
    lateinit var loginBtn : Button
    lateinit var forgotpassBtn : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.loginpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Navigate to createaccount when Sign Up is clicked
        val signUpText = findViewById<TextView>(R.id.signIn)
        signUpText.setOnClickListener {
            val intent = Intent(this, createaccount::class.java)
            startActivity(intent)
        }
        loginBtn.setOnClickListener {
            val intent = Intent(this, dashboard::class.java)
            startActivity(intent)
        }
        forgotpassBtn.setOnClickListener {
            val intent = Intent(this, forgotpassword_page::class.java)
            startActivity(intent)
        }
    }
}