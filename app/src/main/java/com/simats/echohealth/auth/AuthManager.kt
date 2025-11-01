package com.simats.echohealth.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.concurrent.TimeUnit

/**
 * Secure Authentication Manager
 * Handles user session management with encrypted storage
 */
object AuthManager {
    
    private const val PREFS_NAME = "secure_auth_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_LOGIN_TIME = "login_time"
    private const val KEY_TOKEN_EXPIRY = "token_expiry"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    // Token expiry time (24 hours)
    private const val TOKEN_EXPIRY_HOURS = 24L
    
    private var encryptedPrefs: SharedPreferences? = null
    private var masterKey: MasterKey? = null
    
    /**
     * Initialize the AuthManager with encrypted preferences
     */
    fun initialize(context: Context) {
        try {
            Log.d("AuthManager", "🔐 Initializing secure authentication manager")
            
            // Create master key for encryption
            masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            // Create encrypted shared preferences
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey!!,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            Log.d("AuthManager", "✅ Secure authentication manager initialized")
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Failed to initialize secure auth manager: ${e.message}")
            // Fallback to regular SharedPreferences if encryption fails
            encryptedPrefs = context.getSharedPreferences("fallback_auth_prefs", Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Store user session after successful login
     */
    fun storeUserSession(
        context: Context,
        token: String,
        userId: String? = null,
        email: String? = null
    ): Boolean {
        return try {
            Log.d("AuthManager", "💾 Storing user session")
            
            val currentTime = System.currentTimeMillis()
            val expiryTime = currentTime + TimeUnit.HOURS.toMillis(TOKEN_EXPIRY_HOURS)
            
            encryptedPrefs?.edit()?.apply {
                putString(KEY_AUTH_TOKEN, token)
                putString(KEY_USER_ID, userId)
                putString(KEY_USER_EMAIL, email)
                putLong(KEY_LOGIN_TIME, currentTime)
                putLong(KEY_TOKEN_EXPIRY, expiryTime)
                putBoolean(KEY_IS_LOGGED_IN, true)
                apply()
            }
            
            Log.d("AuthManager", "✅ User session stored successfully")
            Log.d("AuthManager", "📧 Email: $email")
            Log.d("AuthManager", "🆔 User ID: $userId")
            Log.d("AuthManager", "⏰ Token expires: ${java.util.Date(expiryTime)}")
            
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Failed to store user session: ${e.message}")
            false
        }
    }
    
    /**
     * Check if user is currently logged in with valid session
     */
    fun isUserLoggedIn(context: Context): Boolean {
        return try {
            if (encryptedPrefs == null) {
                initialize(context)
            }
            
            val isLoggedIn = encryptedPrefs?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
            val token = getAuthToken(context)
            val expiryTime = encryptedPrefs?.getLong(KEY_TOKEN_EXPIRY, 0) ?: 0
            
            val isValid = isLoggedIn && 
                         !token.isNullOrBlank() && 
                         System.currentTimeMillis() < expiryTime
            
            Log.d("AuthManager", "🔍 Session check: loggedIn=$isLoggedIn, hasToken=${!token.isNullOrBlank()}, notExpired=${System.currentTimeMillis() < expiryTime}")
            Log.d("AuthManager", "✅ Valid session: $isValid")
            
            isValid
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Error checking login status: ${e.message}")
            false
        }
    }
    
    /**
     * Get the current authentication token
     */
    fun getAuthToken(context: Context): String? {
        return try {
            if (encryptedPrefs == null) {
                initialize(context)
            }
            
            val token = encryptedPrefs?.getString(KEY_AUTH_TOKEN, null)
            Log.d("AuthManager", "🔑 Retrieved auth token: ${if (token != null) "Present" else "Not found"}")
            token
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Error getting auth token: ${e.message}")
            null
        }
    }
    
    /**
     * Get user information
     */
    fun getUserInfo(context: Context): UserInfo? {
        return try {
            if (encryptedPrefs == null) {
                initialize(context)
            }
            
            val userId = encryptedPrefs?.getString(KEY_USER_ID, null)
            val email = encryptedPrefs?.getString(KEY_USER_EMAIL, null)
            val loginTime = encryptedPrefs?.getLong(KEY_LOGIN_TIME, 0) ?: 0
            
            if (userId != null || email != null) {
                UserInfo(
                    userId = userId,
                    email = email,
                    loginTime = loginTime
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Error getting user info: ${e.message}")
            null
        }
    }
    
    /**
     * Clear user session (logout)
     */
    fun clearUserSession(context: Context): Boolean {
        return try {
            Log.d("AuthManager", "🚪 Clearing user session")
            
            encryptedPrefs?.edit()?.apply {
                remove(KEY_AUTH_TOKEN)
                remove(KEY_USER_ID)
                remove(KEY_USER_EMAIL)
                remove(KEY_LOGIN_TIME)
                remove(KEY_TOKEN_EXPIRY)
                putBoolean(KEY_IS_LOGGED_IN, false)
                apply()
            }
            
            Log.d("AuthManager", "✅ User session cleared successfully")
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Failed to clear user session: ${e.message}")
            false
        }
    }
    
    /**
     * Refresh token expiry time
     */
    fun refreshTokenExpiry(context: Context): Boolean {
        return try {
            if (encryptedPrefs == null) {
                initialize(context)
            }
            
            val currentTime = System.currentTimeMillis()
            val newExpiryTime = currentTime + TimeUnit.HOURS.toMillis(TOKEN_EXPIRY_HOURS)
            
            encryptedPrefs?.edit()?.apply {
                putLong(KEY_TOKEN_EXPIRY, newExpiryTime)
                apply()
            }
            
            Log.d("AuthManager", "🔄 Token expiry refreshed: ${java.util.Date(newExpiryTime)}")
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Failed to refresh token expiry: ${e.message}")
            false
        }
    }
    
    /**
     * Get session expiry time
     */
    fun getSessionExpiryTime(context: Context): Long {
        return try {
            if (encryptedPrefs == null) {
                initialize(context)
            }
            
            encryptedPrefs?.getLong(KEY_TOKEN_EXPIRY, 0) ?: 0
        } catch (e: Exception) {
            Log.e("AuthManager", "❌ Error getting session expiry: ${e.message}")
            0
        }
    }
    
    /**
     * Check if session is about to expire (within 1 hour)
     */
    fun isSessionExpiringSoon(context: Context): Boolean {
        val expiryTime = getSessionExpiryTime(context)
        val oneHourFromNow = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
        return expiryTime > 0 && expiryTime < oneHourFromNow
    }
}

/**
 * Data class for user information
 */
data class UserInfo(
    val userId: String?,
    val email: String?,
    val loginTime: Long
)
