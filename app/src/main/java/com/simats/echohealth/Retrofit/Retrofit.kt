package com.simats.echohealth.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.simats.echohealth.BuildConfig

object RetrofitClient {

    const val BASE_URL = BuildConfig.BASE_URL

    private val _retrofitInstance: Retrofit by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)  // Increased from 20 to 60 seconds
            .readTimeout(60, TimeUnit.SECONDS)     // Increased from 30 to 60 seconds
            .writeTimeout(60, TimeUnit.SECONDS)    // Increased from 30 to 60 seconds
            .retryOnConnectionFailure(true)        // Enable retry on connection failure
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val retrofitInstance: Retrofit
        get() = _retrofitInstance

    fun getClient(): Retrofit {
        return _retrofitInstance
    }
}