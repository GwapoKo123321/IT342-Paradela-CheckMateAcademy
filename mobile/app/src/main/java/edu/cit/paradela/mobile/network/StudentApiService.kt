package edu.cit.paradela.mobile.network

import edu.cit.paradela.mobile.models.*
import retrofit2.Response
import retrofit2.http.*

interface StudentApiService {

    // 1. My Schedule — get all lessons for this student
    @GET("api/lessons/student/{studentId}")
    suspend fun getStudentLessons(@Path("studentId") studentId: String): Response<List<LessonModel>>

    // 2. Available coach slots by date (for booking)
    @GET("api/users/coaches/available-slots")
    suspend fun getAvailableSlots(
        @Query("date") date: String,        // format: YYYY-MM-DD
        @Query("studentId") studentId: String
    ): Response<List<CoachAvailableSlot>>

    // 3. Book a lesson
    @POST("api/lessons")
    suspend fun bookLesson(@Body request: BookingRequest): Response<LessonModel>

    // 4. Get user profile
    @GET("api/users/coaches/{userId}/profile")
    suspend fun getCoachProfile(@Path("userId") userId: String): Response<CoachModel>

    // 5. Update user profile
    @PUT("api/users/profile/update/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body update: UserProfile
    ): Response<UserProfile>
}