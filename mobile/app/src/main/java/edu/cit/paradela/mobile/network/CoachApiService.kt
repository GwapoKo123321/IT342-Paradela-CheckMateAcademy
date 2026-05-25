package edu.cit.paradela.mobile.network

import edu.cit.paradela.mobile.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CoachApiService {

    // 1. Coach Schedule & Requests
    @GET("api/coach/schedule")
    suspend fun getCoachSchedule(): Response<List<LessonModel>>

    @POST("api/coach/lesson/{lessonId}/accept")
    suspend fun acceptLesson(@Path("lessonId") lessonId: String): Response<BaseResponse>

    @POST("api/coach/lesson/{lessonId}/reject")
    suspend fun rejectLesson(@Path("lessonId") lessonId: String): Response<BaseResponse>

    // 2. Lesson Reviews
    @GET("api/coach/reviews")
    suspend fun getCoachReviews(): Response<List<ReviewModel>>

    // 3. Profile Management
    @GET("api/coach/profile")
    suspend fun getCoachProfile(): Response<CoachProfile>

    @PUT("api/coach/profile")
    suspend fun updateCoachProfile(@Body update: CoachProfile): Response<BaseResponse>
}