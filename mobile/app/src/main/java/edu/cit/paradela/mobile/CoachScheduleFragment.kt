package edu.cit.paradela.mobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import edu.cit.paradela.mobile.models.LessonModel
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CoachScheduleFragment : Fragment() {

    private lateinit var llContainer: LinearLayout
    private var coachId: String = ""
    private var coachName: String = ""

    /** Polling coroutine — runs every 10 s while the fragment is visible. */
    private var pollingJob: Job? = null

    // ── Join window helpers ─────────────────────────────────────────────────

    private fun canJoinLesson(startTime: String?): Boolean {
        if (startTime == null) return true
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val lessonDate = sdf.parse(startTime) ?: return true
            val windowOpen = Date(lessonDate.time - 10 * 60 * 1000L)
            Date().after(windowOpen) || Date() == windowOpen
        } catch (e: Exception) { true }
    }

    private fun joinCountdownLabel(startTime: String?): String {
        if (startTime == null) return "Join Lesson"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val lessonDate = sdf.parse(startTime) ?: return "Join Lesson"
            val minsUntil = ((lessonDate.time - Date().time) / 60_000L).toInt().coerceAtLeast(0)
            if (minsUntil <= 10) return "Join Lesson"
            val hrs = minsUntil / 60; val mins = minsUntil % 60
            if (hrs > 0) "Opens in ${hrs}h ${mins}m" else "Opens in ${minsUntil} min"
        } catch (e: Exception) { "Join Lesson" }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_coach_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llContainer = view.findViewById(R.id.llCoachScheduleContainer)

        coachId   = requireActivity().intent.getStringExtra("USER_ID") ?: ""
        coachName = requireActivity().intent.getStringExtra("NAME")    ?: "Coach"
        // Initial fetch is triggered by onResume() below
    }

    // ── Polling lifecycle ───────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Fetch the lesson list immediately, then repeat every 10 seconds while
     * the fragment is in the foreground. New student bookings and status changes
     * appear automatically without the coach needing to refresh.
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                fetchCoachLessons()
                delay(10_000L)
            }
        }
    }

    private fun fetchCoachLessons() {
        if (coachId.isEmpty()) {
            showEmptyMessage("Not logged in. Please restart the app.")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Correct endpoint: GET /api/lessons/coach/{coachId}
                val response = RetrofitClient.coachService.getCoachLessons(coachId)

                withContext(Dispatchers.Main) {
                    llContainer.removeAllViews()

                    if (response.isSuccessful && response.body() != null) {
                        val lessons = response.body()!!
                        if (lessons.isEmpty()) {
                            showEmptyMessage("No lessons yet. Students will book with you soon!")
                        } else {
                            lessons.sortedByDescending { it.startTime }.forEach { lesson ->
                                createLessonCard(lesson)
                            }
                        }
                    } else {
                        showEmptyMessage("Failed to load lessons (${response.code()}).")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    llContainer.removeAllViews()
                    showEmptyMessage("Server offline. Try again later.")
                }
            }
        }
    }

    private fun createLessonCard(lesson: LessonModel) {
        val card = MaterialCardView(requireContext()).apply {
            setCardBackgroundColor(Color.WHITE)
            radius = 24f
            setContentPadding(40, 40, 40, 40)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
            layoutParams = params
        }

        val contentLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }

        // Student Name
        val nameText = TextView(requireContext()).apply {
            text = "Student: ${lesson.studentName ?: "Unknown Student"}"
            textSize = 20f
            setTextColor(Color.parseColor("#333333"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Time
        val timeText = TextView(requireContext()).apply {
            text = formatLessonTime(lesson.startTime)
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 8, 0, 24)
        }

        // Status Badge — kept as a reference so we can update it optimistically
        val statusBadge = TextView(requireContext()).apply {
            text = lesson.status ?: "PENDING"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(24, 8, 24, 8)
            applyStatusStyle(this, lesson.status ?: "PENDING")
        }

        contentLayout.addView(nameText)
        contentLayout.addView(timeText)
        contentLayout.addView(statusBadge)

        // Action buttons row — only for PENDING
        if (lesson.status == "PENDING") {
            val buttonRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 24 }
                layoutParams = params
            }

            val btnAccept = Button(requireContext()).apply {
                text = "Accept"
                setBackgroundColor(Color.parseColor("#15803D"))
                setTextColor(Color.WHITE)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { rightMargin = 16 }
                layoutParams = params
                setOnClickListener {
                    // Optimistic update — instant visual feedback
                    applyStatusStyle(statusBadge, "ACCEPTED")
                    statusBadge.text = "ACCEPTED"
                    buttonRow.visibility = View.GONE
                    updateLessonStatus(lesson.id ?: "", "ACCEPTED", statusBadge, buttonRow)
                }
            }

            val btnReject = Button(requireContext()).apply {
                text = "Reject"
                setBackgroundColor(Color.parseColor("#991B1B"))
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    // Optimistic update — instant visual feedback
                    applyStatusStyle(statusBadge, "REJECTED")
                    statusBadge.text = "REJECTED"
                    buttonRow.visibility = View.GONE
                    updateLessonStatus(lesson.id ?: "", "REJECTED", statusBadge, buttonRow)
                }
            }

            buttonRow.addView(btnAccept)
            buttonRow.addView(btnReject)
            contentLayout.addView(buttonRow)
        }

        // Join button — show for ACCEPTED lessons
        if (lesson.status == "ACCEPTED") {
            val btnJoin = Button(requireContext()).apply {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 24 }
                layoutParams = params

                if (canJoinLesson(lesson.startTime)) {
                    // Within 10-minute window — active gold
                    text = "Join Lesson"
                    setBackgroundColor(Color.parseColor("#D4AF37"))
                    isEnabled = true
                    alpha = 1f
                } else {
                    // Too early — grey countdown
                    text = joinCountdownLabel(lesson.startTime)
                    setBackgroundColor(Color.parseColor("#9E9E9E"))
                    isEnabled = false
                    alpha = 0.6f
                }
                setTextColor(Color.WHITE)

                setOnClickListener {
                    val intent = Intent(requireActivity(), LiveSessionActivity::class.java)
                    intent.putExtra("ROLE", "COACH")
                    intent.putExtra("NAME", coachName)
                    intent.putExtra("LESSON_ID", lesson.id)
                    startActivity(intent)
                }
            }
            contentLayout.addView(btnJoin)
        }

        card.addView(contentLayout)
        llContainer.addView(card)
    }

    /** Apply color + text style to a status badge view for the given status string. */
    private fun applyStatusStyle(badge: TextView, status: String) {
        when (status) {
            "ACCEPTED"  -> { badge.setTextColor(Color.parseColor("#1E40AF")); badge.setBackgroundColor(Color.parseColor("#DBEAFE")) }
            "COMPLETED" -> { badge.setTextColor(Color.parseColor("#15803D")); badge.setBackgroundColor(Color.parseColor("#DCFCE7")) }
            "REJECTED"  -> { badge.setTextColor(Color.parseColor("#991B1B")); badge.setBackgroundColor(Color.parseColor("#FEE2E2")) }
            else        -> { badge.setTextColor(Color.parseColor("#92400E")); badge.setBackgroundColor(Color.parseColor("#FEF3C7")) }
        }
    }

    /**
     * Update lesson status on the server.
     * The optimistic UI change is already applied by the caller before this runs.
     * On failure, we revert by reloading the full list from the server.
     */
    private fun updateLessonStatus(
        lessonId: String,
        status: String,
        statusBadge: TextView,
        buttonRow: LinearLayout
    ) {
        if (lessonId.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.coachService.updateLessonStatus(lessonId, status)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Lesson ${status.lowercase()}.", Toast.LENGTH_SHORT).show()
                        // Background sync — confirms the list matches server state
                        fetchCoachLessons()
                    } else {
                        // Revert the optimistic update by reloading from server
                        Toast.makeText(requireContext(), "Failed to update status.", Toast.LENGTH_SHORT).show()
                        fetchCoachLessons()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Network error.", Toast.LENGTH_SHORT).show()
                    fetchCoachLessons() // revert
                }
            }
        }
    }

    private fun showEmptyMessage(msg: String) {
        val tv = TextView(requireContext()).apply {
            text = msg
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 16)
        }
        llContainer.addView(tv)
    }

    /**
     * Converts "2026-05-26T10:00:00" → "May 26, 2026 • 10:00 AM"
     */
    private fun formatLessonTime(raw: String?): String {
        if (raw == null) return "Unknown Time"
        return try {
            val inFmt   = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateFmt = SimpleDateFormat("MMM dd, yyyy",           Locale.getDefault())
            val timeFmt = SimpleDateFormat("h:mm a",                 Locale.getDefault())
            val date = inFmt.parse(raw) ?: return raw
            "${dateFmt.format(date)} • ${timeFmt.format(date)}"
        } catch (e: Exception) { raw }
    }
}