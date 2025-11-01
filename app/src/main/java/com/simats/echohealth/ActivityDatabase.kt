package com.simats.echohealth

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

data class DbActivityItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val timestampMillis: Long,
    val type: String? = null,
    val riskLevel: String? = null,
    val riskScore: Int? = null,
    val riskPercent: Int? = null,
    val isLiked: Boolean = false
)

class ActivityDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "activity_history.db"
        const val DATABASE_VERSION = 2
        const val TABLE = "activities"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_DESCRIPTION = "description"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_TYPE = "type"
        const val COL_RISK_LEVEL = "risk_level"
        const val COL_RISK_SCORE = "risk_score"
        const val COL_RISK_PERCENT = "risk_percent"
        const val COL_IS_LIKED = "is_liked"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${TABLE} (" +
                "${COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${COL_TITLE} TEXT NOT NULL, " +
                "${COL_DESCRIPTION} TEXT NOT NULL, " +
                "${COL_TIMESTAMP} INTEGER NOT NULL, " +
                "${COL_TYPE} TEXT, " +
                "${COL_RISK_LEVEL} TEXT, " +
                "${COL_RISK_SCORE} INTEGER, " +
                "${COL_RISK_PERCENT} INTEGER, " +
                "${COL_IS_LIKED} INTEGER DEFAULT 0" +
            ")"
        )
        Log.d("ActivityDatabase", "Created table ${TABLE}")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add the is_liked column to existing table
            db.execSQL("ALTER TABLE ${TABLE} ADD COLUMN ${COL_IS_LIKED} INTEGER DEFAULT 0")
            Log.d("ActivityDatabase", "Added is_liked column to existing table")
        }
    }
}

object ActivityDatabase {
    private var helper: ActivityDbHelper? = null

    private fun db(context: Context): SQLiteDatabase {
        if (helper == null) helper = ActivityDbHelper(context.applicationContext)
        return helper!!.writableDatabase
    }

    fun add(context: Context, item: DbActivityItem) {
        try {
            Log.d("ActivityDatabase", "Adding activity: ${item.title} - ${item.description}")
            val values = ContentValues().apply {
                put(ActivityDbHelper.COL_TITLE, item.title)
                put(ActivityDbHelper.COL_DESCRIPTION, item.description)
                put(ActivityDbHelper.COL_TIMESTAMP, item.timestampMillis)
                item.type?.let { put(ActivityDbHelper.COL_TYPE, it) }
                item.riskLevel?.let { put(ActivityDbHelper.COL_RISK_LEVEL, it) }
                item.riskScore?.let { put(ActivityDbHelper.COL_RISK_SCORE, it) }
                item.riskPercent?.let { put(ActivityDbHelper.COL_RISK_PERCENT, it) }
                put(ActivityDbHelper.COL_IS_LIKED, if (item.isLiked) 1 else 0)
            }
            val id = db(context).insert(ActivityDbHelper.TABLE, null, values)
            Log.d("ActivityDatabase", "Inserted activity id=$id")
        } catch (e: Exception) {
            Log.e("ActivityDatabase", "Insert failed: ${e.message}")
            e.printStackTrace()
        }
    }

    fun list(context: Context, limit: Int = 50): List<DbActivityItem> {
        return try {
            Log.d("ActivityDatabase", "Querying database for activities")
            val cursor = db(context).query(
                ActivityDbHelper.TABLE,
                null,
                null,
                null,
                null,
                null,
                "${ActivityDbHelper.COL_TIMESTAMP} DESC",
                limit.toString()
            )
            val items = mutableListOf<DbActivityItem>()
            cursor.use { c ->
                Log.d("ActivityDatabase", "Cursor has ${c.count} rows")
                while (c.moveToNext()) {
                    val id = c.getLong(c.getColumnIndexOrThrow(ActivityDbHelper.COL_ID))
                    val title = c.getString(c.getColumnIndexOrThrow(ActivityDbHelper.COL_TITLE))
                    val description = c.getString(c.getColumnIndexOrThrow(ActivityDbHelper.COL_DESCRIPTION))
                    val ts = c.getLong(c.getColumnIndexOrThrow(ActivityDbHelper.COL_TIMESTAMP))
                    val type = c.getString(c.getColumnIndexOrThrow(ActivityDbHelper.COL_TYPE))
                    val riskLevel = c.getString(c.getColumnIndexOrThrow(ActivityDbHelper.COL_RISK_LEVEL))
                    val riskScoreIdx = c.getColumnIndexOrThrow(ActivityDbHelper.COL_RISK_SCORE)
                    val riskPercentIdx = c.getColumnIndexOrThrow(ActivityDbHelper.COL_RISK_PERCENT)
                    val isLikedIdx = c.getColumnIndexOrThrow(ActivityDbHelper.COL_IS_LIKED)
                    val riskScore = if (c.isNull(riskScoreIdx)) null else c.getInt(riskScoreIdx)
                    val riskPercent = if (c.isNull(riskPercentIdx)) null else c.getInt(riskPercentIdx)
                    val isLiked = c.getInt(isLikedIdx) == 1
                    items.add(DbActivityItem(id, title, description, ts, type, riskLevel, riskScore, riskPercent, isLiked))
                    Log.d("ActivityDatabase", "Retrieved item: $title - $description")
                }
            }
            Log.d("ActivityDatabase", "Returning ${items.size} items")
            items
        } catch (e: Exception) {
            Log.e("ActivityDatabase", "Query failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    fun clear(context: Context) {
        try {
            val deleted = db(context).delete(ActivityDbHelper.TABLE, null, null)
            Log.d("ActivityDatabase", "Cleared $deleted rows")
        } catch (e: Exception) {
            Log.e("ActivityDatabase", "Clear failed: ${e.message}")
        }
    }

    fun updateLikeStatus(context: Context, id: Long, isLiked: Boolean) {
        try {
            val values = ContentValues().apply {
                put(ActivityDbHelper.COL_IS_LIKED, if (isLiked) 1 else 0)
            }
            val updated = db(context).update(
                ActivityDbHelper.TABLE,
                values,
                "${ActivityDbHelper.COL_ID} = ?",
                arrayOf(id.toString())
            )
            Log.d("ActivityDatabase", "Updated like status for id=$id, isLiked=$isLiked, rows=$updated")
        } catch (e: Exception) {
            Log.e("ActivityDatabase", "Update like status failed: ${e.message}")
            e.printStackTrace()
        }
    }
}


