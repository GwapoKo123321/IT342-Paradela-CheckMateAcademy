package edu.cit.paradela.mobile.network

import edu.cit.paradela.mobile.models.*
import retrofit2.Response
import retrofit2.http.*

interface StudentApiService {

    @GET("api/lessons/student/{studentId}")
    suspend fun getStudentLessons(@Path("studentId") studentId: String): Response<List<LessonModel>>

    @GET("api/users/coaches/available-slots")
    suspend fun getAvailableSlots(
        @Query("date") date: String,
        @Query("studentId") studentId: String
    ): Response<List<CoachAvailableSlot>>

    @POST("api/lessons")
    suspend fun bookLesson(@Body request: BookingRequest): Response<LessonModel>

    @GET("api/users/coaches/{userId}/profile")
    suspend fun getCoachProfile(@Path("userId") userId: String): Response<CoachModel>

    @PUT("api/users/profile/update/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body update: UserProfile
    ): Response<UserProfile>
}
