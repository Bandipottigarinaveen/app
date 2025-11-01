package com.simats.echohealth.Responses

import com.google.gson.annotations.SerializedName

data class ProfileRequest(
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String
)
