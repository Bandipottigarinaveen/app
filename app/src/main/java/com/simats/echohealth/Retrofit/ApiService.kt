package com.simats.echohealth.Retrofit

import com.simats.echohealth.Responses.ChatRequest
import com.simats.echohealth.Responses.ChatResponse
import com.simats.echohealth.Responses.LoginRequest
import com.simats.echohealth.Responses.LoginResponse
import com.simats.echohealth.Responses.OralCancerDetectRequest
import com.simats.echohealth.Responses.OralCancerDetectResponse
import com.simats.echohealth.Responses.ProfileRequest
import com.simats.echohealth.Responses.ProfileResponse
import com.simats.echohealth.Responses.RequestOtpRequest
import com.simats.echohealth.Responses.RequestOtpResponse
import com.simats.echohealth.Responses.ResetRequest
import com.simats.echohealth.Responses.ResetResponse
import com.simats.echohealth.Responses.SignupRequest
import com.simats.echohealth.Responses.SignupResponse
import com.simats.echohealth.Responses.SymptomCheckRequest
import com.simats.echohealth.Responses.SymptomCheckResponse
import com.simats.echohealth.Responses.VerifyOtpRequest
import com.simats.echohealth.Responses.VerifyOtpResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

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

	@POST("api/reset-password/")
	fun resetPassword(
		@Header("Authorization") authorization: String,
		@Body body: ResetRequest
	) : Call<ResetResponse>

	// Symptom check endpoint
	@POST("api/predict/")
	fun symptomCheck(@Body body: SymptomCheckRequest): Call<SymptomCheckResponse>

	@POST("api/predict/")
	fun symptomCheck(
		@Header("Authorization") authorization: String?,
		@Body body: SymptomCheckRequest
	): Call<SymptomCheckResponse>

	// Optional chat endpoint if backend supports it
	@POST("api/oral-cancer-chat/")
	suspend fun chat(
		@Header("Authorization") authorization: String? = null,
		@Body body: ChatRequest
	): Response<ChatResponse>

	// Oral cancer detection endpoint
	@POST("api/oral-cancer-detect/")
	fun detectOralCancer(@Body body: OralCancerDetectRequest): Call<OralCancerDetectResponse>

	@POST("api/oral-cancer-detect/")
	fun detectOralCancer(
		@Header("Authorization") authorization: String?,
		@Body body: OralCancerDetectRequest
	): Call<OralCancerDetectResponse>

	// MultipartBody upload for oral cancer detection
    @Multipart
    @POST("api/oral-cancer-detect/")
    fun detectOralCancerMultipart(
        @Header("Authorization") authorization: String?,
        @Part image: MultipartBody.Part
    ): Call<OralCancerDetectResponse>

    

	// Profile endpoints
	@GET("api/profile/")
	fun getProfile(@Header("Authorization") authorization: String): Call<ProfileResponse>

	@PATCH("api/profile/")
	fun updateProfile(
		@Header("Authorization") authorization: String,
		@Body body: ProfileRequest
	): Call<ProfileResponse>

	// Profile photo upload
	@Multipart
	@POST("api/profile/photo/")
	fun uploadProfilePhoto(
		@Header("Authorization") authorization: String,
		@Part photo: MultipartBody.Part
	): Call<ProfileResponse>
}


