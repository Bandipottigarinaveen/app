package com.simats.echohealth.Responses

import com.google.gson.annotations.SerializedName

data class LoginRequest(
	val email: String,
	val password: String
)

data class LoginResponse(
	val message: String? = null,
	@SerializedName(value = "token", alternate = ["access", "auth_token", "key", "jwt", "access_token"])
	val token: String? = null
)


