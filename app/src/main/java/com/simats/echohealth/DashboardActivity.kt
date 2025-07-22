package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Header
        val profileIcon = findViewById<ImageView>(R.id.ivProfile)
        profileIcon.setOnClickListener {
            // TODO: Navigate to profile/settings
        }

        // Action Buttons
        val btnEnterSymptoms = findViewById<LinearLayout>(R.id.btnEnterSymptoms)
        val btnUploadReports = findViewById<LinearLayout>(R.id.btnUploadReports)
        val btnViewRiskScore = findViewById<LinearLayout>(R.id.btnViewRiskScore)
        val btnHistory = findViewById<LinearLayout>(R.id.btnHistory)

        btnEnterSymptoms.setOnClickListener {
            // TODO: Navigate to Enter Symptoms screen
        }
        btnUploadReports.setOnClickListener {
            // TODO: Navigate to Upload Reports screen
        }
        btnViewRiskScore.setOnClickListener {
            // TODO: Navigate to View Risk Score screen
        }
        btnHistory.setOnClickListener {
            // TODO: Navigate to History screen
        }

        // Recent Activity (RecyclerView)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerRecentActivity)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val activities = listOf(
            RecentActivityItem("Today", "Symptoms Entry", "Updated headache symptoms"),
            RecentActivityItem("Yesterday", "Report Upload", "Uploaded blood test results"),
            RecentActivityItem("2 days ago", "Risk Assessment", "Completed monthly check")
        )
        recyclerView.adapter = RecentActivityAdapter(activities)
    }
}

// Data class for recent activity
data class RecentActivityItem(val day: String, val title: String, val subtitle: String)

// Adapter for RecyclerView
class RecentActivityAdapter(private val items: List<RecentActivityItem>) : RecyclerView.Adapter<RecentActivityViewHolder>() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecentActivityViewHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_recent_activity, parent, false)
        return RecentActivityViewHolder(view)
    }
    override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
        holder.bind(items[position])
    }
    override fun getItemCount() = items.size
}

class RecentActivityViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    private val dayView: TextView = itemView.findViewById(R.id.tvDay)
    private val titleView: TextView = itemView.findViewById(R.id.tvTitle)
    private val subtitleView: TextView = itemView.findViewById(R.id.tvSubtitle)
    fun bind(item: RecentActivityItem) {
        dayView.text = item.day
        titleView.text = item.title
        subtitleView.text = item.subtitle
    }
} 