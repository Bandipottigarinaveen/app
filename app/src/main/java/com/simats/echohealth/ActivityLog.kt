package com.simats.echohealth

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class ActivityItem(
    val title: String,
    val description: String,
    val timestampMillis: Long,
    val type: String? = null,           // "symptoms" or "upload"
    val riskLevel: String? = null,      // High/Medium/Low
    val riskScore: Int? = null,         // integer score if available
    val riskPercent: Int? = null        // 0..100 percent if available
)

object ActivityLogStore {
    private const val PREFS_NAME = "RecentActivity"
    private const val KEY_LIST = "items"
    private const val MAX_ITEMS = 20

    fun addActivity(
        context: Context,
        title: String,
        description: String,
        type: String? = null,
        riskLevel: String? = null,
        riskScore: Int? = null,
        riskPercent: Int? = null
    ) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingJson = prefs.getString(KEY_LIST, "[]") ?: "[]"
            val arr = try { JSONArray(existingJson) } catch (_: Exception) { JSONArray() }

            // Prepend new item
            val obj = JSONObject()
            obj.put("title", title)
            obj.put("description", description)
            obj.put("ts", System.currentTimeMillis())
            type?.let { obj.put("type", it) }
            riskLevel?.let { obj.put("riskLevel", it) }
            riskScore?.let { obj.put("riskScore", it) }
            riskPercent?.let { obj.put("riskPercent", it) }

            val newArr = JSONArray()
            newArr.put(obj)
            var count = 1
            for (i in 0 until arr.length()) {
                if (count >= MAX_ITEMS) break
                newArr.put(arr.getJSONObject(i))
                count++
            }

            prefs.edit().putString(KEY_LIST, newArr.toString()).apply()
        } catch (e: Exception) {
            Log.e("ActivityLog", "Failed to add activity: ${e.message}")
        }
    }

    fun getActivities(context: Context, limit: Int = 10): List<ActivityItem> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingJson = prefs.getString(KEY_LIST, "[]") ?: "[]"
            val arr = try { JSONArray(existingJson) } catch (_: Exception) { JSONArray() }
            val result = ArrayList<ActivityItem>()
            val max = kotlin.math.min(limit, arr.length())
            for (i in 0 until max) {
                val o = arr.getJSONObject(i)
                result.add(
                    ActivityItem(
                        title = o.optString("title"),
                        description = o.optString("description"),
                        timestampMillis = o.optLong("ts"),
                        type = o.optString("type", null),
                        riskLevel = o.optString("riskLevel", null),
                        riskScore = if (o.has("riskScore")) o.optInt("riskScore") else null,
                        riskPercent = if (o.has("riskPercent")) o.optInt("riskPercent") else null
                    )
                )
            }
            result
        } catch (e: Exception) {
            Log.e("ActivityLog", "Failed to read activities: ${e.message}")
            emptyList()
        }
    }

    fun clearAllActivities(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d("ActivityLog", "Cleared all activities from SharedPreferences")
        } catch (e: Exception) {
            Log.e("ActivityLog", "Failed to clear activities: ${e.message}")
        }
    }
}


