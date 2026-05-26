package edu.cit.paradela.mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

// The data shapes your Spring Boot server expects
data class BoardUpdateRequest(val boardState: String, val pgnHistory: String)
data class NotesUpdateRequest(val notes: String)

// Simplified model for the active lesson data
data class LiveLessonModel(
    val id: String?,
    val status: String?,
    val boardState: String?,
    val pgnHistory: String?,
    val notes: String? // This holds the stringified JSON chat array
)

interface LiveSessionApiService {
    @GET("api/lessons/{id}") // Change if your backend URL path is slightly different
    suspend fun getLessonById(@Path("id") lessonId: String): Response<LiveLessonModel>

    @PUT("api/lessons/{id}/board") // Change if your backend URL path is slightly different
    suspend fun updateBoardState(@Path("id") lessonId: String, @Body request: BoardUpdateRequest): Response<LiveLessonModel>

    @PUT("api/lessons/{id}/notes") // Change if your backend URL path is slightly different
    suspend fun saveLessonNotes(@Path("id") lessonId: String, @Body request: NotesUpdateRequest): Response<LiveLessonModel>
}