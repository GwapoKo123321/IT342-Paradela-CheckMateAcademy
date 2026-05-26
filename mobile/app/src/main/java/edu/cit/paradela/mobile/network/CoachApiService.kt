package edu.cit.paradela.mobile.network

import edu.cit.paradela.mobile.models.*
import retrofit2.Response
import retrofit2.http.*

interface CoachApiService {

    // 1. Coach Schedule — get all lessons for this coach
    @GET("api/lessons/coach/{coachId}")
    suspend fun getCoachLessons(@Path("coachId") coachId: String): Response<List<LessonModel>>

    // 2. Accept or Reject a lesson (pass status = "ACCEPTED" or "REJECTED")
    @PUT("api/lessons/{lessonId}/status")
    suspend fun updateLessonStatus(
        @Path("lessonId") lessonId: String,
        @Query("status") status: String
    ): Response<LessonModel>

    // 3. Get coach profile
    @GET("api/users/coaches/{coachId}/profile")
    suspend fun getCoachProfile(@Path("coachId") coachId: String): Response<CoachModel>

    // 4. Update coach profile
    @PUT("api/users/coaches/{coachId}/profile")
    suspend fun updateCoachProfile(
        @Path("coachId") coachId: String,
        @Body update: CoachProfileRequest
    ): Response<CoachModel>

    // 5. Update general user profile (name, elo, chessUsername)
    @PUT("api/users/profile/update/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body update: UserProfile
    ): Response<UserProfile>
}