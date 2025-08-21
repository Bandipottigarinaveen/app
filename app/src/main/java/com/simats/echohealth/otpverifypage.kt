package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.simats.echohealth.Responses.VerifyOtpRequest
import com.simats.echohealth.Responses.VerifyOtpResponse
import com.simats.echohealth.Retrofit.ApiService
import com.simats.echohealth.Retrofit.NetworkUtils
import com.simats.echohealth.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class otpverification : AppCompatActivity() {
    companion object {
        private const val TAG = "OTPVerification"
    }

    private lateinit var verifyBtn: Button
    private lateinit var backBtn: Button
    private lateinit var otp1: EditText
    private lateinit var otp2: EditText
    private lateinit var otp3: EditText
    private lateinit var otp4: EditText
    private lateinit var otp5: EditText
    private lateinit var otp6: EditText
    private lateinit var countdownText: TextView
    private lateinit var emailDisplay: TextView
    
    private var email: String = ""
    private var countDownTimer: android.os.CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otpverifypage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupOtpInputListeners()
        setupClickListeners()
        startCountdown()
    }

    private fun initializeViews() {
        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)
        otp5 = findViewById(R.id.otp5)
        otp6 = findViewById(R.id.otp6)
        verifyBtn = findViewById(R.id.btnverify)
        backBtn = findViewById(R.id.iv_back)
        countdownText = findViewById(R.id.countdown_text)
        emailDisplay = findViewById(R.id.email_display)
        
        // Get email from SharedPreferences
        email = getSharedPreferences("OTPFlow", MODE_PRIVATE).getString("reset_email", "") ?: ""
        
        if (email.isEmpty()) {
            Log.e(TAG, "No email found in SharedPreferences")
            Toast.makeText(this, "Email not found. Please start over from the beginning.", Toast.LENGTH_LONG).show()
            // Navigate back to OTP request page
            val intent = Intent(this, otprequestpage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return
        }
        
        Log.d(TAG, "Email retrieved successfully: $email")
        
        // Update email text in UI
        emailDisplay.text = "We have sent a verification code to ${maskEmail(email)}"
    }

    private fun maskEmail(email: String): String {
        return if (email.contains("@")) {
            val parts = email.split("@")
            val username = parts[0]
            val domain = parts[1]
            if (username.length <= 2) {
                email
            } else {
                "${username[0]}***@$domain"
            }
        } else {
            email
        }
    }

    private fun setupClickListeners() {
        verifyBtn.setOnClickListener {
            verifyOtp()
        }

        backBtn.setOnClickListener {
            // Clear the email and go back to OTP request page
            val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
            editor.remove("reset_email")
            editor.apply()
            
            val intent = Intent(this, otprequestpage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun verifyOtp() {
        val otp = otp1.text.toString() + otp2.text.toString() + otp3.text.toString() +
                otp4.text.toString() + otp5.text.toString() + otp6.text.toString()

        if (otp.length != 6) {
            Toast.makeText(this, "Please enter the 6-digit OTP", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email not found. Please start over.", Toast.LENGTH_LONG).show()
            // Navigate back to OTP request page
            val intent = Intent(this, otprequestpage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        verifyBtn.isEnabled = false
        verifyBtn.text = "Verifying..."

        Log.d(TAG, "Sending OTP verification request for email: $email")

        val api = RetrofitClient.getClient().create(ApiService::class.java)
        val request = VerifyOtpRequest(email, otp)

        api.verifyOtp(request).enqueue(object : Callback<VerifyOtpResponse> {
            override fun onResponse(call: Call<VerifyOtpResponse>, response: Response<VerifyOtpResponse>) {
                Log.d(TAG, "OTP verification response received")
                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response body: ${response.body()}")

                // Reset button state
                verifyBtn.isEnabled = true
                verifyBtn.text = "Verify"

                when (response.code()) {
                    200, 201 -> {
                        if (response.body() != null) {
                            Toast.makeText(
                                this@otpverification,
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to reset password page
                            val intent = Intent(this@otpverification, resetpasswordpage::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@otpverification,
                                "Verification successful but no response data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    400 -> {
                        Toast.makeText(
                            this@otpverification,
                            "Invalid OTP. Please check and try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    401 -> {
                        Toast.makeText(
                            this@otpverification,
                            "OTP expired or invalid. Please request a new one.",
                            Toast.LENGTH_LONG
                        ).show()
                        // Clear email and go back to OTP request page
                        val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
                        editor.remove("reset_email")
                        editor.apply()
                        
                        val intent = Intent(this@otpverification, otprequestpage::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        Toast.makeText(
                            this@otpverification,
                            "Verification failed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                Log.e(TAG, "OTP verification network failure", t)
                
                // Reset button state
                verifyBtn.isEnabled = true
                verifyBtn.text = "Verify"
                
                Toast.makeText(
                    this@otpverification,
                    "Network error: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun setupOtpInputListeners() {
        otp1.addTextChangedListener(simpleWatcher { otp2.requestFocus() })
        otp2.addTextChangedListener(simpleWatcher { otp3.requestFocus() })
        otp3.addTextChangedListener(simpleWatcher { otp4.requestFocus() })
        otp4.addTextChangedListener(simpleWatcher { otp5.requestFocus() })
        otp5.addTextChangedListener(simpleWatcher { otp6.requestFocus() })
        otp1.requestFocus()
    }

    private fun simpleWatcher(onFilled: () -> Unit): android.text.TextWatcher {
        return object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s?.length == 1) onFilled()
            }
        }
    }

    private fun startCountdown() {
        countDownTimer = object : android.os.CountDownTimer(
            TimeUnit.MINUTES.toMillis(3), 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                countdownText.text = "Code expires in: %02d:%02d".format(minutes, seconds)
            }

            override fun onFinish() {
                countdownText.text = "Code expired"
                Toast.makeText(
                    this@otpverification,
                    "OTP has expired. Please request a new one.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Clear email and go back to OTP request page
                val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
                editor.remove("reset_email")
                editor.apply()
                
                val intent = Intent(this@otpverification, otprequestpage::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
    
    override fun onBackPressed() {
        // Clear the email and go back to OTP request page
        val editor = getSharedPreferences("OTPFlow", MODE_PRIVATE).edit()
        editor.remove("reset_email")
        editor.apply()
        
        val intent = Intent(this, otprequestpage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}