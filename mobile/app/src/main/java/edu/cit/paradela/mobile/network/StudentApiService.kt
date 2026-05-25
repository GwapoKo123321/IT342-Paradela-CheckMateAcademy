package edu.cit.paradela.mobile.network

import edu.cit.paradela.mobile.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface StudentApiService {

    // 1. My Schedule
    @GET("api/student/schedule/upcoming")
    suspend fun getUpcomingLessons(): Response<List<LessonModel>>

    // 2. Lesson Reviews
    @GET("api/student/reviews/past")
    suspend fun getPastReviews(): Response<List<ReviewModel>>

    // 3. Book a Lesson
    @GET("api/coaches/available")
    suspend fun getAvailableCoaches(): Response<List<CoachModel>>

    @POST("api/student/book")
    suspend fun bookLesson(@Body request: BookingRequest): Response<BaseResponse>

    // 4. Account Profile
    @GET("api/student/profile")
    suspend fun getProfile(): Response<StudentProfile>

    @PUT("api/student/profile")
    suspend fun updateProfile(@Body update: StudentProfile): Response<BaseResponse>
}