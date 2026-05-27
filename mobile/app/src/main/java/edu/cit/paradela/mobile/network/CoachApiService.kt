package edu.cit.paradela.mobile.network

import edu.cit.paradela.mobile.models.*
import retrofit2.Response
import retrofit2.http.*

interface CoachApiService {

    @GET("api/lessons/coach/{coachId}")
    suspend fun getCoachLessons(@Path("coachId") coachId: String): Response<List<LessonModel>>

    @PUT("api/lessons/{lessonId}/status")
    suspend fun updateLessonStatus(
        @Path("lessonId") lessonId: String,
        @Query("status") status: String
    ): Response<LessonModel>

    @GET("api/users/coaches/{coachId}/profile")
    suspend fun getCoachProfile(@Path("coachId") coachId: String): Response<CoachModel>

    @PUT("api/users/coaches/{coachId}/profile")
    suspend fun updateCoachProfile(
        @Path("coachId") coachId: String,
        @Body update: CoachProfileRequest
    ): Response<CoachModel>

    @PUT("api/users/profile/update/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body update: UserProfile
    ): Response<UserProfile>
}
