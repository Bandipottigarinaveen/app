package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.animation.AnimationUtils

class MainActivity : AppCompatActivity() {
    private lateinit var logoImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the logo ImageView
        logoImageView = findViewById(R.id.logo)

        // Start the logo animation
        startLogoAnimation()

        // Find the Get Started button
        val getStartedButton = findViewById<Button>(R.id.getStartedBtn)

        // Set click listener to navigate to LoginPageActivity
        getStartedButton.setOnClickListener {
            val intent = Intent(this, LoginpageActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        // Only start animation if this activity is actually visible
        if (!isFinishing) {
            startLogoAnimation()
        }
    }

    private fun startLogoAnimation() {
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_popup_animation)
        logoImageView.startAnimation(logoAnimation)
    }
}