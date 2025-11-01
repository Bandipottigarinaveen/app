package com.simats.echohealth.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing profile data locally
 */
object ProfileManager {
    private const val PREFS_NAME = "ProfilePrefs"
    
    // Keys for SharedPreferences
    private const val KEY_PHOTO = "photo"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_DATE_OF_BIRTH = "dob"
    private const val KEY_PHONE_NUMBER = "phone"
    
    /**
     * Save profile data to local storage
     */
    fun saveProfileData(
        context: Context,
        photo: String? = null,
        fullName: String? = null,
        username: String? = null,
        email: String? = null,
        dateOfBirth: String? = null,
        phoneNumber: String? = null
    ) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            photo?.let { putString(KEY_PHOTO, it) }
            fullName?.let { putString(KEY_FULL_NAME, it) }
            username?.let { putString(KEY_USERNAME, it) }
            email?.let { putString(KEY_EMAIL, it) }
            dateOfBirth?.let { putString(KEY_DATE_OF_BIRTH, it) }
            phoneNumber?.let { putString(KEY_PHONE_NUMBER, it) }
            apply()
        }
    }
    
    /**
     * Get profile data from local storage
     */
    fun getProfileData(context: Context): ProfileData {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ProfileData(
            photo = sharedPref.getString(KEY_PHOTO, null),
            fullName = sharedPref.getString(KEY_FULL_NAME, "") ?: "",
            username = sharedPref.getString(KEY_USERNAME, "") ?: "",
            email = sharedPref.getString(KEY_EMAIL, "") ?: "",
            dateOfBirth = sharedPref.getString(KEY_DATE_OF_BIRTH, "") ?: "",
            phoneNumber = sharedPref.getString(KEY_PHONE_NUMBER, "") ?: ""
        )
    }
    
    /**
     * Clear all profile data
     */
    fun clearProfileData(context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
    
    /**
     * Check if profile data exists
     */
    fun hasProfileData(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_EMAIL, null) != null
    }
}

/**
 * Data class for profile information
 */
data class ProfileData(
    val photo: String?,
    val fullName: String,
    val username: String,
    val email: String,
    val dateOfBirth: String,
    val phoneNumber: String
)
