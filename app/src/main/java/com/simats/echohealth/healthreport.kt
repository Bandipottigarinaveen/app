package com.simats.echohealth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class HealthReport : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_healthreport)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Add back button functionality
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton?.setOnClickListener {
            finish() // Go back to previous screen
        }

        // Populate with recent activity log entries
        try {
            val root = findViewById<LinearLayout>(R.id.reports_root)
            if (root != null) {
                root.removeAllViews()
                val items = ActivityDatabase.list(this, limit = 20)
                android.util.Log.d("HealthReport", "Retrieved ${items.size} items from database")
                for (item in items) {
                    android.util.Log.d("HealthReport", "Item: ${item.title} - ${item.description} - ${item.type}")
                }
                var lastHeader: String? = null
                for (item in items) {
                    val header = dayLabelFor(item.timestampMillis)
                    if (header != lastHeader) {
                        val headerView = TextView(this)
                        headerView.text = header
                        headerView.setTextColor(android.graphics.Color.parseColor("#666666"))
                        headerView.textSize = 12f
                        headerView.setPadding(0, if (lastHeader == null) 0 else dp(8), 0, dp(4))
                        root.addView(headerView)
                        lastHeader = header
                    }

                    val card = layoutInflater.inflate(R.layout.partial_history_item, root, false) as LinearLayout
                    // Map simplified activity fields into richer UI
                    val dateView = card.findViewById<TextView>(R.id.history_date)
                    val timeView = card.findViewById<TextView>(R.id.history_time)
                    val titleView = card.findViewById<TextView>(R.id.history_title)
                    val descView = card.findViewById<TextView>(R.id.history_desc)
                    val riskView = card.findViewById<TextView>(R.id.history_risk)
                    val riskIcon = card.findViewById<ImageView>(R.id.history_risk_icon)
                    val noteView = card.findViewById<TextView>(R.id.history_note)
                    val likeButton = card.findViewById<ImageView>(R.id.history_like_button)

                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = item.timestampMillis }
                    val dateFmt = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
                    val timeFmt = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                    dateView.text = dateFmt.format(cal.time)
                    timeView.text = timeFmt.format(cal.time)

                    // Use structured fields when available
                    val level = item.riskLevel
                    val levelLabel = when (level?.lowercase()) {
                        "high", "high risk" -> {
                            riskView.setTextColor(android.graphics.Color.parseColor("#D93025"))
                            riskIcon.setImageResource(R.drawable.warning)
                            "High Risk"
                        }
                        "medium", "medium risk", "moderate" -> {
                            riskView.setTextColor(android.graphics.Color.parseColor("#F9A825"))
                            riskIcon.setImageResource(R.drawable.ic_clock)
                            "Medium Risk"
                        }
                        "low", "low risk" -> {
                            riskView.setTextColor(android.graphics.Color.parseColor("#0BAA5F"))
                            riskIcon.setImageResource(R.drawable.ic_check_green)
                            "Low Risk"
                        }
                        else -> {
                            riskIcon.setImageResource(R.drawable.ic_check_green)
                            item.title
                        }
                    }
                    titleView.text = item.title
                    descView.text = item.description
                    riskView.text = levelLabel

                    val note = when {
                        (item.type == "upload" && item.riskPercent != null) -> "Risk ${level ?: ""}, ${item.riskPercent}%"
                        (item.type == "symptoms" && item.riskScore != null) -> "Risk ${level ?: ""}, Score ${item.riskScore}"
                        item.riskPercent != null -> "Risk ${level ?: ""}, ${item.riskPercent}%"
                        item.riskScore != null -> "Risk ${level ?: ""}, Score ${item.riskScore}"
                        else -> item.description
                    }
                    // Show recommendation only for risk assessments (symptoms or upload); hide otherwise
                    if (item.type == "symptoms" || item.type == "upload") {
                        noteView.visibility = android.view.View.VISIBLE
                        noteView.text = note
                    } else {
                        noteView.visibility = android.view.View.GONE
                    }

                    // Set up like button functionality
                    updateLikeButtonAppearance(likeButton, item.isLiked)
                    likeButton.setOnClickListener {
                        val newLikeStatus = !item.isLiked
                        ActivityDatabase.updateLikeStatus(this, item.id, newLikeStatus)
                        updateLikeButtonAppearance(likeButton, newLikeStatus)
                        // Update the item's like status for potential future use
                        val updatedItem = item.copy(isLiked = newLikeStatus)
                    }
                    
                    root.addView(card)
                }
                if (items.isEmpty()) {
                    val empty = TextView(this)
                    empty.text = "No history yet"
                    empty.setTextColor(android.graphics.Color.parseColor("#666666"))
                    empty.textSize = 14f
                    root.addView(empty)
                }
            }
        } catch (_: Exception) {}
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun dayLabelFor(timestampMillis: Long): String {
        val now = java.util.Calendar.getInstance()
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestampMillis }
        fun trunc(c: java.util.Calendar) {
            c.set(java.util.Calendar.HOUR_OF_DAY, 0)
            c.set(java.util.Calendar.MINUTE, 0)
            c.set(java.util.Calendar.SECOND, 0)
            c.set(java.util.Calendar.MILLISECOND, 0)
        }
        trunc(now); trunc(cal)
        val diffDays = ((now.timeInMillis - cal.timeInMillis) / (24L * 60L * 60L * 1000L)).toInt()
        return when (diffDays) { 0 -> "Today"; 1 -> "Yesterday"; else -> "$diffDays days ago" }
    }

    private fun updateLikeButtonAppearance(likeButton: ImageView, isLiked: Boolean) {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.ic_heart_filled)
            likeButton.alpha = 1.0f
        } else {
            likeButton.setImageResource(R.drawable.ic_heart_outline)
            likeButton.alpha = 0.6f
        }
    }
}