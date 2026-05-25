package edu.cit.paradela.mobile.models

// Placeholder models to clear API interface errors
data class BaseResponse(val success: Boolean, val message: String)
data class LessonModel(val id: String, val date: String, val status: String)
data class ReviewModel(val id: String, val notes: String)
data class CoachModel(val id: String, val name: String, val elo: Int)
data class BookingRequest(val coachId: String, val date: String)
data class StudentProfile(val fullName: String, val chessHandle: String, val eloRating: Int)
data class CoachProfile(val fullName: String, val chessHandle: String, val eloRating: Int)

// --- AUTHENTICATION MODELS ---
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String, // "STUDENT" or "COACH"
    val eloRating: Int? = null,
    val chessHandle: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?, // Your Spring Boot JWT Token
    val role: String?,  // "STUDENT" or "COACH"
    val userId: String?
)