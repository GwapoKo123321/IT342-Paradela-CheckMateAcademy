package edu.cit.paradela.mobile.models

// --- AUTHENTICATION MODELS ---
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String,              // "STUDENT" or "Coach"
    val currentElo: Int? = null,   // backend field name
    val chessUsername: String? = null // backend field name
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,   // format: "session-{userId}"
    val role: String?,    // "STUDENT" or "Coach"
    val userId: String?
)

// --- LESSON MODELS ---
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

// Booking a new lesson — matches backend Lesson entity fields exactly
data class BookingRequest(
    val coachId: String,
    val studentId: String,
    val coachName: String,
    val studentName: String,
    val startTime: String,
    val endTime: String
)

// --- COACH MODELS ---
// Matches backend CoachProfileResponse exactly
data class CoachModel(
    val id: String?,
    val fullName: String?,       // backend: fullName
    val currentElo: Int?,        // backend: currentElo
    val chessUsername: String?,
    val specialties: String?,
    val bio: String?,
    val eloVerified: Boolean?
)

// Available coach time slot — matches backend CoachAvailableSlotResponse
data class CoachAvailableSlot(
    val coachId: String?,
    val coachName: String?,
    val currentElo: Int?,        // backend: currentElo
    val eloVerified: Boolean?,
    val specialties: String?,
    val bio: String?,
    val startTime: String?,
    val endTime: String?,
    val studentConflict: Boolean?,
    val conflictLabel: String?
)

// Coach availability slot for the profile request
data class CoachAvailabilitySlot(
    val dayOfWeek: Int?,   // 1=Monday, 7=Sunday
    val startTime: String?, // format: "HH:mm:ss"
    val endTime: String?    // format: "HH:mm:ss"
)

// Request body to update coach profile
data class CoachProfileRequest(
    val specialties: String,
    val bio: String,
    val availability: List<CoachAvailabilitySlot> = emptyList()
)

// --- USER PROFILE MODELS ---
// Matches backend User entity fields exactly
data class UserProfile(
    val id: String? = null,
    val email: String? = null,
    val fullName: String?,
    val chessUsername: String?,  // backend field name
    val currentElo: Int?,        // backend field name
    val role: String? = null,
    val eloVerified: Boolean? = null,
    val isFlagged: Boolean? = null
)

// --- GENERIC ---
data class BaseResponse(val success: Boolean, val message: String)
