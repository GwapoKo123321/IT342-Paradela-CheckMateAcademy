package edu.cit.paradela.mobile.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://it342-paradela-checkmateacademy.onrender.com/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthApiService by lazy {
        instance.create(AuthApiService::class.java)
    }

    val studentService: StudentApiService by lazy {
        instance.create(StudentApiService::class.java)
    }

    val coachService: CoachApiService by lazy {
        instance.create(CoachApiService::class.java)
    }

    val liveSessionService: LiveSessionApiService by lazy {
        instance.create(LiveSessionApiService::class.java)
    }
}