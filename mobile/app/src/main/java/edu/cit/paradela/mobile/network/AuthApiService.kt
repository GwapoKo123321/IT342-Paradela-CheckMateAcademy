package edu.cit.paradela.mobile.network

import edu.cit.paradela.mobile.models.AuthResponse
import edu.cit.paradela.mobile.models.LoginRequest
import edu.cit.paradela.mobile.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>
}