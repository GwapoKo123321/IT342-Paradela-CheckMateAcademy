package edu.cit.paradela.mobile.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {


    // Point directly to your live production server!
    private const val BASE_URL = "https://it342-paradela-checkmateacademy.onrender.com/"

    // Create a customized OkHttpClient for reliable connections
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Build the Retrofit instance
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Initialize your API Services
    val authService: AuthApiService by lazy {
        instance.create(AuthApiService::class.java)
    }

    val studentService: StudentApiService by lazy {
        instance.create(StudentApiService::class.java)
    }

    val coachService: CoachApiService by lazy {
        instance.create(CoachApiService::class.java)
    }
}