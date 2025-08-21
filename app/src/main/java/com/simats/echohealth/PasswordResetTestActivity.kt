package com.simats.echohealth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordResetTestActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var requestOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var resetPasswordButton: Button
    private lateinit var resultText: TextView

    private var currentToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset_test)

        emailInput = findViewById(R.id.emailInput)
        otpInput = findViewById(R.id.otpInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        requestOtpButton = findViewById(R.id.requestOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        resultText = findViewById(R.id.resultText)

        requestOtpButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resultText.text = "Would request OTP for $email (integration removed)"
        }

        verifyOtpButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val otp = otpInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter an OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resultText.text = "Would verify OTP $otp for $email (integration removed)"
        }

        resetPasswordButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentToken == null) {
                // Only allow reset with OTP verification token
                currentToken = getSharedPreferences("ResetFlow", MODE_PRIVATE).getString("reset_token", null)
            }
            if (currentToken == null) {
                Toast.makeText(this, "Please verify OTP first or login again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resultText.text = "Would reset password for $email (integration removed)"
        }
    }
}
