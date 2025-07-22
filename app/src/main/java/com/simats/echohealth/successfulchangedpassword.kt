package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class successfulchangedpassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpassword_success)

        val goToLoginButton = findViewById<Button>(R.id.btn_go_to_login)
        goToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginpageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
} 