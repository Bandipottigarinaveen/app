package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.LinearLayout
import android.os.Handler
import android.os.Looper
import android.net.Uri
import com.simats.echohealth.utils.ProfileManager
import java.io.File
import android.view.View
import android.view.ViewOutlineProvider
import android.graphics.Outline

class Dashboard : AppCompatActivity() {
    private val tips = listOf(
        "Stay hydrated! Aim for 8 glasses of water today",
        "Brush and floss daily to maintain good oral hygiene",
        "Avoid tobacco and limit alcohol for better oral health",
        "Schedule regular dental checkups every 6 months",
        "Include fruits and vegetables rich in antioxidants"
    )
    private var tipIndex = 0
    private val tipHandler = Handler(Looper.getMainLooper())
    private lateinit var healthTipText: TextView
    private val tipRunnable = object : Runnable {
        override fun run() {
            if (::healthTipText.isInitialized) {
                tipIndex = (tipIndex + 1) % tips.size
                healthTipText.text = tips[tipIndex]
            }
            tipHandler.postDelayed(this, 5000)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onResume() {
        super.onResume()
        // Refresh recent activity whenever returning to dashboard
        populateRecentActivity()
        // Refresh profile icon from local profile
        loadProfileIcon()
    }

    private fun populateRecentActivity() {
        try {
            val container = findViewById<LinearLayout>(R.id.recentActivityContainer)
            container?.let {
                it.removeAllViews()
                val items = ActivityLogStore.getActivities(this, limit = 10)
                if (items.isEmpty()) {
                    val empty = android.widget.TextView(this)
                    empty.text = "No recent activity yet"
                    empty.setTextColor(android.graphics.Color.parseColor("#666666"))
                    empty.textSize = 14f
                    it.addView(empty)
                    return
                }
                var lastHeader: String? = null
                for (item in items) {
                    val header = dayLabelFor(item.timestampMillis)
                    if (header != lastHeader) {
                        val headerView = android.widget.TextView(this)
                        headerView.text = header
                        headerView.setTextColor(android.graphics.Color.parseColor("#666666"))
                        headerView.textSize = 12f
                        headerView.setPadding(0, if (lastHeader == null) 0 else dp(8), 0, dp(4))
                        it.addView(headerView)
                        lastHeader = header
                    }
                    val row = android.widget.LinearLayout(this)
                    row.orientation = android.widget.LinearLayout.VERTICAL
                    row.setPadding(0, 0, 0, dp(12))

                    val titleView = android.widget.TextView(this)
                    titleView.text = item.title
                    titleView.setTextColor(android.graphics.Color.parseColor("#333333"))
                    titleView.textSize = 16f
                    titleView.setTypeface(titleView.typeface, android.graphics.Typeface.BOLD)

                    // Create a horizontal layout for description and risk level
                    val descRow = android.widget.LinearLayout(this)
                    descRow.orientation = android.widget.LinearLayout.HORIZONTAL
                    descRow.layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    val descView = android.widget.TextView(this)
                    descView.text = item.description
                    descView.setTextColor(android.graphics.Color.parseColor("#666666"))
                    descView.textSize = 14f
                    descView.layoutParams = android.widget.LinearLayout.LayoutParams(
                        0,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                    // Add risk level badge if available
                    if (!item.riskLevel.isNullOrBlank()) {
                        val riskBadge = android.widget.TextView(this)
                        val riskText = when (item.riskLevel.lowercase()) {
                            "high", "high risk" -> "HIGH RISK"
                            "medium", "medium risk", "moderate" -> "MEDIUM RISK"
                            "low", "low risk" -> "LOW RISK"
                            "very low", "very low risk" -> "VERY LOW RISK"
                            else -> item.riskLevel.uppercase()
                        }
                        
                        val riskColor = when (item.riskLevel.lowercase()) {
                            "high", "high risk" -> android.graphics.Color.parseColor("#D93025")
                            "medium", "medium risk", "moderate" -> android.graphics.Color.parseColor("#F9A825")
                            "low", "low risk" -> android.graphics.Color.parseColor("#0BAA5F")
                            "very low", "very low risk" -> android.graphics.Color.parseColor("#0BAA5F")
                            else -> android.graphics.Color.parseColor("#666666")
                        }
                        
                        riskBadge.text = riskText
                        riskBadge.setTextColor(riskColor)
                        riskBadge.textSize = 12f
                        riskBadge.setTypeface(riskBadge.typeface, android.graphics.Typeface.BOLD)
                        riskBadge.setPadding(dp(8), dp(2), dp(8), dp(2))
                        riskBadge.background = android.graphics.drawable.GradientDrawable().apply {
                            setColor(android.graphics.Color.parseColor("#F5F5F5"))
                            cornerRadius = dp(12).toFloat()
                        }
                        
                        descRow.addView(riskBadge)
                    }

                    descRow.addView(descView)
                    row.addView(titleView)
                    row.addView(descRow)
                    it.addView(row)
                }
            }
        } catch (e: Exception) {
            Log.e("Dashboard", "Failed to populate recent activity: ${e.message}")
        }
    }

    private fun dayLabelFor(timestampMillis: Long): String {
        try {
            val now = java.util.Calendar.getInstance()
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestampMillis }

            // Normalize to midnight for diff
            fun trunc(c: java.util.Calendar) {
                c.set(java.util.Calendar.HOUR_OF_DAY, 0)
                c.set(java.util.Calendar.MINUTE, 0)
                c.set(java.util.Calendar.SECOND, 0)
                c.set(java.util.Calendar.MILLISECOND, 0)
            }
            trunc(now)
            trunc(cal)

            val diffDays = ((now.timeInMillis - cal.timeInMillis) / (24L * 60L * 60L * 1000L)).toInt()
            return when (diffDays) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> "$diffDays days ago"
            }
        } catch (_: Exception) {
            return ""
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Dashboard", "Dashboard onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d("Dashboard", "Edge to edge enabled")
            
            setContentView(R.layout.activity_dashboard)
            Log.d("Dashboard", "Dashboard layout set successfully")
            
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            Log.d("Dashboard", "Window insets setup successful")
        } catch (e: Exception) {
            Log.e("Dashboard", "Error in onCreate setup: ${e.message}")
            Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
        }

        // Initialize dashboard card navigation
        Log.d("Dashboard", "Setting up dashboard navigation")
        
        // Setup profile navigation
        setupProfileNavigation()
        
        // Setup dashboard card navigation
        setupDashboardCardNavigation()
        
        // Setup bottom navigation
        setupBottomNavigation()
        

        
        // Verify UI elements
        verifyUIElements()

        // Start rotating daily health tips
        healthTipText = findViewById(R.id.healthTipText)
        if (healthTipText != null) {
            healthTipText.text = tips[tipIndex]
            tipHandler.postDelayed(tipRunnable, 5000)
        }
        
        // Initial recent activity load
        populateRecentActivity()
        // Initial profile icon load
        loadProfileIcon()

        Log.d("Dashboard", "Dashboard onCreate completed successfully")
    }
    
    private fun verifyUIElements() {
        try {
            Log.d("Dashboard", "Verifying UI elements...")
            
            val main = findViewById<androidx.core.widget.NestedScrollView>(R.id.main)
            Log.d("Dashboard", "Main scroll view: ${if (main != null) "FOUND" else "NOT FOUND"}")
            
            val welcomeText = findViewById<android.widget.TextView>(R.id.welcomeText)
            Log.d("Dashboard", "Welcome text: ${if (welcomeText != null) "FOUND" else "NOT FOUND"}")
            
            val userProfileIcon = findViewById<ImageView>(R.id.userProfileIcon)
            Log.d("Dashboard", "User profile icon: ${if (userProfileIcon != null) "FOUND" else "NOT FOUND"}")
            
            // Verify bottom navigation items
            val homeNavItem = findViewById<LinearLayout>(R.id.homeNavItem)
            Log.d("Dashboard", "Home nav item: ${if (homeNavItem != null) "FOUND" else "NOT FOUND"}")
            
            val settingsNavItem = findViewById<LinearLayout>(R.id.settingsNavItem)
            Log.d("Dashboard", "Settings nav item: ${if (settingsNavItem != null) "FOUND" else "NOT FOUND"}")
            
            Log.d("Dashboard", "UI elements verification completed")
        } catch (e: Exception) {
            Log.e("Dashboard", "Error verifying UI elements: ${e.message}")
        }
    }

    private fun loadProfileIcon() {
        try {
            val icon = findViewById<ImageView>(R.id.userProfileIcon)
            if (icon == null) return
            makeCircular(icon)
            val local = ProfileManager.getProfileData(this)
            val photoStr = local.photo
            if (photoStr.isNullOrBlank()) return
            try {
                if (photoStr.startsWith("content:") || photoStr.startsWith("file:")) {
                    icon.setImageURI(Uri.parse(photoStr))
                } else {
                    val file = File(photoStr)
                    if (file.exists()) {
                        icon.setImageURI(Uri.fromFile(file))
                    }
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            Log.e("Dashboard", "Failed to load profile icon: ${e.message}")
        }
    }

    private fun makeCircular(imageView: ImageView) {
        try {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true

            val applyOutline: (View) -> Unit = { view ->
                val w = view.width
                val h = view.height
                if (w > 0 && h > 0) {
                    val size = kotlin.math.min(w, h)
                    // Force square to avoid oval clipping
                    val lp = view.layoutParams
                    if (lp != null && (lp.width != size || lp.height != size)) {
                        lp.width = size
                        lp.height = size
                        view.layoutParams = lp
                    }
                    view.outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(v: View, outline: Outline) {
                            outline.setOval(0, 0, v.width, v.height)
                        }
                    }
                    view.clipToOutline = true
                    view.invalidateOutline()
                }
            }

            // Apply after initial layout and on future layout changes
            imageView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ -> applyOutline(v) }
            imageView.post { applyOutline(imageView) }
        } catch (_: Exception) {}
    }
    
    private fun setupProfileNavigation() {
        try {
            Log.d("Dashboard", "Setting up profile navigation")
            
            // Test if profile class can be instantiated
            try {
                Log.d("Dashboard", "Testing profile class instantiation")
                val testIntent = Intent(this, com.simats.echohealth.Profile::class.java)
                Log.d("Dashboard", "Profile class test successful: ${testIntent.component?.className}")
            } catch (e: Exception) {
                Log.e("Dashboard", "Profile class test failed: ${e.message}")
                e.printStackTrace()
            }
            
            val userProfileIcon = findViewById<ImageView>(R.id.userProfileIcon)
            Log.d("Dashboard", "Profile icon found: ${userProfileIcon != null}")
            
            if (userProfileIcon != null) {
                // Make the ImageView more clickable
                userProfileIcon.isClickable = true
                userProfileIcon.isFocusable = true
                
                userProfileIcon.setOnClickListener {
                    Log.d("Dashboard", "User Profile Icon clicked - navigating to profile")
                    try {
                        Log.d("Dashboard", "Creating intent for profile")
                        // Try using fully qualified class name
                        val intent = Intent(this, com.simats.echohealth.Profile::class.java)
                        Log.d("Dashboard", "Intent created successfully")
                        
                        Log.d("Dashboard", "Starting profile activity")
                        startActivity(intent)
                        Log.d("Dashboard", "Successfully navigated to profile")
                        
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to profile: ${e.message}")
                        Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
                
                // Add a test click listener to verify it's working
                userProfileIcon.setOnLongClickListener {
                    Log.d("Dashboard", "Profile icon long clicked - this confirms the view is clickable")
                    true
                }
                
                Log.d("Dashboard", "Profile navigation setup successful")
                Log.d("Dashboard", "Profile icon clickable: ${userProfileIcon.isClickable}")
                Log.d("Dashboard", "Profile icon focusable: ${userProfileIcon.isFocusable}")
                
            } else {
                Log.e("Dashboard", "User profile icon not found")
            }
        } catch (e: Exception) {
            Log.e("Dashboard", "Error setting up profile navigation: ${e.message}")
            Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
            e.printStackTrace()
        }
    }
    
    private fun setupDashboardCardNavigation() {
        try {
            Log.d("Dashboard", "Setting up dashboard card navigation")
            
            // Setup Upload Reports Card navigation
            val uploadReportsCard = findViewById<androidx.cardview.widget.CardView>(R.id.uploadReportsCard)
            Log.d("Dashboard", "Upload reports card found: ${uploadReportsCard != null}")
            
            if (uploadReportsCard != null) {
                uploadReportsCard.setOnClickListener {
                    Log.d("Dashboard", "Upload Reports Card clicked - navigating to uploadreport")
                    try {
                        Log.d("Dashboard", "Creating intent for uploadreport")
                        val intent = Intent(this, com.simats.echohealth.UploadReport::class.java)
                        Log.d("Dashboard", "Intent created successfully")
                        
                        Log.d("Dashboard", "Starting uploadreport activity")
                        startActivity(intent)
                        Log.d("Dashboard", "Successfully navigated to uploadreport")
                        
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to uploadreport: ${e.message}")
                        Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
                
                Log.d("Dashboard", "Upload reports card navigation setup successful")
            } else {
                Log.e("Dashboard", "Upload reports card not found")
            }
            
            // Setup View Risk Score Card navigation
            val viewRiskScoreCard = findViewById<androidx.cardview.widget.CardView>(R.id.viewRiskScoreCard)
            Log.d("Dashboard", "View Risk Score card found: ${viewRiskScoreCard != null}")
            
            if (viewRiskScoreCard != null) {
                // Populate risk score and level directly on dashboard
                try {
                    val tvRiskLevel = findViewById<android.widget.TextView>(R.id.tvRiskLevel)
                    val tvRiskScore = findViewById<android.widget.TextView>(R.id.tvRiskScore)
                    val prefs = getSharedPreferences("LastResult", MODE_PRIVATE)
                    val riskScore = prefs.getString("riskScore", null)
                    val riskLevel = prefs.getString("riskLevel", null)

                    tvRiskLevel?.text = "Risk Level: ${riskLevel ?: "--"}"
                    tvRiskScore?.text = "Score: ${riskScore ?: "--"}"
                } catch (e: Exception) {
                    Log.e("Dashboard", "Error populating risk score on dashboard: ${e.message}")
                }

                // Remove navigation on click
                viewRiskScoreCard.setOnClickListener { /* no-op */ }

                Log.d("Dashboard", "View Risk Score card now displays data without navigation")
            } else {
                Log.e("Dashboard", "View Risk Score card not found")
            }
            
            // Setup Enter Symptoms Card navigation
            val enterSymptomsCard = findViewById<androidx.cardview.widget.CardView>(R.id.enterSymptomsCard)
            Log.d("Dashboard", "Enter Symptoms card found: ${enterSymptomsCard != null}")
            
            if (enterSymptomsCard != null) {
                enterSymptomsCard.setOnClickListener {
                    Log.d("Dashboard", "Enter Symptoms Card clicked - navigating to SymptomChecker")
                    try {
                        Log.d("Dashboard", "Creating intent for SymptomChecker")
                        val intent = Intent(this, com.simats.echohealth.SymptomChecker::class.java)
                        Log.d("Dashboard", "Intent created successfully")
                        
                        Log.d("Dashboard", "Starting SymptomChecker activity")
                        startActivity(intent)
                        Log.d("Dashboard", "Successfully navigated to SymptomChecker")
                        
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to SymptomChecker: ${e.message}")
                        Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
                
                Log.d("Dashboard", "Enter Symptoms card navigation setup successful")
            } else {
                Log.e("Dashboard", "Enter Symptoms card not found")
            }
            
            // Setup History Card navigation
            val historyCard = findViewById<androidx.cardview.widget.CardView>(R.id.historyCard)
            Log.d("Dashboard", "History card found: ${historyCard != null}")
            
            if (historyCard != null) {
                historyCard.setOnClickListener {
                    Log.d("Dashboard", "History Card clicked - navigating to HealthReport")
                    try {
                        Log.d("Dashboard", "Creating intent for HealthReport")
                        val intent = Intent(this, com.simats.echohealth.HealthReport::class.java)
                        Log.d("Dashboard", "Intent created successfully")
                        
                        Log.d("Dashboard", "Starting HealthReport activity")
                        startActivity(intent)
                        Log.d("Dashboard", "Successfully navigated to HealthReport")
                        
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to HealthReport: ${e.message}")
                        Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
                
                Log.d("Dashboard", "History card navigation setup successful")
            } else {
                Log.e("Dashboard", "History card not found")
            }
            
            // Setup other dashboard cards if needed
            // TODO: All main dashboard cards now have navigation implemented
            
        } catch (e: Exception) {
            Log.e("Dashboard", "Error setting up dashboard card navigation: ${e.message}")
            Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
            e.printStackTrace()
        }
    }
    
    private fun setupBottomNavigation() {
        try {
            Log.d("Dashboard", "Setting up bottom navigation")
            
            // Get all navigation items
            val homeNavItem = findViewById<LinearLayout>(R.id.homeNavItem)
            val learnNavItem = findViewById<LinearLayout>(R.id.learnNavItem)
            val chatNavItem = findViewById<LinearLayout>(R.id.chatNavItem)
            val resultNavItem = findViewById<LinearLayout>(R.id.resultNavItem)
            val settingsNavItem = findViewById<LinearLayout>(R.id.settingsNavItem)
            
            // Setup Home navigation (stays on current screen)
            if (homeNavItem != null) {
                homeNavItem.setOnClickListener {
                    Log.d("Dashboard", "Home button clicked - already on home screen")
                    setActiveNavigationItem(homeNavItem)
                }
                // Set Home as initially active
                setActiveNavigationItem(homeNavItem)
            }
            
            // Setup Learn navigation
            if (learnNavItem != null) {
                learnNavItem.setOnClickListener {
                    Log.d("Dashboard", "Learn button clicked - navigating to LearnActivity")
                    setActiveNavigationItem(learnNavItem)
                    
                    try {
                        Log.d("Dashboard", "Creating intent for LearnActivity")
                        val intent = Intent(this, com.simats.echohealth.LearnActivity::class.java)
                        Log.d("Dashboard", "Intent created successfully")
                        
                        Log.d("Dashboard", "Starting LearnActivity")
                        startActivity(intent)
                        Log.d("Dashboard", "Successfully navigated to LearnActivity")
                        
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to LearnActivity: ${e.message}")
                        Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
            }
            
            // Setup Chat navigation
            if (chatNavItem != null) {
                chatNavItem.setOnClickListener {
                    Log.d("Dashboard", "Chat button clicked - navigating to livechat")
                    setActiveNavigationItem(chatNavItem)
                    
                    try {
                        Log.d("Dashboard", "Creating intent for livechat")
                        val intent = Intent(this, com.simats.echohealth.LiveChat::class.java)
                        Log.d("Dashboard", "Intent created successfully")
                        
                        Log.d("Dashboard", "Starting livechat activity")
                        startActivity(intent)
                        Log.d("Dashboard", "Successfully navigated to livechat")
                        
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to livechat: ${e.message}")
                        Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
            }
            
            // Setup Result navigation
            if (resultNavItem != null) {
                resultNavItem.setOnClickListener {
                    Log.d("Dashboard", "Result button clicked - navigating to Results with last data")
                    setActiveNavigationItem(resultNavItem)
                    try {
                        val prefs = getSharedPreferences("LastResult", MODE_PRIVATE)
                        val riskScore = prefs.getString("riskScore", null)
                        val riskLevel = prefs.getString("riskLevel", null)
                        val probability = prefs.getFloat("probability", -1f)
                        val assessmentType = prefs.getString("assessmentType", "Assessment Results")
                        val isApiResult = prefs.getBoolean("isApiResult", false)
                        val recs = prefs.getString("recommendations", "")
                        val warns = prefs.getString("warningSigns", "")
                        val steps = prefs.getString("nextSteps", "")

                        val intent = Intent(this, com.simats.echohealth.Results::class.java)
                        assessmentType?.let { intent.putExtra("assessmentType", it) }
                        intent.putExtra("isApiResult", isApiResult)
                        riskScore?.let { intent.putExtra("riskScore", it) }
                        riskLevel?.let { intent.putExtra("riskLevel", it) }
                        if (probability >= 0f) intent.putExtra("probability", probability.toDouble())
                        if (!recs.isNullOrEmpty()) intent.putExtra("recommendations", recs.split("||").toTypedArray())
                        if (!warns.isNullOrEmpty()) intent.putExtra("warningSigns", warns.split("||").toTypedArray())
                        if (!steps.isNullOrEmpty()) intent.putExtra("nextSteps", steps.split("||").toTypedArray())
                        intent.putExtra("useProvidedScore", true)

                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to Results with last data: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }

            // Populate Recent Activity dynamically (also called in onResume)
            populateRecentActivity()
            
            // Setup Settings navigation
            if (settingsNavItem != null) {
                // Make sure the item is clickable
                settingsNavItem.isClickable = true
                settingsNavItem.isFocusable = true
                
                settingsNavItem.setOnClickListener {
                    Log.d("Dashboard", "Settings button clicked - navigating to settings")
                    setActiveNavigationItem(settingsNavItem)
                    
                    try {
                        Log.d("Dashboard", "Creating intent for settings")
                        val intent = Intent(this, com.simats.echohealth.SettingPage::class.java)
                        Log.d("Dashboard", "Intent created successfully")
                        
                        Log.d("Dashboard", "Starting settings activity")
                        startActivity(intent)
                        Log.d("Dashboard", "Successfully navigated to settings")
                        
                    } catch (e: Exception) {
                        Log.e("Dashboard", "Error navigating to settings: ${e.message}")
                        Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
                        e.printStackTrace()
                    }
                }
                
                // Add a test click listener to verify it's working
                settingsNavItem.setOnLongClickListener {
                    Log.d("Dashboard", "Settings nav item long clicked - this confirms the view is clickable")
                    true
                }
                
                Log.d("Dashboard", "Settings navigation setup successful")
                Log.d("Dashboard", "Settings nav item clickable: ${settingsNavItem.isClickable}")
                Log.d("Dashboard", "Settings nav item focusable: ${settingsNavItem.isFocusable}")
                
            } else {
                Log.e("Dashboard", "Settings nav item not found")
            }
            

            
        } catch (e: Exception) {
            Log.e("Dashboard", "Error setting up bottom navigation: ${e.message}")
            Log.e("Dashboard", "Stack trace: ${e.stackTraceToString()}")
            e.printStackTrace()
        }
    }
    
    private fun setActiveNavigationItem(activeItem: LinearLayout) {
        try {
            Log.d("Dashboard", "Setting active navigation item")
            
            // Get all navigation items
            val homeNavItem = findViewById<LinearLayout>(R.id.homeNavItem)
            val learnNavItem = findViewById<LinearLayout>(R.id.learnNavItem)
            val chatNavItem = findViewById<LinearLayout>(R.id.chatNavItem)
            val resultNavItem = findViewById<LinearLayout>(R.id.resultNavItem)
            val settingsNavItem = findViewById<LinearLayout>(R.id.settingsNavItem)
            
            // Reset all items to inactive state (gray)
            homeNavItem?.let { setNavigationItemInactive(it) }
            learnNavItem?.let { setNavigationItemInactive(it) }
            chatNavItem?.let { setNavigationItemInactive(it) }
            resultNavItem?.let { setNavigationItemInactive(it) }
            settingsNavItem?.let { setNavigationItemInactive(it) }
            
            // Set the clicked item to active state (blue)
            setNavigationItemActive(activeItem)
            
            Log.d("Dashboard", "Active navigation item set successfully")
            
        } catch (e: Exception) {
            Log.e("Dashboard", "Error setting active navigation item: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setNavigationItemActive(item: LinearLayout) {
        try {
            // Find the ImageView and TextView within the item
            val imageView = item.getChildAt(0) as? ImageView
            val textView = item.getChildAt(1) as? TextView
            
            // Set active colors (blue)
            imageView?.setColorFilter(resources.getColor(android.R.color.holo_blue_light, theme))
            textView?.setTextColor(resources.getColor(android.R.color.holo_blue_light, theme))
            
            Log.d("Dashboard", "Navigation item set to active (blue)")
            
        } catch (e: Exception) {
            Log.e("Dashboard", "Error setting navigation item active: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setNavigationItemInactive(item: LinearLayout) {
        try {
            // Find the ImageView and TextView within the item
            val imageView = item.getChildAt(0) as? ImageView
            val textView = item.getChildAt(1) as? TextView
            
            // Set inactive colors (gray)
            imageView?.clearColorFilter()
            imageView?.setColorFilter(resources.getColor(android.R.color.darker_gray, theme))
            textView?.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            
            Log.d("Dashboard", "Navigation item set to inactive (gray)")
            
        } catch (e: Exception) {
            Log.e("Dashboard", "Error setting navigation item inactive: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Removed duplicate onResume; onResume at top handles refreshing
    
    override fun onPause() {
        super.onPause()
        Log.d("Dashboard", "Dashboard onPause called")
        tipHandler.removeCallbacks(tipRunnable)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("Dashboard", "Dashboard onDestroy called")
        tipHandler.removeCallbacks(tipRunnable)
    }
}