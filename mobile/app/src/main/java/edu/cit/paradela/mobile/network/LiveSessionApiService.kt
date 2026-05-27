package edu.cit.paradela.mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class BoardUpdateRequest(val boardState: String, val pgnHistory: String)
data class NotesUpdateRequest(val notes: String)

data class LiveLessonModel(
    val id: String?,
    val status: String?,
    val boardState: String?,
    val pgnHistory: String?,
    val notes: String?
)

interface LiveSessionApiService {
    @GET("api/lessons/{id}")
    suspend fun getLessonById(@Path("id") lessonId: String): Response<LiveLessonModel>

    @PUT("api/lessons/{id}/board")
    suspend fun updateBoardState(@Path("id") lessonId: String, @Body request: BoardUpdateRequest): Response<LiveLessonModel>

    @PUT("api/lessons/{id}/notes")
    suspend fun saveLessonNotes(@Path("id") lessonId: String, @Body request: NotesUpdateRequest): Response<LiveLessonModel>
}
