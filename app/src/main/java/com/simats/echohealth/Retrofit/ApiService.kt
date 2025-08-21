package com.simats.echohealth.Retrofit

import com.simats.echohealth.Responses.ChatRequest
import com.simats.echohealth.Responses.ChatResponse
import com.simats.echohealth.Responses.LoginRequest
import com.simats.echohealth.Responses.LoginResponse
import com.simats.echohealth.Responses.RequestOtpRequest
import com.simats.echohealth.Responses.RequestOtpResponse
import com.simats.echohealth.Responses.ResetRequest
import com.simats.echohealth.Responses.ResetResponse
import com.simats.echohealth.Responses.SignupRequest
import com.simats.echohealth.Responses.SignupResponse
import com.simats.echohealth.Responses.VerifyOtpRequest
import com.simats.echohealth.Responses.VerifyOtpResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

	@POST("api/register/")
	fun register(@Body body: SignupRequest): Call<SignupResponse>

	@POST("api/login/")
	fun login(@Body body: LoginRequest): Call<LoginResponse>

	@POST("api/request-otp/")
	fun requestOtp(@Body body: RequestOtpRequest): Call<RequestOtpResponse>

	@POST("api/verify-otp/")
	fun verifyOtp(@Body body: VerifyOtpRequest): Call<VerifyOtpResponse>

	@POST("api/reset-password/")
	fun resetPassword(@Body body: ResetRequest) : Call<ResetResponse>

	// Optional chat endpoint if backend supports it
	@POST("api/oral-cancer-chat/")
	suspend fun chat(
		@Header("Authorization") authorization: String? = null,
		@Body body: ChatRequest
	): Response<ChatResponse>
}


