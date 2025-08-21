package com.simats.echohealth.Retrofit

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NetworkUtils {
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    
    fun <T> executeCall(call: Call<T>, callback: Callback<T>) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                Log.d("NetworkUtils", "Response Code: ${response.code()}")
                Log.d("NetworkUtils", "Response Body: ${response.body()}")
                callback.onResponse(call, response)
            }
            
            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.e("NetworkUtils", "Network Error: ${t.message}", t)
                callback.onFailure(call, t)
            }
        })
    }
    
    fun testServerConnectivity(context: Context, callback: (Boolean, String) -> Unit) {
        if (!isNetworkAvailable(context)) {
            callback(false, "No network connection available")
            return
        }
        // No specific endpoint call now; simply report network availability
        callback(true, "Network available")
    }
}
