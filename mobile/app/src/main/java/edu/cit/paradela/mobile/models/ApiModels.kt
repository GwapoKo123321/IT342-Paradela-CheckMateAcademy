package edu.cit.paradela.mobile.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String,
    val currentElo: Int? = null,
    val chessUsername: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val role: String?,
    val userId: String?,
    val data: AuthData? = null
)

data class AuthData(
    val user: UserProfile? = null,
    val accessToken: String? = null
)

data class LessonModel(
    val id: String?,
    val coachId: String?,
    val studentId: String?,
    val coachName: String?,
    val studentName: String?,
    val startTime: String?,
    val endTime: String?,
    val status: String?,
    val notes: String?,
    val boardState: String?,
    val pgnHistory: String?
)

data class BookingRequest(
    val coachId: String,
    val studentId: String,
    val coachName: String,
    val studentName: String,
    val startTime: String,
    val endTime: String
)

data class CoachModel(
    val id: String?,
    val fullName: String?,
    val currentElo: Int?,
    val chessUsername: String?,
    val specialties: String?,
    val bio: String?,
    val eloVerified: Boolean?,
    val availability: List<CoachAvailabilitySlot> = emptyList()
)

data class CoachAvailableSlot(
    val coachId: String?,
    val coachName: String?,
    val currentElo: Int?,
    val eloVerified: Boolean?,
    val specialties: String?,
    val bio: String?,
    val startTime: String?,
    val endTime: String?,
    val studentConflict: Boolean?,
    val conflictLabel: String?
)

data class CoachAvailabilitySlot(
    val dayOfWeek: Int?,
    val startTime: String?,
    val endTime: String?
)

data class CoachProfileRequest(
    val specialties: String,
    val bio: String,
    val availability: List<CoachAvailabilitySlot> = emptyList()
)

data class UserProfile(
    val id: String? = null,
    val email: String? = null,
    val fullName: String?,
    val chessUsername: String?,
    val currentElo: Int?,
    val role: String? = null,
    val eloVerified: Boolean? = null,
    val isFlagged: Boolean? = null
)

data class BaseResponse(val success: Boolean, val message: String)
